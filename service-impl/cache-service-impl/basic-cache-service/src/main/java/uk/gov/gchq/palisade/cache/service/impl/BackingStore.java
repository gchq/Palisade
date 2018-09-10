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

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.Duration;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;


/**
 * This class defines the basic operations that any backing add to the cache service must define. This allows for many
 * different database or persistence mechanisms to be used as stores for Palisades. This interface should be
 * deliberately kept as minimal and simple as possible. Persistence mechanisms must be able to support a "time to live"
 * facility on cache entries; it is assumed that after the specified time to live has expired the associated data will
 * be automatically removed from the backing add and subsequent attempts to get that entry will fail.
 *
 * @apiNote All methods on this class are assumed to throw {@link NullPointerException} if any method parameters are
 * <code>null</code>.
 */
@JsonPropertyOrder(value = {"class"}, alphabetic = true)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "class"
)
public interface BackingStore {

    /**
     * Store the given data in the backing add. The byte array <code>value</code> is assumed to encode an object of
     * the type represented by the class <code>valueClass</code>. The <code>key</code> must not be empty or
     * <code>null</code>. If a time to live duration is required on this entry, then the optional specified should not
     * be empty. Durations must not be negative.
     * <p>
     * It is the responsibility of the backing store to ensure that the <code>valueClass</code> is stored along with the
     * object byte array. The standard way to do this is to store the canonical name of the class. See {@link
     * Class#getCanonicalName()}.
     *
     * @param key        the cache key to add this entry
     * @param valueClass the object type represented in the byte array
     * @param value      the encoded object
     * @param timeToLive an optional time to live, maybe be empty
     * @return true if and only if the the cache entry was made successfully
     * @throws IllegalArgumentException if the duration is negative
     * @throws IllegalArgumentException if <code>key</code> is empty (once whitespace is trimmed)
     */
    boolean add(final String key, final Class<?> valueClass, final byte[] value, final Optional<Duration> timeToLive);

    /**
     * Store the given data with infinite time to live. This a convenience method that simply calls {@link
     * BackingStore#add(String, Class, byte[], Optional)} with an empty duration.
     *
     * @param key        the cache key to add this entry
     * @param valueClass the object type represented in the byte array
     * @param value      the encoded object
     * @return true if and only if the cache entry was made successfully
     * @throws IllegalArgumentException if <code>key</code> is empty (once whitespace is trimmed)
     * @see BackingStore#add(String, Class, byte[], Optional)
     */
    default boolean add(final String key, final Class<?> valueClass, final byte[] value) {
        return add(key, valueClass, value, Optional.empty());
    }

    /**
     * Attempt to get the given key from the backing add. Looks up the given key and attempts to get it. If
     * the requested key couldn't be found then the value of the returned {@link BasicCacheObject} will be empty. A new
     * cache object should be returned for each call to this method. If the key can't be found, then the expected class
     * should be <code>Object.class</code> inside the returned object.
     *
     * @param key the key to lookup
     * @return a new cache object
     * @throws IllegalArgumentException if <code>key</code> is empty (once whitespace is trimmed)
     */
    BasicCacheObject get(final String key);

    /**
     * Get a stream of all keys with a given prefix. This method acts as a way to query the backing add to find which
     * keys it has. The returned list will have all the keys that start with the given string in it.
     *
     * @param prefix the prefix to look for
     * @return a stream of valid keys
     */
    Stream<String> list(final String prefix);

    /**
     * Check the provided key is not empty.
     *
     * @param key the backing add key
     * @return the key
     * @throws IllegalArgumentException if the given key is <code>null</code> once whitespace is trimmed
     */
    static String keyCheck(final String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("key cannot be empty");
        }
        return key;
    }

    /**
     * Check the provided duration. If the <code>duration</code> is  <code>null</code> or negative an exception is
     * thrown.
     *
     * @param duration duration to check
     * @throws NullPointerException     if <code>duration</code> is <code>null</code>
     * @throws IllegalArgumentException if the duration is negative
     */
    static void durationCheck(final Optional<Duration> duration) {
        requireNonNull(duration, "timeToLive");
        duration.ifPresent(x -> {
            if (x.isNegative()) {
                throw new IllegalArgumentException("time to live cannot be negative");
            }
        });
    }

    @JsonGetter("class")
    default String _getClass() {
        return getClass().getName();
    }

    @JsonSetter("class")
    default void _setClass(final String className) {
        // do nothing.
    }
}
