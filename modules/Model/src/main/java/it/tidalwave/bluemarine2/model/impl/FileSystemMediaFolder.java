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
package it.tidalwave.bluemarine2.model.impl;

import java.util.Optional;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.nio.file.Path;
import it.tidalwave.util.spi.AsSupport;
import it.tidalwave.bluemarine2.model.EntityWithPath;
import it.tidalwave.bluemarine2.model.MediaFolder;
import it.tidalwave.bluemarine2.model.finder.EntityFinder;
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
public class FileSystemMediaFolder implements MediaFolder
  {
    @Getter @Nonnull
    private final Path path;

    @CheckForNull
    private MediaFolder parent;

    @Getter @Nonnull
    private final Path basePath;

    @Delegate
    private final AsSupport asSupport = new AsSupport(this);

    @Override @Nonnull
    public Optional<EntityWithPath> getParent()
      {
        return Optional.ofNullable(parent);
      }

    @Override @Nonnull
    public EntityFinder findChildren()
      {
        return new MediaFolderFinder(this, basePath).sort(new MediaItemComparator());
      }

    @Override @Nonnull
    public String toString()
      {
        return String.format("FileSystemMediaFolder(%s)", basePath.relativize(path));
      }
  }
