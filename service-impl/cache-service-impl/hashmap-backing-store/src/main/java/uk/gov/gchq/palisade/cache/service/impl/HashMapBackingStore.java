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
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * A HashMapBackingStore is a simple implementation of a {@link BackingStore} that simply caches the objects in a
 * ConcurrentHashMap. By default the cache is static so it will be shared across the same JVM. This is designed to be
 * the simplest implementation of {@link BackingStore} suitable for use in examples only.
 */
public class HashMapBackingStore implements BackingStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(HashMapBackingStore.class);

    /**
     * The static cache that will cause all instances of this across a JVM to be shared.
     */
    private static final ConcurrentHashMap<String, CachedPair> CACHE = new ConcurrentHashMap<>();
    /**
     * The actual backing store for all cached data.
     */
    private final ConcurrentHashMap<String, CachedPair> cache;

    /**
     * Simple POJO for pairing together the object's class with the encoded form of the object.
     */
    private static class CachedPair {
        /**
         * Encoded form.
         */
        public final byte[] value;
        /**
         * Class of the value field.
         */
        public final Class<?> clazz;

        /**
         * Create a cache entry pair.
         *
         * @param value encoded object
         * @param clazz Java class object
         */
        CachedPair(final byte[] value, final Class<?> clazz) {
            this.value = value;
            this.clazz = clazz;
        }
    }

    /**
     * Timer thread to remove cache entries after expiry.
     */
    private static final ScheduledExecutorService REMOVAL_TIMER = Executors.newSingleThreadScheduledExecutor();

    /**
     * Create a {@link HashMapBackingStore} which uses the JVM wide shared object cache.
     */
    public HashMapBackingStore() {
        this(true);
    }

    /**
     * Create a store which may have its own store or may use the JVM shared instance.
     *
     * @param useStatic if true then use the JVM shared backing store
     */
    public HashMapBackingStore(final boolean useStatic) {
        if (useStatic) {
            cache = CACHE;
        } else {
            cache = new ConcurrentHashMap<>();
        }
    }

    @Override
    public boolean store(final String key, final Class<?> valueClass, final byte[] value, final Optional<Duration> timeToLive) {
        String cacheKey = BackingStore.keyCheck(key);
        requireNonNull(valueClass, "valueClass");
        requireNonNull(value, "value");
        requireNonNull(timeToLive, "timeToLive");
        timeToLive.ifPresent(x -> {
            if (x.isNegative()) {
                throw new IllegalArgumentException("time to live cannot be negative");
            }
        });
        LOGGER.debug("Adding to cache: {} of class {}", new String(value), valueClass);
        cache.put(cacheKey, new CachedPair(value, valueClass));
        /*Here we set up a simple timer to deal with the removal of the item from the cache if a duration is present
         *This uses a single timer to remove elements, this is fine for this example, but in production we would want
         *something more performant.
         */
        timeToLive.ifPresent(duration -> {
            REMOVAL_TIMER.schedule(() -> cache.remove(cacheKey), duration.toMillis(), TimeUnit.MILLISECONDS);
        });
        return true;
    }

    @Override
    public BasicCacheObject retrieve(final String key) {
        String cacheKey = BackingStore.keyCheck(key);
        LOGGER.debug("Getting from cache: {}", cacheKey);
        final CachedPair result = cache.getOrDefault(cacheKey, new CachedPair(null, Object.class));
        return new BasicCacheObject(result.clazz, Optional.ofNullable(result.value));
    }

    @Override
    public Stream<String> list(final String prefix) {
        requireNonNull(prefix, "prefix");
        return cache.keySet()
                .stream()
                .filter(x -> x.startsWith(
                                prefix)
                );
    }
}
