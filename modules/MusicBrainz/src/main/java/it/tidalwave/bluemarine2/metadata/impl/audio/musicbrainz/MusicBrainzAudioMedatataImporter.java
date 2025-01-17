/*
 * *********************************************************************************************************************
 *
 * blueMarine II: Semantic Media Centre
 * http://tidalwave.it/projects/bluemarine2
 *
 * Copyright (C) 2015 - 2021 by Tidalwave s.a.s. (http://tidalwave.it)
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
 * git clone https://bitbucket.org/tidalwave/bluemarine2-src
 * git clone https://github.com/tidalwave-it/bluemarine2-src
 *
 * *********************************************************************************************************************
 */
package it.tidalwave.bluemarine2.metadata.impl.audio.musicbrainz;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.io.IOException;
import java.math.BigInteger;
import javax.xml.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.musicbrainz.ns.mmd_2.Artist;
import org.musicbrainz.ns.mmd_2.DefTrackData;
import org.musicbrainz.ns.mmd_2.Disc;
import org.musicbrainz.ns.mmd_2.Medium;
import org.musicbrainz.ns.mmd_2.MediumList;
import org.musicbrainz.ns.mmd_2.Offset;
import org.musicbrainz.ns.mmd_2.Recording;
import org.musicbrainz.ns.mmd_2.Relation;
import org.musicbrainz.ns.mmd_2.Relation.AttributeList.Attribute;
import org.musicbrainz.ns.mmd_2.RelationList;
import org.musicbrainz.ns.mmd_2.Release;
import org.musicbrainz.ns.mmd_2.ReleaseGroup;
import org.musicbrainz.ns.mmd_2.ReleaseGroupList;
import org.musicbrainz.ns.mmd_2.ReleaseList;
import it.tidalwave.util.Id;
import it.tidalwave.bluemarine2.util.ModelBuilder;
import it.tidalwave.bluemarine2.model.MediaItem;
import it.tidalwave.bluemarine2.model.MediaItem.Metadata;
import it.tidalwave.bluemarine2.model.vocabulary.*;
import it.tidalwave.bluemarine2.metadata.cddb.CddbAlbum;
import it.tidalwave.bluemarine2.metadata.cddb.CddbMetadataProvider;
import it.tidalwave.bluemarine2.metadata.musicbrainz.MusicBrainzMetadataProvider;
import it.tidalwave.bluemarine2.rest.RestResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.With;
import lombok.extern.slf4j.Slf4j;
import static java.util.Collections.*;
import static java.util.Comparator.*;
import static java.util.Map.entry;
import static java.util.stream.Collectors.*;
import static it.tidalwave.util.FunctionalCheckedExceptionWrappers.*;
import static it.tidalwave.bluemarine2.util.RdfUtilities.*;
import static it.tidalwave.bluemarine2.model.MediaItem.Metadata.*;
import static it.tidalwave.bluemarine2.metadata.musicbrainz.MusicBrainzMetadataProvider.*;
import static lombok.AccessLevel.PRIVATE;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Slf4j
@RequiredArgsConstructor
public class MusicBrainzAudioMedatataImporter
  {
    enum Validation
      {
        TRACK_OFFSETS_MATCH_REQUIRED,
        TRACK_OFFSETS_MATCH_NOT_REQUIRED
      }

    private static final QName QNAME_SCORE = new QName("http://musicbrainz.org/ns/ext#-2.0", "score");

    private static final ValueFactory FACTORY = SimpleValueFactory.getInstance();

    private static final String[] TOC_INCLUDES = { "aliases", "artist-credits", "labels", "recordings" };

    private static final String[] RELEASE_INCLUDES = { "aliases", "artist-credits", "discids", "labels", "recordings" };

    private static final String[] RECORDING_INCLUDES = { "aliases", "artist-credits", "artist-rels" };

    private static final IRI SOURCE_MUSICBRAINZ = FACTORY.createIRI(BMMO.NS, "source#musicbrainz");

    @Nonnull
    private final CddbMetadataProvider cddbMetadataProvider;

    @Nonnull
    private final MusicBrainzMetadataProvider mbMetadataProvider;

    @Getter @Setter
    private int trackOffsetsMatchThreshold = 2500;

    @Getter @Setter
    private int releaseGroupScoreThreshold = 50;

    /** If {@code true}, in case of multiple collections to pick from, those that are not the least one are marked as
        alternative. */
    @Getter @Setter
    private boolean discourageCollections = true;

    private final Set<String> processedTocs = new HashSet<>();

    private static final Map<String, IRI> PERFORMER_MAP = Map.ofEntries(
        entry("arranger",                           BMMO.P_ARRANGER),
        entry("balance",                            BMMO.P_BALANCE),
        entry("chorus master",                      BMMO.P_CHORUS_MASTER),
        entry("conductor",                          MO.P_CONDUCTOR),
        entry("editor",                             BMMO.P_EDITOR),
        entry("engineer",                           MO.P_ENGINEER),
        entry("instrument arranger",                BMMO.P_ARRANGER),
        entry("mastering",                          BMMO.P_MASTERING),
        entry("mix",                                BMMO.P_MIX),
        entry("orchestrator",                       BMMO.P_ORCHESTRATOR),
        entry("performer",                          MO.P_PERFORMER),
        entry("performing orchestra",               BMMO.P_ORCHESTRA),
        entry("producer",                           MO.P_PRODUCER),
        entry("programming",                        BMMO.P_PROGRAMMING),
        entry("recording",                          BMMO.P_RECORDING),
        entry("remixer",                            BMMO.P_MIX),
        entry("sound",                              MO.P_ENGINEER),

        entry("vocal",                              MO.P_SINGER),
        entry("vocal/additional",                   BMMO.P_BACKGROUND_SINGER),
        entry("vocal/alto vocals",                  BMMO.P_ALTO),
        entry("vocal/background vocals",            BMMO.P_BACKGROUND_SINGER),
        entry("vocal/baritone vocals",              BMMO.P_BARITONE),
        entry("vocal/bass-baritone vocals",         BMMO.P_BASS_BARITONE),
        entry("vocal/bass vocals",                  BMMO.P_BASS),
        entry("vocal/choir vocals",                 BMMO.P_CHOIR),
        entry("vocal/contralto vocals",             BMMO.P_CONTRALTO),
        entry("vocal/guest",                        MO.P_SINGER),
        entry("vocal/lead vocals",                  BMMO.P_LEAD_SINGER),
        entry("vocal/mezzo-soprano vocals",         BMMO.P_MEZZO_SOPRANO),
        entry("vocal/other vocals",                 BMMO.P_BACKGROUND_SINGER),
        entry("vocal/solo",                         BMMO.P_LEAD_SINGER),
        entry("vocal/soprano vocals",               BMMO.P_SOPRANO),
        entry("vocal/spoken vocals",                MO.P_SINGER),
        entry("vocal/tenor vocals",                 BMMO.P_TENOR),

        entry("instrument",                         MO.P_PERFORMER),
        entry("instrument/accordion",               BMMO.P_PERFORMER_ACCORDION),
        entry("instrument/acoustic guitar",         BMMO.P_PERFORMER_ACOUSTIC_GUITAR),
        entry("instrument/acoustic bass guitar",    BMMO.P_PERFORMER_ACOUSTIC_BASS_GUITAR),
        entry("instrument/agogô",                   BMMO.P_PERFORMER_AGOGO),
        entry("instrument/alto saxophone",          BMMO.P_PERFORMER_ALTO_SAX),
        entry("instrument/banjo",                   BMMO.P_PERFORMER_BANJO),
        entry("instrument/baritone guitar",         BMMO.P_PERFORMER_BARITONE_GUITAR),
        entry("instrument/baritone saxophone",      BMMO.P_PERFORMER_BARITONE_SAX),
        entry("instrument/bass",                    BMMO.P_PERFORMER_BASS),
        entry("instrument/bass clarinet",           BMMO.P_PERFORMER_BASS_CLARINET),
        entry("instrument/bass drum",               BMMO.P_PERFORMER_BASS_DRUM),
        entry("instrument/bass guitar",             BMMO.P_PERFORMER_BASS_GUITAR),
        entry("instrument/bass trombone",           BMMO.P_PERFORMER_BASS_TROMBONE),
        entry("instrument/bassoon",                 BMMO.P_PERFORMER_BASSOON),
        entry("instrument/bells",                   BMMO.P_PERFORMER_BELLS),
        entry("instrument/berimbau",                BMMO.P_PERFORMER_BERIMBAU),
        entry("instrument/brass",                   BMMO.P_PERFORMER_BRASS),
        entry("instrument/brushes",                 BMMO.P_PERFORMER_BRUSHES),
        entry("instrument/cello",                   BMMO.P_PERFORMER_CELLO),
        entry("instrument/clarinet",                BMMO.P_PERFORMER_CLARINET),
        entry("instrument/classical guitar",        BMMO.P_PERFORMER_CLASSICAL_GUITAR),
        entry("instrument/congas",                  BMMO.P_PERFORMER_CONGAS),
        entry("instrument/cornet",                  BMMO.P_PERFORMER_CORNET),
        entry("instrument/cymbals",                 BMMO.P_PERFORMER_CYMBALS),
        entry("instrument/double bass",             BMMO.P_PERFORMER_DOUBLE_BASS),
        entry("instrument/drums",                   BMMO.P_PERFORMER_DRUMS),
        entry("instrument/drum machine",            BMMO.P_PERFORMER_DRUM_MACHINE),
        entry("instrument/electric bass guitar",    BMMO.P_PERFORMER_ELECTRIC_BASS_GUITAR),
        entry("instrument/electric guitar",         BMMO.P_PERFORMER_ELECTRIC_GUITAR),
        entry("instrument/electric piano",          BMMO.P_PERFORMER_ELECTRIC_PIANO),
        entry("instrument/electric sitar",          BMMO.P_PERFORMER_ELECTRIC_SITAR),
        entry("instrument/electronic drum set",     BMMO.P_PERFORMER_ELECTRONIC_DRUM_SET),
        entry("instrument/english horn",            BMMO.P_PERFORMER_ENGLISH_HORN),
        entry("instrument/flugelhorn",              BMMO.P_PERFORMER_FLUGELHORN),
        entry("instrument/flute",                   BMMO.P_PERFORMER_FLUTE),
        entry("instrument/frame drum",              BMMO.P_PERFORMER_FRAME_DRUM),
        entry("instrument/french horn",             BMMO.P_PERFORMER_FRENCH_HORN),
        entry("instrument/glockenspiel",            BMMO.P_PERFORMER_GLOCKENSPIEL),
        entry("instrument/grand piano",             BMMO.P_PERFORMER_GRAND_PIANO),
        entry("instrument/guest",                   BMMO.P_PERFORMER_GUEST),
        entry("instrument/guitar",                  BMMO.P_PERFORMER_GUITAR),
        entry("instrument/guitar synthesizer",      BMMO.P_PERFORMER_GUITAR_SYNTHESIZER),
        entry("instrument/guitars",                 BMMO.P_PERFORMER_GUITARS),
        entry("instrument/handclaps",               BMMO.P_PERFORMER_HANDCLAPS),
        entry("instrument/hammond organ",           BMMO.P_PERFORMER_HAMMOND_ORGAN),
        entry("instrument/harmonica",               BMMO.P_PERFORMER_HARMONICA),
        entry("instrument/harp",                    BMMO.P_PERFORMER_HARP),
        entry("instrument/harpsichord",             BMMO.P_PERFORMER_HARPSICHORD),
        entry("instrument/hi-hat",                  BMMO.P_PERFORMER_HIHAT),
        entry("instrument/horn",                    BMMO.P_PERFORMER_HORN),
        entry("instrument/keyboard",                BMMO.P_PERFORMER_KEYBOARD),
        entry("instrument/koto",                    BMMO.P_PERFORMER_KOTO),
        entry("instrument/lute",                    BMMO.P_PERFORMER_LUTE),
        entry("instrument/maracas",                 BMMO.P_PERFORMER_MARACAS),
        entry("instrument/marimba",                 BMMO.P_PERFORMER_MARIMBA),
        entry("instrument/mellophone",              BMMO.P_PERFORMER_MELLOPHONE),
        entry("instrument/melodica",                BMMO.P_PERFORMER_MELODICA),
        entry("instrument/oboe",                    BMMO.P_PERFORMER_OBOE),
        entry("instrument/organ",                   BMMO.P_PERFORMER_ORGAN),
        entry("instrument/other instruments",       BMMO.P_PERFORMER_OTHER_INSTRUMENTS),
        entry("instrument/percussion",              BMMO.P_PERFORMER_PERCUSSION),
        entry("instrument/piano",                   BMMO.P_PERFORMER_PIANO),
        entry("instrument/piccolo trumpet",         BMMO.P_PERFORMER_PICCOLO_TRUMPET),
        entry("instrument/pipe organ",              BMMO.P_PERFORMER_PIPE_ORGAN),
        entry("instrument/psaltery",                BMMO.P_PERFORMER_PSALTERY),
        entry("instrument/recorder",                BMMO.P_PERFORMER_RECORDER),
        entry("instrument/reeds",                   BMMO.P_PERFORMER_REEDS),
        entry("instrument/rhodes piano",            BMMO.P_PERFORMER_RHODES_PIANO),
        entry("instrument/santur",                  BMMO.P_PERFORMER_SANTUR),
        entry("instrument/saxophone",               BMMO.P_PERFORMER_SAXOPHONE),
        entry("instrument/shakers",                 BMMO.P_PERFORMER_SHAKERS),
        entry("instrument/sitar",                   BMMO.P_PERFORMER_SITAR),
        entry("instrument/slide guitar",            BMMO.P_PERFORMER_SLIDE_GUITAR),
        entry("instrument/snare drum",              BMMO.P_PERFORMER_SNARE_DRUM),
        entry("instrument/solo",                    BMMO.P_PERFORMER_SOLO),
        entry("instrument/soprano saxophone",       BMMO.P_PERFORMER_SOPRANO_SAX),
        entry("instrument/spanish acoustic guitar", BMMO.P_PERFORMER_SPANISH_ACOUSTIC_GUITAR),
        entry("instrument/steel guitar",            BMMO.P_PERFORMER_STEEL_GUITAR),
        entry("instrument/synclavier",              BMMO.P_PERFORMER_SYNCLAVIER),
        entry("instrument/synthesizer",             BMMO.P_PERFORMER_SYNTHESIZER),
        entry("instrument/tambourine",              BMMO.P_PERFORMER_TAMBOURINE),
        entry("instrument/tenor saxophone",         BMMO.P_PERFORMER_TENOR_SAX),
        entry("instrument/timbales",                BMMO.P_PERFORMER_TIMBALES),
        entry("instrument/timpani",                 BMMO.P_PERFORMER_TIMPANI),
        entry("instrument/tiple",                   BMMO.P_PERFORMER_TIPLE),
        entry("instrument/trombone",                BMMO.P_PERFORMER_TROMBONE),
        entry("instrument/trumpet",                 BMMO.P_PERFORMER_TRUMPET),
        entry("instrument/tuba",                    BMMO.P_PERFORMER_TUBA),
        entry("instrument/tubular bells",           BMMO.P_PERFORMER_TUBULAR_BELLS),
        entry("instrument/tuned percussion",        BMMO.P_PERFORMER_TUNED_PERCUSSION),
        entry("instrument/ukulele",                 BMMO.P_PERFORMER_UKULELE),
        entry("instrument/vibraphone",              BMMO.P_PERFORMER_VIBRAPHONE),
        entry("instrument/viola",                   BMMO.P_PERFORMER_VIOLA),
        entry("instrument/viola da gamba",          BMMO.P_PERFORMER_VIOLA_DA_GAMBA),
        entry("instrument/violin",                  BMMO.P_PERFORMER_VIOLIN),
        entry("instrument/whistle",                 BMMO.P_PERFORMER_WHISTLE),
        entry("instrument/xylophone",               BMMO.P_PERFORMER_XYLOPHONE));

    /*******************************************************************************************************************
     *
     * Aggregate of a {@link Release}, a {@link Medium} inside that {@code Release} and a {@link Disc} inside that
     * {@code Medium}.
     *
     ******************************************************************************************************************/
    @RequiredArgsConstructor @AllArgsConstructor @Getter
    static class ReleaseMediumDisk
      {
        @Nonnull
        private final Release release;

        @Nonnull
        private final Medium medium;

        @With
        private Disc disc;

        @With
        private boolean alternative;

        private String embeddedTitle;

        private int score;

        /***************************************************************************************************************
         *
         **************************************************************************************************************/
        @Nonnull
        public ReleaseMediumDisk withEmbeddedTitle (@Nonnull final String embeddedTitle)
          {
            return new ReleaseMediumDisk(release, medium, disc, alternative, embeddedTitle,
                                         similarity(pickTitle(), embeddedTitle));
          }

        /***************************************************************************************************************
         *
         * Prefer Medium title - typically available in case of disk collections, in which case Release has got
         * the collection title, which is very generic.
         *
         **************************************************************************************************************/
        @Nonnull
        public String pickTitle()
          {
            return Optional.ofNullable(medium.getTitle()).orElse(release.getTitle());
          }

        /***************************************************************************************************************
         *
         **************************************************************************************************************/
        @Nonnull
        public ReleaseMediumDisk alternativeIf (final boolean condition)
          {
            return withAlternative(alternative || condition);
          }

        /***************************************************************************************************************
         *
         **************************************************************************************************************/
        @Nonnull
        public Id computeId()
          {
            return createSha1IdNew(getRelease().getId() + "+" + getDisc().getId());
          }

        /***************************************************************************************************************
         *
         **************************************************************************************************************/
        @Nonnull
        public Optional<Integer> getDiskCount()
          {
            return Optional.ofNullable(release.getMediumList()).map(MediumList::getCount).map(BigInteger::intValue);
          }

        /***************************************************************************************************************
         *
         **************************************************************************************************************/
        @Nonnull
        public Optional<Integer> getDiskNumber()
          {
            return Optional.ofNullable(medium.getPosition()).map(BigInteger::intValue);
          }

        /***************************************************************************************************************
         *
         **************************************************************************************************************/
        @Nonnull
        public Optional<String> getAsin()
          {
            return Optional.ofNullable(release.getAsin());
          }

        /***************************************************************************************************************
         *
         **************************************************************************************************************/
        @Nonnull
        public Optional<String> getBarcode()
          {
            return Optional.ofNullable(release.getBarcode());
          }

        /***************************************************************************************************************
         *
         **************************************************************************************************************/
        @Nonnull
        public Cddb getCddb()
          {
            return MediaItem.Metadata.Cddb.builder()
                    .discId("") // FIXME
                    .trackFrameOffsets(disc.getOffsetList().getOffset()
                            .stream()
                            .map(Offset::getValue)
                            .mapToInt(BigInteger::intValue)
                            .toArray())
                    .build();
          }

        /***************************************************************************************************************
         *
         **************************************************************************************************************/
        @Nonnull
        public String getMediumAndDiscString()
          {
            return String.format("%s/%s", medium.getTitle(), (disc != null) ? disc.getId() : "null");
          }

        /***************************************************************************************************************
         *
         **************************************************************************************************************/
        @Override
        public boolean equals (@Nullable final Object other)
          {
            if (this == other)
              {
                return true;
              }

            if ((other == null) || (getClass() != other.getClass()))
              {
                return false;
              }

            return Objects.equals(this.computeId(), ((ReleaseMediumDisk)other).computeId());
          }

        /***************************************************************************************************************
         *
         **************************************************************************************************************/
        @Override
        public int hashCode()
          {
            return computeId().hashCode();
          }

        /***************************************************************************************************************
         *
         **************************************************************************************************************/
        @Override @Nonnull
        public String toString()
          {
            return String.format("ALT: %-5s %s ASIN: %-10s BARCODE: %-13s SCORE: %4d #: %3s/%3s " +
                                 "TITLES: PICKED: %s EMBEDDED: %s RELEASE: %s MEDIUM: %s",
                                  alternative,
                                  release.getId(),
                                  release.getAsin(),
                                  release.getBarcode(),
                                  getScore(),
                                  getDiskNumber().map(Number::toString).orElse(""),
                                  getDiskCount().map(Number::toString).orElse(""),
                                  pickTitle(), embeddedTitle, release.getTitle(), medium.getTitle());
          }
      }

    /*******************************************************************************************************************
     *
     * Aggregate of a {@link Relation} and a target type.
     *
     ******************************************************************************************************************/
    @RequiredArgsConstructor(access = PRIVATE) @Getter
    static class RelationAndTargetType
      {
        @Nonnull
        private final Relation relation;

        @Nonnull
        private final String targetType;

        @Nonnull
        public static Stream<RelationAndTargetType> toStream (@Nonnull final RelationList relationList)
          {
            return relationList.getRelation().stream()
                                             .map(rel -> new RelationAndTargetType(rel, relationList.getTargetType()));
          }
      }

    /*******************************************************************************************************************
     *
     * Downloads and imports MusicBrainz data for the given {@link Metadata}.
     *
     * @param   metadata                the {@code Metadata}
     * @return                          the RDF triples
     * @throws  InterruptedException    in case of I/O error
     * @throws  IOException             in case of I/O error
     *
     ******************************************************************************************************************/
    @Nonnull
    public Optional<Model> handleMetadata (@Nonnull final Metadata metadata)
      throws InterruptedException, IOException
      {
        final ModelBuilder model                  = createModelBuilder();
        final Optional<String> optionalAlbumTitle = metadata.get(ALBUM);
        final Optional<Cddb> optionalCddb         = metadata.get(CDDB);

        if (optionalAlbumTitle.isPresent() && !optionalAlbumTitle.get().trim().isEmpty() && optionalCddb.isPresent())
          {
            final String albumTitle = optionalAlbumTitle.get();
            final Cddb cddb         = optionalCddb.get();
            final String toc        = cddb.getToc();

            synchronized (processedTocs)
              {
                if (processedTocs.contains(toc))
                  {
                    return Optional.empty();
                  }

                processedTocs.add(toc);
              }

            log.info("QUERYING MUSICBRAINZ FOR TOC OF: {}", albumTitle);
            final List<ReleaseMediumDisk> rmds = new ArrayList<>();
            final RestResponse<ReleaseList> releaseList = mbMetadataProvider.findReleaseListByToc(toc, TOC_INCLUDES);
            // even though we're querying by TOC, matching offsets is required to kill many false results
            releaseList.ifPresent(releases -> rmds.addAll(findReleases(releases, cddb, Validation.TRACK_OFFSETS_MATCH_REQUIRED)));

            if (rmds.isEmpty())
              {
                log.info("TOC NOT FOUND, QUERYING MUSICBRAINZ FOR TITLE: {}", albumTitle);
                final List<ReleaseGroup> releaseGroups = new ArrayList<>();
                releaseGroups.addAll(mbMetadataProvider.findReleaseGroupByTitle(albumTitle)
                                                       .map(ReleaseGroupList::getReleaseGroup)
                                                       .orElse(emptyList()));

                final Optional<String> alternateTitle = cddbAlternateTitleOf(metadata);
                alternateTitle.ifPresent(t -> log.info("ALSO USING ALTERNATE TITLE: {}", t));
                releaseGroups.addAll(alternateTitle.map(_f(mbMetadataProvider::findReleaseGroupByTitle))
                                                   .map(response -> response.get().getReleaseGroup())
                                                   .orElse(emptyList()));
                rmds.addAll(findReleases(releaseGroups, cddb, Validation.TRACK_OFFSETS_MATCH_REQUIRED));
              }

            model.with(markedAlternative(rmds, albumTitle).stream()
                                                          .parallel()
                                                          .map(_f(rmd -> handleRelease(metadata, rmd)))
                                                          .collect(toList()));
          }

        return Optional.of(model.toModel());
      }

    /*******************************************************************************************************************
     *
     * Given a valid list of {@link ReleaseMediumDisk}s - that is, that has been already validated and correctly matches
     * the searched record - if it contains more than one element picks the most suitable one. Unwanted elements are
     * not filtered out, because it's not always possible to automatically pick the best one: in fact, some entries
     * might differ for ASIN or barcode; or might be items individually sold or part of a collection. It makes sense to
     * offer the user the possibility of manually pick them later. So, instead of being filtered out, those elements
     * are marked as "alternative" (and they will be later marked as such in the triple store).
     *
     * These are the performed steps:
     *
     * <ol>
     * <li>Eventual duplicates are collapsed.</li>
     * <li>If required, in case of members of collections, collections that are larger than the least are marked as
     *     alternative.</li>
     * <li>A matching score is computed about the affinity of the title found in MusicBrainz metadata with respect
     *     to the title in the embedded metadata: elements that don't reach the maximum score are marked as alternative.
     * </li>
     * <li>If at least one element has got an ASIN, other elements that don't bear it are marked as alternative.</li>
     * <li>If at least one element has got a barcode, other elements that don't bear it are marked as alternative.</li>
     * <li>If the pick is not unique yet, an ASIN is picked as the first in lexicographic order and elements not
     *     bearing it are marked as alternative.</li>
     * <li>If the pick is not unique yet, a barcode is picked as the first in lexicographic order and elements not
     *     bearing it are marked as alternative.</li>
     * <li>If the pick is not unique yet, elements other than the first one are marked as alternative.</i>
     * </ol>
     *
     * The last criteria are implemented for giving consistency to automated tests, considering that the order in which
     * elements are found is not guaranteed because of multi-threading.
     *
     * @param   inRmds          the incoming {@code ReleaseAndMedium}s
     * @param   embeddedTitle   the album title found in the file
     * @return                  the processed {@code ReleaseAndMedium}s
     *
     ******************************************************************************************************************/
    @Nonnull
    private List<ReleaseMediumDisk> markedAlternative (@Nonnull final List<ReleaseMediumDisk> inRmds,
                                                       @Nonnull final String embeddedTitle)
      {
        if (inRmds.size() <= 1)
          {
            return inRmds;
          }

        List<ReleaseMediumDisk> rmds = inRmds.stream()
                                             .map(rmd -> rmd.withEmbeddedTitle(embeddedTitle))
                                             .distinct()
                                             .collect(toList());
        rmds = discourageCollections ? markedAlternativeIfNotLeastCollection(rmds) : rmds;
        rmds = markedAlternativeByTitleAffinity(rmds);
        rmds = markedAlternativeByAsinOrBarcode(rmds);
        rmds = markedAlternativeButTheFirstNotAlternative(rmds);

        synchronized (log) // keep log lines together
          {
            log.info("MULTIPLE RESULTS");
            rmds.forEach(rmd -> log.info(">>> MULTIPLE RESULTS: {}", rmd));
          }

        final int count = countOfNotAlternative(rmds);
        assert count == 1 : "Still too many items not alternative: " + count;

        return rmds;
      }

    /*******************************************************************************************************************
     *
     * @param   rmds    the incoming {@code ReleaseMediumDisk}
     * @return          the processed {@code ReleaseMediumDisk}
     *
     ******************************************************************************************************************/
    @Nonnull
    private static List<ReleaseMediumDisk> markedAlternativeByAsinOrBarcode (@Nonnull List<ReleaseMediumDisk> rmds)
      {
        final boolean asinPresent = rmds.stream().anyMatch(rmd -> !rmd.isAlternative() && rmd.getAsin().isPresent());
        rmds = markedAlternative(rmds, rmd -> asinPresent && rmd.getAsin().isEmpty());

        final boolean barcodePresent =
                rmds.stream().anyMatch(rmd -> !rmd.isAlternative() && rmd.getBarcode().isPresent());
        rmds = markedAlternative(rmds, rmd -> barcodePresent && rmd.getBarcode().isEmpty());

        if (asinPresent && (countOfNotAlternative(rmds) > 1))
          {
            final Optional<String> asin = findFirstNotInAlternative(rmds, rmd -> rmd.getAsin());
            rmds = markedAlternative(rmds, rmd -> !rmd.getAsin().equals(asin));
          }

        if (barcodePresent && (countOfNotAlternative(rmds) > 1))
          {
            final Optional<String> barcode = findFirstNotInAlternative(rmds, rmd -> rmd.getBarcode());
            rmds = markedAlternative(rmds, rmd -> !rmd.getBarcode().equals(barcode));
          }

        return rmds;
      }

    /*******************************************************************************************************************
     *
     * Sweeps the given {@link ReleaseMediumDisk}s and marks as alternative all the items after a not alternative item.
     *
     * @param   rmds    the incoming {@code ReleaseMediumDisk}
     * @return          the processed {@code ReleaseMediumDisk}
     *
     ******************************************************************************************************************/
    @Nonnull
    private static List<ReleaseMediumDisk> markedAlternativeButTheFirstNotAlternative (@Nonnull final List<ReleaseMediumDisk> rmds)
      {
        if (countOfNotAlternative(rmds) <= 1)
          {
            return rmds;
          }

        final ReleaseMediumDisk pick = rmds.stream()
                                           .filter(rmd -> !rmd.isAlternative())
                                           .sorted(comparing(rmd -> rmd.getRelease().getId())) // Fix for BMT-166
                                           .findFirst()
                                           .get();
        return markedAlternative(rmds, rmd -> rmd != pick);
      }

    /*******************************************************************************************************************
     *
     * Sweeps the given {@link ReleaseMediumDisk}s and marks as alternative all the items which are not part of the
     * disk collections with the minimum size.
     *
     * @param   rmds    the incoming {@code ReleaseMediumDisk}s
     * @return          the processed {@code ReleaseMediumDisk}s
     *
     ******************************************************************************************************************/
    @Nonnull
    private static List<ReleaseMediumDisk> markedAlternativeIfNotLeastCollection (@Nonnull final List<ReleaseMediumDisk> rmds)
      {
        final int leastSize = rmds.stream().filter(rmd -> !rmd.isAlternative())
                                           .mapToInt(rmd -> rmd.getDiskCount().orElse(1))
                                           .min().getAsInt();
        return markedAlternative(rmds, rmd -> rmd.getDiskCount().orElse(1) > leastSize);
      }

    /*******************************************************************************************************************
     *
     * Sweeps the given {@link ReleaseMediumDisk}s and marks as alternative the items without the best score.
     *
     * @param   rmds    the incoming {@code ReleaseMediumDisk}
     * @return          the processed {@code ReleaseMediumDisk}
     *
     ******************************************************************************************************************/
    @Nonnull
    private static List<ReleaseMediumDisk> markedAlternativeByTitleAffinity (@Nonnull final List<ReleaseMediumDisk> rmds)
      {
        final int bestScore = rmds.stream().filter(rmd -> !rmd.isAlternative())
                                           .mapToInt(ReleaseMediumDisk::getScore)
                                           .max().getAsInt();
        return markedAlternative(rmds, rmd -> rmd.getScore() < bestScore);
      }

    /*******************************************************************************************************************
     *
     * Creates a copy of the collection where items have been marked alternative if the given predicate applies.
     *
     * @param rmds          the source
     * @param predicate     the predicate to decide whether an item must be marked as alternative
     * @return              the processed collection
     *
     ******************************************************************************************************************/
    @Nonnull
    private static List<ReleaseMediumDisk> markedAlternative (@Nonnull final List<ReleaseMediumDisk> rmds,
                                                              @Nonnull final Predicate<ReleaseMediumDisk> predicate)
      {
        return rmds.stream().map(rmd -> rmd.alternativeIf(predicate.test(rmd))).collect(toList());
      }

    /*******************************************************************************************************************
     *
     * Finds the first attribute specified by an extractor among items not already marked as alternatives.
     *
     * @param   rmds        the collection to search into
     * @param   extractor   the extractor
     * @return              the searched object
     *
     ******************************************************************************************************************/
    @Nonnull
    private static <T extends Comparable<?>> Optional<T> findFirstNotInAlternative (
            @Nonnull final List<ReleaseMediumDisk> rmds,
            @Nonnull final Function<ReleaseMediumDisk, Optional<T>> extractor)
      {
        return rmds.stream()
                   .filter(rmd -> !rmd.isAlternative())
                   .map(extractor)
                   .flatMap(Optional::stream)
                   .sorted()
                   .findFirst();
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnegative
    private static int countOfNotAlternative (@Nonnull final List<ReleaseMediumDisk> rmds)
      {
        return (int)rmds.stream().filter(rmd -> !rmd.isAlternative()).count();
      }

    /*******************************************************************************************************************
     *
     * Extracts data from the given release. For MusicBrainz, a Release is typically a disk, but it can be made of
     * multiple disks in case of many tracks.
     *
     * @param   metadata                the {@code Metadata}
     * @param   rmd                     the release
     * @return                          the RDF triples
     * @throws  InterruptedException    in case of I/O error
     * @throws  IOException             in case of I/O error
     *
     ******************************************************************************************************************/
    @Nonnull
    private ModelBuilder handleRelease (@Nonnull final Metadata metadata, @Nonnull final ReleaseMediumDisk rmd)
      throws IOException, InterruptedException
      {
        final Medium medium              = rmd.getMedium();
        final String releaseId           = rmd.getRelease().getId();
        final List<DefTrackData> tracks  = medium.getTrackList().getDefTrack();
        final String embeddedRecordTitle = metadata.get(ALBUM).get(); // .orElse(parent.getPath().toFile().getName());
        final Cddb cddb                  = metadata.get(CDDB).get();
        final String recordTitle         = rmd.pickTitle();
        final IRI embeddedRecordIri      = recordIriOf(metadata, embeddedRecordTitle);
        final IRI recordIri              = BMMO.recordIriFor(rmd.computeId());
        log.info("importing {} {} ...", recordTitle, (rmd.isAlternative() ? "(alternative)" : ""));

        ModelBuilder model = createModelBuilder()
            .with(recordIri, RDF.TYPE,              MO.C_RECORD)
            .with(recordIri, RDFS.LABEL,            literalFor(recordTitle))
            .with(recordIri, DC.TITLE,              literalFor(recordTitle))
            .with(recordIri, BMMO.P_IMPORTED_FROM,  BMMO.O_SOURCE_MUSICBRAINZ)
            .with(recordIri, BMMO.P_ALTERNATE_OF,   embeddedRecordIri)
            .with(recordIri, MO.P_MEDIA_TYPE,       MO.C_CD)
            .with(recordIri, MO.P_TRACK_COUNT,      literalFor(tracks.size()))
            .with(recordIri, MO.P_MUSICBRAINZ_GUID, literalFor(releaseId))
            .with(recordIri, MO.P_MUSICBRAINZ,      musicBrainzIriFor("release", releaseId))
            .with(recordIri, MO.P_AMAZON_ASIN,      literalFor(rmd.getAsin()))
            .with(recordIri, MO.P_GTIN,             literalFor(rmd.getBarcode()))

            .with(tracks.stream().parallel()
                                 .map(_f(track -> handleTrack(rmd, cddb, recordIri, track)))
                                 .collect(toList()));

        if (rmd.isAlternative())
          {
            model = model.with(recordIri, BMMO.P_ALTERNATE_PICK_OF, embeddedRecordIri);
          }

        return model;
        // TODO: release.getLabelInfoList();
        // TODO: record producer - requires inc=artist-rels
      }

    /*******************************************************************************************************************
     *
     * Extracts data from the given {@link DefTrackData}.
     *
     * @param   rmd                     the release
     * @param   cddb                    the CDDB of the track we're handling
     * @param   track                   the track
     * @return                          the RDF triples
     * @throws  InterruptedException    in case of I/O error
     * @throws  IOException             in case of I/O error
     *
     ******************************************************************************************************************/
    @Nonnull
    private ModelBuilder handleTrack (@Nonnull final ReleaseMediumDisk rmd,
                                      @Nonnull final Cddb cddb,
                                      @Nonnull final IRI recordIri,
                                      @Nonnull final DefTrackData track)
      throws IOException, InterruptedException
      {
        final IRI trackIri                 = trackIriOf(track.getId());
        final int trackNumber              = track.getPosition().intValue();
        final Optional<Integer> diskCount  = emptyIfOne(rmd.getDiskCount());
        final Optional<Integer> diskNumber = diskCount.flatMap(dc -> rmd.getDiskNumber());
        final String recordingId           = track.getRecording().getId();
//                final Recording recording = track.getRecording();
        final Recording recording = mbMetadataProvider.getResource(RECORDING, recordingId, RECORDING_INCLUDES).get();
        final String trackTitle   = recording.getTitle();
//                    track.getRecording().getAliasList().getAlias().get(0).getSortName();
        final IRI signalIri       = signalIriFor(cddb, track.getPosition().intValue());
        log.info(">>>>>>>> {}. {}", trackNumber, trackTitle);

        return createModelBuilder()
            .with(recordIri, MO.P_TRACK,            trackIri)
            .with(recordIri, BMMO.P_DISK_COUNT,     literalForInt(diskCount))
            .with(recordIri, BMMO.P_DISK_NUMBER,    literalForInt(diskNumber))

            .with(signalIri, MO.P_PUBLISHED_AS,     trackIri)

            .with(trackIri,  RDF.TYPE,              MO.C_TRACK)
            .with(trackIri,  RDFS.LABEL,            literalFor(trackTitle))
            .with(trackIri,  DC.TITLE,              literalFor(trackTitle))
            .with(trackIri,  BMMO.P_IMPORTED_FROM,  BMMO.O_SOURCE_MUSICBRAINZ)
            .with(trackIri,  MO.P_TRACK_NUMBER,     literalFor(trackNumber))
            .with(trackIri,  MO.P_MUSICBRAINZ_GUID, literalFor(track.getId()))
            .with(trackIri,  MO.P_MUSICBRAINZ,      musicBrainzIriFor("track", track.getId()))

            .with(handleTrackRelations(signalIri, trackIri, recordIri, recording));
      }

    /*******************************************************************************************************************
     *
     * Extracts data from the relations of the given {@link Recording}.
     *
     * @param   signalIri   the IRI of the signal associated to the track we're handling
     * @param   recording   the {@code Recording}
     * @return              the RDF triples
    *
     ******************************************************************************************************************/
    @Nonnull
    private ModelBuilder handleTrackRelations (@Nonnull final IRI signalIri,
                                               @Nonnull final IRI trackIri,
                                               @Nonnull final IRI recordIri,
                                               @Nonnull final Recording recording)
      {
        return createModelBuilder().with(recording.getRelationList()
                                                  .stream()
                                                  .parallel()
                                                  .flatMap(RelationAndTargetType::toStream)
                                                  .map(ratt -> handleTrackRelation(signalIri, trackIri, recordIri, recording, ratt))
                                                  .collect(toList()));
      }

    /*******************************************************************************************************************
     *
     * Extracts data from a relation of the given {@link Recording}.
     *
     * @param   signalIri   the IRI of the signal associated to the track we're handling
     * @param   recording   the {@code Recording}
     * @param   ratt        the relation
     * @return              the RDF triples
     *
     ******************************************************************************************************************/
    @Nonnull
    private ModelBuilder handleTrackRelation (@Nonnull final IRI signalIri,
                                              @Nonnull final IRI trackIri,
                                              @Nonnull final IRI recordIri,
                                              @Nonnull final Recording recording,
                                              @Nonnull final RelationAndTargetType ratt)
      {
        final Relation relation = ratt.getRelation();
        final String targetType = ratt.getTargetType();
        final List<Attribute> attributes = getAttributes(relation);
//        final Target target = relation.getTarget();
        final String type   = relation.getType();
        final Artist artist = relation.getArtist();

        log.info(">>>>>>>>>>>> {} {} {} {} ({})", targetType,
                                                  type,
                                                  attributes.stream().map(a -> toString(a)).collect(toList()),
                                                  artist.getName(),
                                                  artist.getId());

        final IRI performanceIri = performanceIriFor(recording.getId());
        final IRI artistIri      = artistIriOf(artist.getId());

        final ModelBuilder model = createModelBuilder()
            .with(performanceIri,  RDF.TYPE,              MO.C_PERFORMANCE)
            .with(performanceIri,  BMMO.P_IMPORTED_FROM,  BMMO.O_SOURCE_MUSICBRAINZ)
            .with(performanceIri,  MO.P_MUSICBRAINZ_GUID, literalFor(recording.getId()))
            .with(performanceIri,  MO.P_RECORDED_AS,      signalIri)

            .with(artistIri,       RDF.TYPE,              MO.C_MUSIC_ARTIST)
            .with(artistIri,       RDFS.LABEL,            literalFor(artist.getName()))
            .with(artistIri,       FOAF.NAME,             literalFor(artist.getName()))
            .with(artistIri,       BMMO.P_IMPORTED_FROM,  BMMO.O_SOURCE_MUSICBRAINZ)
            .with(artistIri,       MO.P_MUSICBRAINZ_GUID, literalFor(artist.getId()))
            .with(artistIri,       MO.P_MUSICBRAINZ,      musicBrainzIriFor("artist", artist.getId()))

            // TODO these could be inferred - performance shortcuts. Catalog queries rely upon these.
            .with(recordIri,       FOAF.MAKER,            artistIri)
            .with(trackIri,        FOAF.MAKER,            artistIri)
            .with(performanceIri,  FOAF.MAKER,            artistIri);
//            .with(signalIri,       FOAF.MAKER,          artistIri);

        if ("artist".equals(targetType))
          {
            predicatesForArtists(type, attributes)
                    .forEach(predicate -> model.with(performanceIri, predicate, artistIri));
          }

        return model;
//                        relation.getBegin();
//                        relation.getEnd();
//                        relation.getEnded();
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private static List<IRI> predicatesForArtists (@Nonnull final String type, @Nonnull final List<Attribute> attributes)
      {
        if (attributes.isEmpty())
          {
            return singletonList(predicateFor(type));
          }
        else
          {
            return attributes.stream().map(attribute ->
              {
                String role = type;

                if (type.equals("vocal") || type.equals("instrument"))
                  {
                    role += "/" + attribute.getContent();
                  }

                return predicateFor(role);
              }).collect(toList());
          }
      }

    /*******************************************************************************************************************
     *
     * Given a list of {@link ReleaseGroup}s, navigates into it and extract all CD {@link Medium}s that match the
     * given CDDB track offsets.
     *
     * @param   releaseGroups   the {@code ReleaseGroup}s
     * @param   cddb            the track offsets
     * @param   validation      how the results must be validated
     * @return                  a collection of filtered {@code Medium}s
     *
     ******************************************************************************************************************/
    @Nonnull
    private Collection<ReleaseMediumDisk> findReleases (@Nonnull final List<ReleaseGroup> releaseGroups,
                                                        @Nonnull final Cddb cddb,
                                                        @Nonnull final Validation validation)
      {
        return releaseGroups.stream()
                            .parallel()
                            .filter(releaseGroup -> scoreOf(releaseGroup) >= releaseGroupScoreThreshold)
                            .peek(this::logArtists)
                            .map(ReleaseGroup::getReleaseList)
                            .flatMap(releaseList -> findReleases(releaseList, cddb, validation).stream())
                            .collect(toList());
      }

    /*******************************************************************************************************************
     *
     * Given a {@link ReleaseList}, navigates into it and extract all CD {@link Medium}s that match the given CDDB track
     * offsets.
     *
     * @param   releaseList     the {@code ReleaseList}
     * @param   cddb            the track offsets to match
     * @param   validation      how the results must be validated
     * @return                  a collection of filtered {@code Medium}s
     *
     ******************************************************************************************************************/
    @Nonnull
    private Collection<ReleaseMediumDisk> findReleases (@Nonnull final ReleaseList releaseList,
                                                        @Nonnull final Cddb cddb,
                                                        @Nonnull final Validation validation)
      {
        return releaseList.getRelease().stream()
            .parallel()
//            .peek(this::logArtists)
            .peek(release -> log.info(">>>>>>>> release: {} {}", release.getId(), release.getTitle()))
            .flatMap(_f(release -> mbMetadataProvider.getResource(RELEASE, release.getId(), RELEASE_INCLUDES).get()
                            .getMediumList().getMedium()
                            .stream()
                            .map(medium -> new ReleaseMediumDisk(release, medium))))
            .filter(MusicBrainzAudioMedatataImporter::matchesFormat)
            .flatMap(rmd -> rmd.getMedium().getDiscList().getDisc().stream().map(rmd::withDisc))
            .filter(rmd -> matchesTrackOffsets(rmd, cddb, validation))
            .peek(rmd -> log.info(">>>>>>>> FOUND {} - with score {}", rmd.getMediumAndDiscString(), 0 /* scoreOf(releaseGroup) FIXME */))
            .collect(toMap(rmd -> rmd.getRelease().getId(), rmd -> rmd, (u, v) -> v, TreeMap::new))
            .values();
      }

    /*******************************************************************************************************************
     *
     *
     *
     *
     ******************************************************************************************************************/
    public static int similarity (@Nonnull final String a, @Nonnull final String b)
      {
        int score = StringUtils.getFuzzyDistance(a.toLowerCase(), b.toLowerCase(), Locale.UK);
        //
        // While this is a hack, it isn't so ugly as it might appear. The idea is to give a lower score to
        // collections and records with a generic title, hoping that a better one is picked.
        // FIXME: put into a map and then into an external resource with the delta score associated.
        // FIXME: with the filtering on collection size, this might be useless?
        //
        if (a.matches("^Great Violin Concertos.*")
         || a.matches("^CBS Great Performances.*"))
          {
            score -= 50;
          }

        if (a.matches("^Piano Concertos$")
         || a.matches("^Klavierkonzerte$"))
          {
            score -= 30;
          }

        return score;
      }

    /*******************************************************************************************************************
     *
     * Returns {@code true} if the given {@link ReleaseMediumDisk} is of a meaningful type (that is, a CD) or it's not set.
     *
     * @param   rmd     the {@code ReleaseMediumDisk}
     * @return          {@code true} if there is a match
     *
     ******************************************************************************************************************/
    private static boolean matchesFormat (@Nonnull final ReleaseMediumDisk rmd)
      {
        final String format = rmd.getMedium().getFormat();

        if ((format != null) && !"CD".equals(format))
          {
            log.info(">>>>>>>> discarded {} because not a CD ({})", rmd.getMediumAndDiscString(), format);
            return false;
          }

        return true;
      }

    /*******************************************************************************************************************
     *
     * Returns {@code true} if the given {@link ReleaseMediumDisk} matches the track offsets in the given {@link Cddb}.
     *
     * @param   rmd             the {@code ReleaseMediumDisk}
     * @param   requestedCddb   the track offsets to match
     * @param   validation      how the results must be validated
     * @return                  {@code true} if there is a match
     *
     ******************************************************************************************************************/
    private boolean matchesTrackOffsets (@Nonnull final ReleaseMediumDisk rmd,
                                         @Nonnull final Cddb requestedCddb,
                                         @Nonnull final Validation validation)
      {
        final Cddb cddb = rmd.getCddb();

        if ((cddb == null) && (validation == Validation.TRACK_OFFSETS_MATCH_NOT_REQUIRED))
          {
            log.info(">>>>>>>> no track offsets, but not required");
            return true;
          }

        final boolean matches = requestedCddb.matches(cddb, trackOffsetsMatchThreshold);

        if (!matches)
          {
            synchronized (log) // keep log lines together
              {
                log.info(">>>>>>>> discarded {} because track offsets don't match", rmd.getMediumAndDiscString());
                log.debug(">>>>>>>> iTunes offsets: {}", requestedCddb.getTrackFrameOffsets());
                log.debug(">>>>>>>> found offsets:  {}", cddb.getTrackFrameOffsets());
              }
          }

        return matches;
      }

    /*******************************************************************************************************************
     *
     * Searches for an alternate title of a record by querying the embedded title against the CDDB. The CDDB track
     * offsets are checked to validate the result.
     *
     * @param   metadata    the {@code Metadata}
     * @return              the title, if found
     *
     ******************************************************************************************************************/
    @Nonnull
    private Optional<String> cddbAlternateTitleOf (@Nonnull final Metadata metadata)
      throws IOException, InterruptedException
      {
        final RestResponse<CddbAlbum> optionalAlbum = cddbMetadataProvider.findCddbAlbum(metadata);

        if (!optionalAlbum.isPresent())
          {
            return Optional.empty();
          }

        final CddbAlbum album = optionalAlbum.get();
        final Cddb albumCddb = album.getCddb();
        final Cddb requestedCddb = metadata.get(ITUNES_COMMENT).get().getCddb();
        final Optional<String> dTitle = album.getProperty("DTITLE");

        if (!albumCddb.matches(requestedCddb, trackOffsetsMatchThreshold))
          {
            synchronized (log) // keep log lines together
              {
                log.info(">>>> discarded alternate title because of mismatching track offsets: {}", dTitle);
                log.debug(">>>>>>>> found track offsets:    {}", albumCddb.getTrackFrameOffsets());
                log.debug(">>>>>>>> searched track offsets: {}", requestedCddb.getTrackFrameOffsets());
                log.debug(">>>>>>>> ppm                     {}", albumCddb.computeDifference(requestedCddb));
              }

            return Optional.empty();
          }

        return dTitle;
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private static List<Attribute> getAttributes (@Nonnull final Relation relation)
      {
        final List<Attribute> attributes = new ArrayList<>();

        if (relation.getAttributeList() != null)
          {
            attributes.addAll(relation.getAttributeList().getAttribute());
          }

        return attributes;
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private static ModelBuilder createModelBuilder()
      {
        return new ModelBuilder(SOURCE_MUSICBRAINZ);
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private static IRI artistIriOf (@Nonnull final String id)
      {
        return BMMO.artistIriFor(createSha1IdNew(musicBrainzIriFor("artist", id).stringValue()));
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private static IRI trackIriOf (@Nonnull final String id)
      {
        return BMMO.trackIriFor(createSha1IdNew(musicBrainzIriFor("track", id).stringValue()));
      }

    /*******************************************************************************************************************
     *
     * FIXME: DUPLICATED FROM EmbbededAudioMetadataImporter
     *
     ******************************************************************************************************************/
    @Nonnull
    private static IRI recordIriOf (@Nonnull final Metadata metadata, @Nonnull final String recordTitle)
      {
        final Optional<Cddb> cddb = metadata.get(CDDB);
        return BMMO.recordIriFor(cddb.map(value -> createSha1IdNew(value.getToc()))
                                     .orElseGet(() -> createSha1IdNew("RECORD:" + recordTitle)));
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private IRI signalIriFor (@Nonnull final Cddb cddb, @Nonnegative final int trackNumber)
      {
        return BMMO.signalIriFor(createSha1IdNew(cddb.getToc() + "/" + trackNumber));
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private static IRI performanceIriFor (@Nonnull final String id)
      {
        return BMMO.performanceIriFor(createSha1IdNew(musicBrainzIriFor("performance", id).stringValue()));
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private static IRI musicBrainzIriFor (@Nonnull final String resourceType, @Nonnull final String id)
      {
        return FACTORY.createIRI(String.format("http://musicbrainz.org/%s/%s", resourceType, id));
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private static IRI predicateFor (@Nonnull final String role)
      {
        return Objects.requireNonNull(PERFORMER_MAP.get(role.toLowerCase()), "Cannot map role: " + role);
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    private static int scoreOf (@Nonnull final ReleaseGroup releaseGroup)
      {
        return Integer.parseInt(releaseGroup.getOtherAttributes().get(QNAME_SCORE));
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    private void logArtists (@Nonnull final ReleaseGroup releaseGroup)
      {
        log.debug(">>>> {} {} {} artist: {}",
                  releaseGroup.getOtherAttributes().get(QNAME_SCORE),
                  releaseGroup.getId(),
                  releaseGroup.getTitle(),
                  releaseGroup.getArtistCredit().getNameCredit().stream().map(nc -> nc.getArtist().getName()).collect(toList()));
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private static Optional<Integer> emptyIfOne (@Nonnull final Optional<Integer> number)
      {
        return number.flatMap(n -> (n == 1) ? Optional.empty() : Optional.of(n));
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private static String toString (@Nonnull final Attribute attribute)
      {
        return String.format("%s %s (%s)", attribute.getContent(), attribute.getCreditedAs(), attribute.getValue());
      }
  }
