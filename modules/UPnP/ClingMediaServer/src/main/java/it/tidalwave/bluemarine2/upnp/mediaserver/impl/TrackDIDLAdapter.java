/*
 * #%L
 * *********************************************************************************************************************
 *
 * blueMarine2 - Semantic Media Center
 * http://bluemarine2.tidalwave.it - git clone https://tidalwave@bitbucket.org/tidalwave/bluemarine2-src.git
 * %%
 * Copyright (C) 2015 - 2016 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.bluemarine2.upnp.mediaserver.impl;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.item.MusicTrack;
import it.tidalwave.role.Displayable;
import it.tidalwave.dci.annotation.DciRole;
import it.tidalwave.bluemarine2.model.Track;
import lombok.RequiredArgsConstructor;
import static it.tidalwave.role.Identifiable.Identifiable;

/***********************************************************************************************************************
 *
 * The {@link DIDLAdapter} for {@link Track}.
 *
 * @stereotype Role
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor
@Immutable @DciRole(datumType = Track.class)
public class TrackDIDLAdapter implements DIDLAdapter
  {
    @Nonnull
    private final Track datum;

    @Override @Nonnull
    public DIDLObject toObject()
      {
        // parentID not set here
        final MusicTrack item = new MusicTrack();
        item.setId(datum.as(Identifiable).getId().stringValue());
        item.setTitle(datum.asOptional(Displayable.Displayable).map(d -> d.getDisplayName()).orElse("???"));
        item.setOriginalTrackNumber(datum.getTrackNumber());
//        datum.getDuration();
//        datum.getDiskNumber();
//        datum.getTrackNumber();
        item.setRestricted(false);
        return item;
      }
  }
