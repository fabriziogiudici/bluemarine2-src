/*
 * #%L
 * *********************************************************************************************************************
 *
 * blueMarine2 - Semantic Media Center
 * http://bluemarine2.tidalwave.it - git clone https://tidalwave@bitbucket.org/tidalwave/bluemarine2-src.git
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

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.DIDLContent;
import it.tidalwave.util.Finder;
import it.tidalwave.util.As8;
import it.tidalwave.role.SimpleComposite8;
import it.tidalwave.bluemarine2.model.role.Entity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.bluemarine2.upnp.mediaserver.impl.didl.DIDLAdapter.DIDLAdapter;

/***********************************************************************************************************************
 *
 * @stereotype Role
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor @Slf4j
@Immutable
public abstract class CompositeDIDLAdapterSupport<T extends As8> implements DIDLAdapter
  {
    @Nonnull
    protected final T datum;

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public ContentHolder toContent (final @Nonnull BrowseFlag browseFlag,
                                    final @Nonnegative int from,
                                    final @Nonnegative int maxResults)
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
                final Finder<Entity> finder = datum.as(SimpleComposite8.class).findChildren();
                totalMatches = finder.count();
                finder.from(from)
                      .max(maxResults)
                      .results()
                      .stream()
                      .forEach(child ->
                        {
                          try
                            {
                              content.addObject(asDIDLAdapter(child).toObject());
                            }
                          catch (Exception e)
                            {
                              throw new RuntimeException(e);
                            }
                        });
                numberReturned = (int)content.getCount();
                break;

            default:
                throw new IllegalArgumentException(browseFlag.toString());
          }

        return new ContentHolder(content, numberReturned, totalMatches);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    protected static DIDLAdapter asDIDLAdapter (final @Nonnull As8 object)
      {
        return object.asOptional(DIDLAdapter)
                .orElseGet(() ->
                  {
                    if (object instanceof Entity) // FIXME: must be fallback; annotations don't warrant this
                      {
                        return new EntityDIDLAdapter((Entity)object);
                      }
                    else
                      {
                        throw new RuntimeException("No DIDL adapter for " + object);
                      }
                  });
//                .orElseThrow(() -> new RuntimeException("No DIDL adapter for " + object));
      }
  }
