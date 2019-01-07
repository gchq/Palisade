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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.gchq.palisade.Util;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

public class HeartbeatTestBackingStore implements BackingStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(HeartbeatTestBackingStore.class);

    /**
     * The actual backing store for all cached data.
     */
    private final ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<>();

    /**
     * The map of removal handles for time to live entries.
     */
    private final ConcurrentHashMap<String, ScheduledFuture<?>> removals = new ConcurrentHashMap<>();

    /**
     * Timer thread to remove cache entries after expiry.
     */
    private static final ScheduledExecutorService REMOVAL_TIMER = Executors.newSingleThreadScheduledExecutor(Util.createDaemonThreadFactory());

    public HeartbeatTestBackingStore() {
    }

    /**
     * Simple POJO for pairing together the object's class with the encoded form of the object.
     */
    private static class CachedPair {
        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("value", "\"" + new String(value) + "\"")
                    .append("clazz", clazz)
                    .append("locallyCacheable", locallyCacheable)
                    .toString();
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            CachedPair that = (CachedPair) o;

            return new EqualsBuilder()
                    .append(value, that.value)
                    .append(clazz, that.clazz)
                    .append(locallyCacheable, that.locallyCacheable)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .append(value)
                    .append(clazz)
                    .append(locallyCacheable)
                    .toHashCode();
        }

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

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("cache", cache)
                .append("removals", removals)
                .toString();
    }

    @Override
    public boolean add(final String key, final Class<?> valueClass, final byte[] value, final Optional<Duration> timeToLive) {
        String cacheKey = validateAddParameters(key, valueClass, value, timeToLive);
        LOGGER.debug("Adding to cache key {} of class {}", key, valueClass);
        cache.put(cacheKey, new CachedPair(value, valueClass));
        /*Here we set up a simple timer to deal with the removal of the item from the cache if a duration is present
         *This uses a single timer to remove elements, this is fine for this example, but in production we would want
         *something more performant.
         */
        //remove the old TTL handle if is there
        ScheduledFuture<?> oldHandle = removals.remove(cacheKey);
        //cancel the task
        if (nonNull(oldHandle)) {
            oldHandle.cancel(true);
        }

        timeToLive.ifPresent(duration -> {
            ScheduledFuture<?> removalHandle = REMOVAL_TIMER.schedule(() -> {
                cache.remove(cacheKey);
                removals.remove(cacheKey);
            }, duration.toMillis(), TimeUnit.MILLISECONDS);
            //store new handle
            removals.put(cacheKey, removalHandle);
        });
        return true;
    }

    @Override
    public SimpleCacheObject get(final String key) {
        throw new UnsupportedOperationException();
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

    @Override
    public boolean remove(final String key) {
        throw new UnsupportedOperationException();
    }
}
