/*
 * #%L
 * *********************************************************************************************************************
 *
 * blueMarine2 - lightweight MediaCenter
 * http://bluemarine.tidalwave.it - hg clone https://bitbucket.org/tidalwave/bluemarine2-src
 * %%
 * Copyright (C) 2015 - 2015 Tidalwave s.a.s. (http://tidalwave.it)
 * %%
 *
 * *********************************************************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * *********************************************************************************************************************
 *
 * $Id$
 *
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.bluemarine2.mediascanner.impl;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.openrdf.model.URI;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.DC;
import org.openrdf.model.vocabulary.FOAF;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import it.tidalwave.util.Key;
import it.tidalwave.bluemarine2.model.Entity;
import it.tidalwave.bluemarine2.model.MediaItem;
import it.tidalwave.bluemarine2.model.MediaItem.Metadata;
import it.tidalwave.bluemarine2.vocabulary.BM;
import it.tidalwave.bluemarine2.vocabulary.MO;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import static java.util.stream.Collectors.*;
import static it.tidalwave.bluemarine2.mediascanner.impl.Utilities.*;
import it.tidalwave.bluemarine2.model.MediaFolder;
import it.tidalwave.bluemarine2.vocabulary.DbTune;
import it.tidalwave.bluemarine2.vocabulary.Purl;
import java.util.stream.Stream;
import lombok.Getter;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class EmbeddedMetadataManager 
  {
    @RequiredArgsConstructor @Getter @ToString
    static class Entry
      {
        @Nonnull
        private final URI uri;
        
        @Nonnull
        private final String name;
      }

    @Inject
    private DbTuneMetadataManager dbTuneMetadataManager;
    
    @Inject
    private StatementManager statementManager;
    
    @Inject
    private IdCreator idCreator;
    
    @Inject
    private Shared shared;
    
    /*******************************************************************************************************************
     *
     * 
     * 
     ******************************************************************************************************************/
    @Immutable @RequiredArgsConstructor @ToString
    static class Pair
      {
        @Nonnull
        private final URI predicate;
        
        @Nonnull
        private final Value object;
        
        @Nonnull
        public Statement createStatementWithSubject (final @Nonnull URI subject)
          {
            return ValueFactoryImpl.getInstance().createStatement(subject, predicate, object);
          }
      }
    
    /*******************************************************************************************************************
     *
     * Facility that creates a request to add statements for the giving {@link Metadata} and {@code subjectURi}. It
     * maps metadata items to the proper statement predicate and literal.
     * 
     ******************************************************************************************************************/
    static class Mapper extends HashMap<Key<?>, Function<Object, Pair>>
      {
        @Nonnull
        public List<Statement> statementsFor (final @Nonnull Metadata metadata, final @Nonnull URI subjectUri)
          {
            return metadata.getEntries().stream()
                                        .filter(e -> containsKey(e.getKey()))
                                        .map(e -> forEntry(e).createStatementWithSubject(subjectUri))
                                        .collect(toList());
          }
                
        @Nonnull
        private Pair forEntry (final @Nonnull Map.Entry<Key<?>, ?> entry)
          {
            return get(entry.getKey()).apply(entry.getValue());
          }
      }

    private static final Mapper SIGNAL_MAPPER = new Mapper();
    private static final Mapper TRACK_MAPPER = new Mapper();

    static
      {
        TRACK_MAPPER. put(Metadata.TRACK,       v -> new Pair(MO.P_TRACK_NUMBER,    literalFor((int)v)));
    
        SIGNAL_MAPPER.put(Metadata.SAMPLE_RATE, v -> new Pair(MO.P_SAMPLE_RATE,     literalFor((int)v)));
        SIGNAL_MAPPER.put(Metadata.BIT_RATE,    v -> new Pair(MO.P_BITS_PER_SAMPLE, literalFor((int)v)));
        SIGNAL_MAPPER.put(Metadata.DURATION,    v -> new Pair(MO.P_DURATION,        
                                                           literalFor((float)((Duration)v).toMillis())));
      }
    
    /*******************************************************************************************************************
     *
     * Imports the metadata embedded in a file for the given {@link MediaItem}. It only processes the portion of 
     * metadata which are never superseded by external catalogs (such as sample rate, duration, etc...).
     * 
     * @param   mediaItem               the {@code MediaItem}.
     * @param   signalUri               the URI of the signal
     * @param   trackUri                the URI of the track
     * 
     ******************************************************************************************************************/
    public void importAudioFileMetadata (final @Nonnull MediaItem mediaItem, 
                                         final @Nonnull URI signalUri,
                                         final @Nonnull URI trackUri)
      {
        log.debug("importAudioFileMetadata({}, {}, {})", mediaItem, signalUri, trackUri);
        final Metadata metadata = mediaItem.getMetadata();
        statementManager.requestAdd(SIGNAL_MAPPER.statementsFor(metadata, signalUri));
        statementManager.requestAdd(TRACK_MAPPER.statementsFor(metadata, trackUri));
      }
    
    /*******************************************************************************************************************
     *
     * Imports all the remaining metadata embedded in a track for the given {@link MediaItem}. This method is called
     * when we failed to match a track to an external catalog.
     * 
     * @param   mediaItem               the {@code MediaItem}.
     * @param   trackUri                the URI of the track
     *
     ******************************************************************************************************************/
    public void importFallbackTrackMetadata (final @Nonnull MediaItem mediaItem, final @Nonnull URI trackUri) 
      {
        // FIXME: scenarios can be complex. For instance, we could have a valid MBZ artistId - we might be here just
        // because we couldn't retrieve the DbTune resource for the track.
        // Also, consider the COMPOSER.
        log.debug("importFallbackTrackMetadata({}, {})", mediaItem, trackUri);
        
        final Metadata metadata           = mediaItem.getMetadata();  
        final Entity parent               = mediaItem.getParent();
        log.debug(">>>> metadata of {}: {}", trackUri, metadata);
        
        final Optional<String> title      = metadata.get(Metadata.TITLE);
        final Optional<String> makerName  = metadata.get(Metadata.ARTIST);
        Optional<URI> makerUri      = makerName.map(name -> createUriForLocalArtist(name));

        // FIXME: some are multiple, split by / - should be done in Metadata
        List<Entry> artists  = metadata.getAll(Metadata.MBZ_ARTIST_ID).stream()
                .map(i -> new Entry(uriFor("http://dbtune.org/musicbrainz/resource/artist/" + i), makerName.orElse("???"))).collect(toList());
            
        if (artists.isEmpty())
          {
            artists = makerName
                .map(name -> Stream.of(name.split("[;]")).map(String::trim)).orElse(Stream.empty())
                .map(name -> new Entry(createUriForLocalArtist(name), name))
                .collect(toList());
          }
        else
          {
            makerUri = Optional.of(artists.get(0).getUri()); // FIXME Not only the first one
            dbTuneMetadataManager.requestArtistMetadata(makerUri.get(), makerName);
            
//            artists.clear(); // don't try to add new artists - we don't have the name. We hope we have already their data somewhere 
        /*
            FIXME: Unfortunately, we don't have their data when there are no other tracks/records that trigger a download
            from DbTune. E.g. we're missing:
            
        Alexander Scriabin
        Anonyme Grèce
        Anonymus (País Vasco)
        Antonio Carlos Jobim
        Astor Piazzolla
        Bedřich Smetana
        Berliner Philharmoniker & Rafael Kubelík,
        Claudio Monteverdi
        Cristóbal de Morales
        etc..
            
            By not clearing the list, many are retrieved, but still missing:
            
            Anonyme Grèce
            Anonymus (País Vasco)
            Berliner Philharmoniker & Rafael Kubelík
            
            There should be a fallback strategy that creates the data here from the embedded artist name.
            We should accumulate them and insert only at the very end?
            If we don't clear the list, we're creating them with the embedded name, that could be worse than the one
            in DbTune. 
            
            IDEA: put them in a fallbackArtistMap (uri -> name) and post a request for donload DbTunes data. 
            On receiving the results, in case of errors, if the artist is registered in the fallback map, the local
            name will be inserted.
        
        */
        
            
          }
        
        final List<Entry> newArtists   = artists.stream().filter(
                e -> shared.seenArtistUris.putIfAbsentAndGetNewKey(e.getUri(), Optional.empty()).isPresent())
                .collect(toList());
        final List<URI> newArtistUris       = newArtists.stream().map(Entry::getUri).collect(toList());
        final List<Value> newArtistLiterals = newArtists.stream().map(e -> literalFor(e.getName())).collect(toList());
        
        final Optional<URI> newGroupUri = (artists.size() <= 1) ? Optional.empty()
                : shared.seenArtistUris.putIfAbsentAndGetNewKey(makerUri, Optional.empty());

//        final List<Id> artistsMBIds       = metadata.getAll(Metadata.MBZ_ARTIST_ID); TODO
        
        final String recordTitle          = metadata.get(Metadata.ALBUM)
                                                    .orElse(((MediaFolder)parent).getPath().toFile().getName()); // FIXME
//                                                    .orElse(parent.as(Displayable).getDisplayName());
        
        final Optional<URI> recordUri     = Optional.of(createUriForLocalRecord(recordTitle));
        final Optional<URI> newRecordUri  = shared.seenRecordUris.putIfAbsentAndGetNewKey(recordUri, true);
        
        statementManager.requestAddStatements()
            .with(trackUri,      RDFS.LABEL,                literalFor(title))
            .with(trackUri,      DC.TITLE,                  literalFor(title))
            .with(trackUri,      FOAF.MAKER,                makerUri)

            .with(recordUri,     MO.P_TRACK,                trackUri)

            .with(newRecordUri,  RDF.TYPE,                  MO.C_RECORD)
            .with(newRecordUri,  RDFS.LABEL,                literalFor(recordTitle))
            .with(newRecordUri,  DC.TITLE,                  literalFor(recordTitle))
            .with(newRecordUri,  FOAF.MAKER,                makerUri)
            .with(newRecordUri,  MO.P_MEDIA_TYPE,           MO.C_CD)
                
            .with(newArtistUris, RDF.TYPE,                  MO.C_MUSIC_ARTIST)
            .with(newArtistUris, RDFS.LABEL,                newArtistLiterals)
            .with(newArtistUris, FOAF.NAME,                 newArtistLiterals)
                
            .with(newGroupUri,   RDF.TYPE,                  MO.C_MUSIC_ARTIST)
            .with(newGroupUri,   RDFS.LABEL,                literalFor(makerName))
            .with(newGroupUri,   FOAF.NAME,                 literalFor(makerName))
            .with(newGroupUri,   DbTune.ARTIST_TYPE,        literalFor((short)2))
            .with(newGroupUri,   Purl.COLLABORATES_WITH,    artists.stream().map(Entry::getUri))
            .publish();
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private URI createUriForLocalRecord (final @Nonnull String recordTitle) 
      {
        return BM.localRecordUriFor(idCreator.createSha1("RECORD:" + recordTitle));
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private URI createUriForLocalArtist (final @Nonnull String name) 
      {
        return BM.localArtistUriFor(idCreator.createSha1("ARTIST:" + name));
      }
  }
