/*
 * #%L
 * *********************************************************************************************************************
 *
 * blueMarine2 - lightweight MediaCenter
 * http://bluemarine.tidalwave.it - hg clone https://bitbucket.org/tidalwave/bluemarine2-src
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
package it.tidalwave.bluemarine2.catalog.impl.browser;

import javax.annotation.Nonnull;
import it.tidalwave.util.Finder8;
import it.tidalwave.role.Displayable;
import it.tidalwave.role.SimpleComposite8;
import it.tidalwave.bluemarine2.model.Record;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class RepositoryBrowserByRecordThenTrack extends RepositoryBrowserSupport implements Displayable
  {
    public RepositoryBrowserByRecordThenTrack()
      {
//        super(() -> getCatalog().findRecords());
        setCompositeForRootEntity(new SimpleComposite8<Record>() 
          {
            @Override @Nonnull
            public Finder8<Record> findChildren() 
              {
                return getCatalog().findRecords();
              }
          });
      }

    @Override @Nonnull
    public String getDisplayName() 
      {
        return "by Record, then track"; // FIXME: use a Bundle
      }  
  }