/*
 * #%L
 * *********************************************************************************************************************
 *
 * blueMarine2 - Semantic Media Center
 * http://bluemarine2.tidalwave.it - git clone https://bitbucket.org/tidalwave/bluemarine2-src.git
 * %%
 * Copyright (C) 2015 - 2017 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.bluemarine2.model.impl.catalog;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.time.Duration;
import java.util.Optional;
import java.nio.file.Path;
import org.eclipse.rdf4j.repository.Repository;
import it.tidalwave.util.Id;
import it.tidalwave.bluemarine2.model.AudioFile;
import it.tidalwave.bluemarine2.model.Record;
import it.tidalwave.bluemarine2.model.finder.MusicArtistFinder;
import it.tidalwave.bluemarine2.model.spi.MetadataSupport;
import it.tidalwave.bluemarine2.model.role.PathAwareEntity;
import it.tidalwave.bluemarine2.model.vocabulary.BM;
import it.tidalwave.bluemarine2.model.impl.catalog.finder.RepositoryMusicArtistFinder;
import it.tidalwave.bluemarine2.model.impl.catalog.finder.RepositoryRecordFinder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import static it.tidalwave.bluemarine2.model.MediaItem.Metadata.*;

/***********************************************************************************************************************
 *
 * An implementation of {@link AudioFile} that is mapped to a {@link Repository}.
 *
 * @stereotype  Datum
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Immutable @EqualsAndHashCode(of = { "path", "trackId" }, callSuper = false)
public class RepositoryAudioFile extends RepositoryEntitySupport implements AudioFile
  {
//    @RequiredArgsConstructor // FIXME: refactor with a Factory of children
//    public static class RepositoryAudioFileArtistFinder extends Finder8Support<MusicArtist, MusicArtistFinder> implements MusicArtistFinder
//      {
//        private static final long serialVersionUID = -4130836861989484793L;
//
//        @Nonnull
//        private final RepositoryAudioFile file;
//
//        /** Clone constructor. */
//        public RepositoryAudioFileArtistFinder (final @Nonnull RepositoryAudioFileArtistFinder other,
//                                          final @Nonnull Object override)
//          {
//            super(other, override);
//            this.file = other.file;
//          }
//
//        // FIXME: query the repository
//        @Override @Nonnull
//        protected List<MusicArtist> computeNeededResults()
//          {
//            return file.getMetadata().get(Metadata.COMPOSER)
//                                     .map(artistName -> Arrays.asList(new RepositoryMusicArtist(file.repository, artistName)))
//                                     .orElse(Collections.emptyList());
//          }
//      }

    @Getter @Nonnull
    private final Path path;

    @Getter @Nonnull
    private final Path relativePath;

    @Getter @Nonnull
    private final Metadata metadata;

    @Nonnull
    private final Id trackId;

    // FIXME: too maby arguments, pass a BindingSet
    public RepositoryAudioFile (final @Nonnull Repository repository,
                                final @Nonnull Id id,
                                final @Nonnull Id trackId,
                                final @Nonnull Path path,
                                final @Nonnull Path basePath,
                                final @Nonnull Optional<Duration> duration,
                                final String rdfsLabel,
                                final @Nonnull Optional<Long> fileSize)
      {
        super(repository, id, rdfsLabel, Optional.of(new Id(BM.V_EMBEDDED.stringValue())));
        this.trackId = trackId;
        // See BMT-36
        this.path = path;
        this.relativePath = path;// FIXME basePath.relativize(path);
        this.metadata = new MetadataSupport(path).with(TITLE, rdfsLabel)
                                                 .with(DURATION, duration)
                                                 .with(FILE_SIZE, fileSize);
      }

    @Override @Nonnull
    public String toString()
      {
        return String.format("RepositoryAudioFileEntity(%s, %s)", relativePath, id);
      }

    @Override @Nonnull
    public AudioFile getAudioFile()
      {
        return this;
      }

    @Override @Nonnull
    public MusicArtistFinder findMakers()
      {
        return new RepositoryMusicArtistFinder(repository).makerOf(trackId);
      }

    @Override @Nonnull
    public MusicArtistFinder findComposers()
      {
        return new RepositoryMusicArtistFinder(repository).makerOf(trackId);
//        return new RepositoryAudioFileArtistFinder(this); FIXME
      }

    @Override @Nonnull
    public Optional<Record> getRecord()
      {
        return new RepositoryRecordFinder(repository).recordOf(id).optionalFirstResult();
      }

    @Override @Nonnull
    public Optional<PathAwareEntity> getParent() // FIXME: drop it
      {
        throw new UnsupportedOperationException();
      }
  }
