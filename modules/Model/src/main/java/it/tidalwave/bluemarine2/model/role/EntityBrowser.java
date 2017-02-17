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
package it.tidalwave.bluemarine2.model.role;

import javax.annotation.Nonnull;
import it.tidalwave.util.As8;
import it.tidalwave.bluemarine2.model.spi.Entity;

/***********************************************************************************************************************
 *
 * A role which can supply an {@link Entity} that is the root of a hierarchy.
 *
 * @stereotype  Role
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public interface EntityBrowser extends As8
  {
    /*******************************************************************************************************************
     *
     * Returns the root {@link Entity}.
     *
     * @return  the root {@code Entity}
     *
     ******************************************************************************************************************/
    @Nonnull
    public Entity getRoot();
  }
