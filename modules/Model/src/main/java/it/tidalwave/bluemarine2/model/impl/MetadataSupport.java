/*
 * #%L
 * *********************************************************************************************************************
 *
 * blueMarine2 - Semantic Media Center
 * http://bluemarine2.tidalwave.it - git clone https://tidalwave@bitbucket.org/tidalwave/bluemarine2-src.git
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.nio.file.Path;
import it.tidalwave.util.Key;
import it.tidalwave.bluemarine2.model.MediaItem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor @ToString(of = "properties") @Slf4j
public class MetadataSupport implements MediaItem.Metadata
  {
    @Getter @Nonnull
    protected final Path path;
    
    protected final Map<Key<?>, Object> properties = new HashMap<>();
    
    /*******************************************************************************************************************
     *
     * {@inheritDocs}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public <T> Optional<T> get (final @Nonnull Key<T> key)
      {
        return Optional.ofNullable(((T)properties.get(key)));
      }

    /*******************************************************************************************************************
     *
     * {@inheritDocs}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public <T> T getAll (final @Nonnull Key<T> key)
      {
        final T list = (T)properties.get(key);
        return (list != null) ? list : (T)Collections.emptyList();
      }

    /*******************************************************************************************************************
     *
     * {@inheritDocs}
     *
     ******************************************************************************************************************/
    @Override
    public boolean containsKey (final @Nonnull Key<?> key)
      {
        return properties.containsKey(key);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDocs}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Set<Key<?>> getKeys()
      {
        return Collections.unmodifiableSet(properties.keySet());
      }
    
    /*******************************************************************************************************************
     *
     * {@inheritDocs}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Set<Map.Entry<Key<?>, ?>> getEntries() 
      {
        return Collections.unmodifiableSet(properties.entrySet());
      }
    
    /*******************************************************************************************************************
     *
     * 
     * 
     ******************************************************************************************************************/
    protected <V> void put (final @Nonnull Key<V> key, final @Nullable V value)
      {
        if (value != null)
          {
            properties.put(key, value);
          }
      }

  }