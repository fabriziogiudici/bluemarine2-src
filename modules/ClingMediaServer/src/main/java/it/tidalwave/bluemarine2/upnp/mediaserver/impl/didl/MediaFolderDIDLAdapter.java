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
package it.tidalwave.bluemarine2.upnp.mediaserver.impl.didl;

import javax.annotation.concurrent.Immutable;
import javax.annotation.Nonnull;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.container.StorageFolder;
import it.tidalwave.dci.annotation.DciRole;
import it.tidalwave.bluemarine2.model.MediaFolder;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.bluemarine2.upnp.mediaserver.impl.UpnpUtilities.*;

/***********************************************************************************************************************
 *
 * An implementation of {@link DIDLAdapter} for {@link MediaFolder}.
 *
 * @see http://upnp.org/specs/av/UPnP-av-ContentDirectory-v1-Service.pdf
 *
 * @stereotype  Role, Adapter
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
@Immutable @DciRole(datumType = MediaFolder.class)
public class MediaFolderDIDLAdapter extends CompositeDIDLAdapterSupport<MediaFolder>
  {
    public MediaFolderDIDLAdapter (final @Nonnull MediaFolder datum)
      {
        super(datum);
      }

    @Override @Nonnull
    public DIDLObject toObject()
      {
        log.debug("toObject() - {}", datum);
        final Container container = setCommonFields(new Container());
        container.setClazz(StorageFolder.CLASS);
        container.setId(pathToDidlId(datum.getPath()));
        container.setParentID(datum.getParent().map(parent -> pathToDidlId(parent.getPath())).orElse(ID_NONE));

        if (!container.getId().equals("/music")) // FIXME workaround for DefaultMediaFileSystem
          {
            container.setTitle(container.getTitle().replace("Music", "By file"));
          }

        return container;
      }
  }
