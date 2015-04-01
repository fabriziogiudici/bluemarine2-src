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
package it.tidalwave.bluemarine2.model.impl;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.nio.file.Path;
import it.tidalwave.util.spi.AsSupport;
import it.tidalwave.bluemarine2.model.MediaFolder;
import it.tidalwave.bluemarine2.model.MediaItem;
import lombok.AllArgsConstructor;
import lombok.Delegate;
import lombok.Getter;

/***********************************************************************************************************************
 *
 * The default implementation of {@link MediaFolder}. It basically does nothing, it just acts as an aggregator of roles.
 * 
 * @stereotype  Datum
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Immutable @AllArgsConstructor
public class DefaultMediaItem implements MediaItem
  {
    @Getter @Nonnull
    private final Path path;
    
    @Getter @CheckForNull
    private MediaFolder parent;
    
    @CheckForNull
    private Metadata metadata;
    
    @Delegate
    private final AsSupport asSupport = new AsSupport(this);

    @Override @Nonnull
    public String toString() 
      {
        return String.format("DefaultMediaItem(%s)", path.toFile().getName());
      }

    @Override @Nonnull
    public synchronized Metadata getMetadata() 
      {
        if (metadata == null)
          {
            metadata = new AudioMetadata(path);
          }
        
        return metadata;
      }
  }