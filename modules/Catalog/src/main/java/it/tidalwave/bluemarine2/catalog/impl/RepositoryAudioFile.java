/*
 * #%L
 * *********************************************************************************************************************
 *
 * blueMarine2 - Semantic Media Center
 * http://bluemarine2.tidalwave.it - hg clone https://bitbucket.org/tidalwave/bluemarine2-src
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
package it.tidalwave.bluemarine2.catalog.impl;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.nio.file.Path;
import org.openrdf.repository.Repository;
import it.tidalwave.util.Finder8;
import it.tidalwave.util.Finder8Support;
import it.tidalwave.util.Id;
import it.tidalwave.bluemarine2.model.Entity;
import it.tidalwave.bluemarine2.model.AudioFile;
import it.tidalwave.bluemarine2.model.impl.AudioMetadata;
import it.tidalwave.bluemarine2.model.impl.NamedEntity;
import it.tidalwave.bluemarine2.catalog.impl.finder.RepositoryMusicArtistFinder;
import it.tidalwave.bluemarine2.catalog.impl.finder.RepositoryRecordFinder;
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
@Immutable
public class RepositoryAudioFile extends RepositoryEntitySupport implements AudioFile
  {
    @Getter @Nonnull
    private final Path path;
    
    @Getter @Nonnull
    private final Path relativePath;
    
    @Getter @CheckForNull
    private final Entity parent;
    
    @CheckForNull
    private Metadata metadata;
    
    @Nonnull
    private final Id trackId;
    
    @Nonnull
    private final Duration duration;
    
    public RepositoryAudioFile (final @Nonnull Repository repository,
                                final @Nonnull Id id,
                                final @Nonnull Id trackId,
                                final @Nonnull Path path,
                                final @Nonnull Entity parent,
                                final @Nonnull Path basePath,
                                final @Nonnull Duration duration,
                                final String rdfsLabel)
      {
        super(repository, id, rdfsLabel);
        this.trackId = trackId;
        this.path = path;
        this.parent = parent;
        this.relativePath = path; // basePath.relativize(path);
        this.duration = duration;
      }
    
    @Override @Nonnull
    public String toString() 
      {
        return String.format("RepositoryAudioFileEntity(%s, %s)", relativePath, id);
      }

    @Override @Nonnull
    public synchronized Metadata getMetadata() 
      {
        if (metadata == null)
          {
            metadata = new AudioMetadata(path);
          }
        
        return metadata;
      }

    @Override @Nonnull
    public Optional<String> getLabel() 
      {
        return Optional.of(rdfsLabel);
      }

    @Override @Nonnull
    public Optional<Duration> getDuration() 
      {
        return getMetadata().get(DURATION);
//        return Optional.of(duration);
      }

    @Override @Nonnull
    public AudioFile getAudioFile()
      {
        return this;
      }

    @Override @Nonnull
    public Finder8<? extends Entity> findMakers() 
      {
        return new RepositoryMusicArtistFinder(repository).makerOf(trackId);
      }
 
    @Override
    public Optional<? extends Entity> getRecord()
      {
        return new RepositoryRecordFinder(repository).recordOf(id).optionalFirstResult();
      }
    
    @Override @Nonnull
    public Finder8<? extends Entity> findComposers() 
      {
        // FIXME: query the repository
        return new Finder8Support<Entity, Finder8<Entity>>()
          {
            @Override
            protected List<? extends Entity> computeNeededResults() 
              {
                return getMetadata().get(Metadata.COMPOSER)
                                    .map(artistName -> Arrays.asList(new NamedEntity(artistName)))
                                    .orElse(Collections.emptyList());
              }
          };
      }
  }
