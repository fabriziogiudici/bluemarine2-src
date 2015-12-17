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
package it.tidalwave.bluemarine2.model;

import javax.annotation.Nonnull;
import it.tidalwave.bluemarine2.model.finder.TrackFinder;
import it.tidalwave.bluemarine2.model.finder.RecordFinder;
import it.tidalwave.role.Identifiable;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public interface MusicArtist extends Entity, Identifiable
  {
    public static final Class<MusicArtist> MusicArtist = MusicArtist.class;
    
    /*******************************************************************************************************************
     *
     * Finds the tracks made by this artist.
     * 
     * @return  the tracks
     *
     ******************************************************************************************************************/
    @Nonnull
    public TrackFinder findTracks();
    
    /*******************************************************************************************************************
     *
     * Finds the records made by this artist.
     * 
     * @return  the records
     *
     ******************************************************************************************************************/
    @Nonnull
    public RecordFinder findRecords();
    
    public int getType(); // FIXME: use an enum
  }
