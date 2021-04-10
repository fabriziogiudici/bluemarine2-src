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
package it.tidalwave.bluemarine2.rest.impl.resource;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.springframework.beans.factory.annotation.Configurable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import it.tidalwave.role.Identifiable;
import it.tidalwave.bluemarine2.rest.spi.ResourceServer;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Configurable(preConstruction = true)
public abstract class ResourceSupport
  {
    @Inject @JsonIgnore
    private ResourceServer server;

    @Nonnull
    protected final String resourceUri (@Nonnull final String resourceType, @Nonnull final Identifiable resource)
      {
        return resourceUri(resourceType, resource.getId().stringValue());
      }

    @Nonnull
    protected final String resourceUri (@Nonnull final String resourceType, @Nonnull final String resourceId)
      {
        return server.absoluteUrl(String.format("rest/%s/%s", resourceType, resourceId));
      }
  }
