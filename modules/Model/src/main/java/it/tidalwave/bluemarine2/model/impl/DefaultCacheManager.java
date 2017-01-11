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
package it.tidalwave.bluemarine2.model.impl;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.lang.ref.WeakReference;
import it.tidalwave.messagebus.annotation.ListensTo;
import it.tidalwave.messagebus.annotation.SimpleMessageSubscriber;
import it.tidalwave.bluemarine2.model.spi.CacheManager;
import it.tidalwave.bluemarine2.util.PersistenceInitializedNotification;
import lombok.extern.slf4j.Slf4j;
import static java.util.Objects.requireNonNull;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j @SimpleMessageSubscriber
public class DefaultCacheManager implements CacheManager
  {
    private final Map<Object, WeakReference<? extends Object>> cache = new ConcurrentHashMap<>();

    @Override @Nonnull
    public synchronized <T> T getCachedObject (final @Nonnull Object key, final @Nonnull Supplier<T> supplier)
      {
        WeakReference<T> ref = (WeakReference<T>)(cache.computeIfAbsent(key, k -> computeObject(k, supplier)));
        T object = ref.get();

        if (object == null)
          {
            ref = computeObject(key, supplier);
            object = ref.get();
            cache.put(key, ref);
          }

        return object;
      }

    @Nonnull
    private static <T> WeakReference<T> computeObject (final @Nonnull Object key, final @Nonnull Supplier<T> supplier)
      {
        log.debug(">>>> cache miss for {}", key);
        return new WeakReference<>(requireNonNull(supplier.get(), "Supplier returned null"));
      }

    private void onPersistenceUpdated (final @ListensTo PersistenceInitializedNotification notification)
      {
        log.debug("onPersistenceUpdated({})", notification);
        cache.clear();
      }
  }
