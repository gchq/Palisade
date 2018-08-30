/*
 * Copyright 2018 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.gchq.palisade.cache.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * A HashMapBackingStore is a simple implementation of a {@link BackingStore} that simply caches the objects in a
 * ConcurrentHashMap. By default the cache is static so it will be shared across the same JVM.
 */
public class HashMapBackingStore implements BackingStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(HashMapBackingStore.class);

    private static final ConcurrentHashMap<String, CachedPair> CACHE = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CachedPair> cache;

    private static class CachedPair {
        public final byte[] value;
        public final Class<?> clazz;

        CachedPair(final byte[] value, final Class<?> clazz) {
            this.value = value;
            this.clazz = clazz;
        }
    }

    /**
     * Timer thread to remove cache entries after expiry.
     */
    private static final ScheduledExecutorService REMOVAL_TIMER = Executors.newSingleThreadScheduledExecutor();

    public HashMapBackingStore() {
        this(true);
    }

    public HashMapBackingStore(final boolean useStatic) {
        if (useStatic) {
            cache = CACHE;
        } else {
            cache = new ConcurrentHashMap<>();
        }
    }

    @Override
    public boolean store(final String key, final Class<?> valueClass, final byte[] value, final Optional<Duration> timeToLive) {
        LOGGER.debug("Adding to cache: {}", new String(value));
        cache.put(key, new CachedPair(value, valueClass));
        /*Here we set up a simple timer to deal with the removal of the item from the cache if a duration is present
         *This uses a single timer to remove elements, this is fine for this example, but in production we would want
         *something more performant.
         */
        timeToLive.ifPresent(duration -> {
            REMOVAL_TIMER.schedule(() -> cache.remove(key), duration.toMillis(), TimeUnit.MILLISECONDS);
        });
        return true;
    }

    @Override
    public BasicCacheObject retrieve(final String key) {
        LOGGER.debug("Getting from cache: {}", key);
        final CachedPair result = cache.getOrDefault(key, new CachedPair(null, null));

        LOGGER.debug("Got from cache: {}", result.value);
        return new BasicCacheObject(result.clazz, Optional.ofNullable(result.value));
    }

    @Override
    public Collection<String> list(final String prefix) {
        return cache.keySet()
                .stream()
                .filter(x -> x.startsWith(prefix))
                .collect(Collectors.toSet());
    }

    @Override
    public boolean supportsTimeToLive() {
        return true;
    }
}
