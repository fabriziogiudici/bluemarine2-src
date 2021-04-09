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

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.DIDLContent;
import it.tidalwave.util.As;
import it.tidalwave.util.Finder;
import it.tidalwave.bluemarine2.model.spi.Entity;
import it.tidalwave.bluemarine2.rest.spi.ResourceServer;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.role.SimpleComposite._SimpleComposite_;
import static it.tidalwave.util.FunctionalCheckedExceptionWrappers.*;

/***********************************************************************************************************************
 *
 * @stereotype Role
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Immutable @Slf4j
public abstract class CompositeDIDLAdapterSupport<T extends As> extends DIDLAdapterSupport<T>
  {
    public CompositeDIDLAdapterSupport (@Nonnull final T datum, @Nonnull final ResourceServer server)
      {
        super(datum, server);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public ContentHolder toContent (@Nonnull final BrowseFlag browseFlag,
                                    @Nonnegative final int from,
                                    @Nonnegative final int maxResults)
      throws Exception
      {
        final DIDLContent content = new DIDLContent();
        int numberReturned = 0;
        int totalMatches = 0;

        switch (browseFlag)
          {
            case METADATA:
                totalMatches = numberReturned = 1;
                content.addObject(toObject());
                break;

            case DIRECT_CHILDREN:
                final Finder<Entity> finder = datum.as(_SimpleComposite_).findChildren();
                totalMatches = finder.count();
                finder.from(from)
                      .max(maxResults)
                      .results()
                      .forEach(_c(child -> content.addObject(child.as(_DIDLAdapter_).toObject())));
                numberReturned = (int)content.getCount();
                break;

            default:
                throw new IllegalArgumentException(browseFlag.toString());
          }

        return new ContentHolder(content, numberReturned, totalMatches);
      }
  }
