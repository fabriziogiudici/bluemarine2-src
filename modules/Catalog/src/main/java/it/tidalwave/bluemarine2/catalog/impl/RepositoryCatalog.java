/*
 * #%L
 * *********************************************************************************************************************
 *
 * blueMarine2 - Semantic Media Center
 * http://bluemarine2.tidalwave.it - git clone https://tidalwave@bitbucket.org/tidalwave/bluemarine2-src.git
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

import javax.annotation.Nonnull;
import org.openrdf.repository.Repository;
import it.tidalwave.bluemarine2.model.finder.MusicArtistFinder;
import it.tidalwave.bluemarine2.model.finder.RecordFinder;
import it.tidalwave.bluemarine2.model.finder.TrackFinder;
import it.tidalwave.bluemarine2.catalog.Catalog;
import it.tidalwave.bluemarine2.catalog.impl.finder.RepositoryRecordFinder;
import it.tidalwave.bluemarine2.catalog.impl.finder.RepositoryMusicArtistFinder;
import it.tidalwave.bluemarine2.catalog.impl.finder.RepositoryTrackFinder;
import lombok.RequiredArgsConstructor;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor
public class RepositoryCatalog implements Catalog
  {
    @Nonnull
    private final Repository repository;
    
    @Override @Nonnull
    public MusicArtistFinder findArtists()
      { 
        return new RepositoryMusicArtistFinder(repository);
      }

    @Override @Nonnull
    public RecordFinder findRecords() 
      {
        return new RepositoryRecordFinder(repository);
      }

    @Override @Nonnull
    public TrackFinder findTracks() 
      {
        return new RepositoryTrackFinder(repository);
      }
  }
