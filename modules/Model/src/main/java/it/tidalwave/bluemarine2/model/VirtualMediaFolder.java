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
package it.tidalwave.bluemarine2.model;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.nio.file.Path;
import it.tidalwave.util.Id;
import it.tidalwave.role.Identifiable;
import it.tidalwave.role.ui.Displayable;
import it.tidalwave.bluemarine2.model.spi.EntityWithRoles;
import it.tidalwave.bluemarine2.model.spi.PathAwareEntity;
import it.tidalwave.bluemarine2.model.spi.PathAwareFinder;
import lombok.Getter;
import lombok.ToString;
import static it.tidalwave.util.Parameters.r;

/***********************************************************************************************************************
 *
 * Represents a folder which doesn't have a physical counterpart in the repository. It can be used to created in-memory
 * aggregations of media items.
 *
 * @stereotype  Datum
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@ToString(exclude = "finderFactory")
public class VirtualMediaFolder extends EntityWithRoles implements MediaFolder
  {
    // These two interfaces are needed to avoid clashes with constructor overloading
    public static interface EntityCollectionFactory extends Function<MediaFolder, Collection<? extends PathAwareEntity>>
      {
      }

    public static interface EntityFinderFactory extends Function<MediaFolder, PathAwareFinder>
      {
      }

    @Getter @Nonnull
    private final Path path;

    @Nonnull
    private final Optional<? extends MediaFolder> optionalParent;

    @Nonnull
    private final EntityFinderFactory finderFactory;

    public VirtualMediaFolder (@Nonnull final MediaFolder parent,
                               @Nonnull final Path pathSegment,
                               @Nonnull final String displayName,
                               @Nonnull final EntityCollectionFactory childrenFactory)
      {
        this(Optional.of(parent), pathSegment, displayName, Optional.of(childrenFactory), Optional.empty());
      }

    public VirtualMediaFolder (@Nonnull final Optional<? extends MediaFolder> optionalParent,
                               @Nonnull final Path pathSegment,
                               @Nonnull final String displayName,
                               @Nonnull final EntityCollectionFactory childrenFactory)
      {
        this(optionalParent, pathSegment, displayName, Optional.of(childrenFactory), Optional.empty());
      }

    public VirtualMediaFolder (@Nonnull final MediaFolder parent,
                               @Nonnull final Path pathSegment,
                               @Nonnull final String displayName,
                               @Nonnull final EntityFinderFactory finderFactory)
      {
        this(Optional.of(parent), pathSegment, displayName, Optional.empty(), Optional.of(finderFactory));
      }

    public VirtualMediaFolder (@Nonnull final Optional<? extends MediaFolder> optionalParent,
                               @Nonnull final Path pathSegment,
                               @Nonnull final String displayName,
                               @Nonnull final EntityFinderFactory finderFactory)
      {
        this(optionalParent, pathSegment, displayName, Optional.empty(), Optional.of(finderFactory));
      }

    private VirtualMediaFolder (@Nonnull final Optional<? extends MediaFolder> optionalParent,
                                @Nonnull final Path pathSegment,
                                @Nonnull final String displayName,
                                @Nonnull final Optional<EntityCollectionFactory> childrenSupplier,
                                @Nonnull final Optional<EntityFinderFactory> finderFactory)
      {
        // FIXME: review if first should be prioritised
        super(r((Identifiable)() -> Id.of(absolutePath(optionalParent, pathSegment).toString()),
              Displayable.of(displayName)));
        this.path = absolutePath(optionalParent, pathSegment);
        this.optionalParent = optionalParent;
        this.finderFactory = finderFactory.orElse(mediaFolder -> mediaFolder.finderOf(childrenSupplier.get()));
      }

    @Override @Nonnull
    public Optional<PathAwareEntity> getParent()
      {
        return (Optional<PathAwareEntity>)(Object)optionalParent;
      }

    @Override @Nonnull
    public PathAwareFinder findChildren()
      {
        return finderFactory.apply(this);
      }

    @Override @Nonnull
    public String toDumpString()
      {
        return String.format("Folder(path=%s)", path);
      }

    @Nonnull
    private static Path absolutePath (@Nonnull final Optional<? extends MediaFolder> optionalParent,
                                      @Nonnull final Path pathSegment)
      {
        return optionalParent.map(parent -> parent.getPath().resolve(pathSegment)).orElse(pathSegment);
      }
  }
