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
package it.tidalwave.bluemarine2.model.spi;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.nio.file.Path;
import it.tidalwave.util.Id;
import it.tidalwave.role.Identifiable;
import it.tidalwave.role.spi.DefaultDisplayable;
import it.tidalwave.bluemarine2.model.Entity;
import it.tidalwave.bluemarine2.model.MediaFolder;
import it.tidalwave.bluemarine2.model.finder.EntityFinder;
import it.tidalwave.bluemarine2.model.impl.EntityWithRoles;
import lombok.Getter;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class VirtualMediaFolder extends EntityWithRoles implements MediaFolder
  {
    // These two interfaces are needed to avoid clashes with constructor overloading
    public static interface EntityCollectionFactory extends Function<MediaFolder, Collection<Entity>>
      {
      }

    public static interface EntityFinderFactory extends Function<MediaFolder, EntityFinder>
      {
      }

    @Getter @Nonnull
    private final Path path;

    @Getter @CheckForNull
    private MediaFolder parent;

    @Nonnull
    private final EntityFinderFactory finderFactory;

    public VirtualMediaFolder (final @Nullable MediaFolder parent,
                               final @Nonnull Path path,
                               final @Nonnull String displayName,
                               final @Nonnull EntityCollectionFactory childrenSupplier)
      {
        this(parent, path, displayName, Optional.of(childrenSupplier), Optional.empty());
      }

    public VirtualMediaFolder (final @Nullable MediaFolder parent,
                               final @Nonnull Path path,
                               final @Nonnull String displayName,
                               final @Nonnull EntityFinderFactory finderFactory)
      {
        this(parent, path, displayName, Optional.empty(), Optional.of(finderFactory));
      }

    private VirtualMediaFolder (final @Nullable MediaFolder parent,
                                final @Nonnull Path path,
                                final @Nonnull String displayName,
                                final @Nonnull Optional<EntityCollectionFactory> childrenSupplier,
                                final @Nonnull Optional<EntityFinderFactory> finderFactory)
      {
        super((Identifiable)() -> new Id(absolutePath(parent, path).toString()),
              new DefaultDisplayable(displayName));
        this.path = absolutePath(parent, path);
        this.parent = parent;
        this.finderFactory = finderFactory.orElse(
                mediaFolder -> new FactoryBasedEntityFinder(mediaFolder, childrenSupplier.get()));
      }

    @Override
    public boolean isRoot()
      {
        return parent == null;
      }

    @Override @Nonnull
    public EntityFinder findChildren()
      {
        return finderFactory.apply(this);
      }

    @Override @Nonnull
    public String toString()
      {
        return String.format("VirtualMediaFolder(%s)", path);
      }

    @Nonnull
    private static Path absolutePath (final @Nullable MediaFolder parent, final @Nonnull Path path)
      {
        return (parent == null) ? path : parent.getPath().resolve(path);
      }
  }
