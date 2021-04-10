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
package it.tidalwave.bluemarine2.upnp.mediaserver.impl.didl;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.item.Item;
import it.tidalwave.bluemarine2.model.impl.PathAwareEntityDecorator;
import it.tidalwave.bluemarine2.rest.spi.ResourceServer;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.bluemarine2.upnp.mediaserver.impl.UpnpUtilities.externalized;

/***********************************************************************************************************************
 *
 * A support for roles dealing with {@link PathAwareEntityDecorator} that creates the {@link DIDLObject} in function
 * of the delegate; and properly sets the {@code id} and {@code parentId} in function of the path, overriding any
 * specific setting by the original datum adapter.
 *
 * @stereotype Role
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Slf4j @Immutable
public class PathAwareDecoratorDIDLAdapter extends CompositeDIDLAdapterSupport<PathAwareEntityDecorator>
  {
    public PathAwareDecoratorDIDLAdapter (@Nonnull final PathAwareEntityDecorator datum,
                                          @Nonnull final ResourceServer server)
      {
        super(datum, server);
      }

    @Override @Nonnull
    public final DIDLObject toObject()
      throws Exception
      {
        log.debug("toObject() - {}", datum);
        final DIDLObject item = datum.getDelegate().as(_DIDLAdapter_).toObject();

        if (item instanceof Item)
          {
            ((Item)item).setRefID(item.getId()); // don't externalize this
          }

        item.setId(externalized(datum.getPath().toString()));
        datum.getParent().ifPresent(parent -> item.setParentID(externalized(parent.getPath().toString())));
        return item;
      }
  }
