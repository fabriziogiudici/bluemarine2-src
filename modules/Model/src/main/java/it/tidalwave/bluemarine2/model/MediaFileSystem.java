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
package it.tidalwave.bluemarine2.model;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import it.tidalwave.bluemarine2.model.role.EntityBrowser;

/***********************************************************************************************************************
 *
 * Represents a filesystem (even a virtual one) that contains {@link MediaFolder}s and {@link MediaItem}s.
 * 
 * @stereotype  Datum
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public interface MediaFileSystem extends Entity, EntityBrowser
  {
    /*******************************************************************************************************************
     *
     * Returns the root {@link MediaFolder}.
     * 
     * @return  the path
     *
     ******************************************************************************************************************/
    @Override @Nonnull // FIXME: this declaration could be dropped if we don't need covariance on return type
    public MediaFolder getRoot();
    
    @Nonnull
    public Path getRootPath();
  }
