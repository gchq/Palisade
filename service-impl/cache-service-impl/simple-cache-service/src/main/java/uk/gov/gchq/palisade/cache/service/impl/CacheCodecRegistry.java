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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * Maintains a registry of encoder and decoder function pairs (codecs) for various object types in to byte arrays. This
 * is so that the cache can encode the various values into a backing store. Clients may add extra specialisations if
 * they wish via {@link CacheCodecRegistry#registerFunctionPair(Class, Function, BiFunction)}. JSON serialisation is
 * used as the default codec.
 */
public final class CacheCodecRegistry {

    /**
     * Map of encoder functions.
     */
    @JsonIgnore
    private final Map<Class<?>, Function<?, byte[]>> addMap = new HashMap<>();

    /**
     * Map of decoder functions.
     */
    @JsonIgnore
    private final Map<Class<?>, BiFunction<byte[], ? extends Class<?>, ?>> getMap = new HashMap<>();

    /**
     * Default constructor registers some simple codecs.
     */
    public CacheCodecRegistry() {
        registerDefaults();
    }

    /**
     * Creates the default mappings.
     */
    private void registerDefaults() {
        registerFunctionPair(byte[].class, CacheCodecUtils.BYTE_ENCODER, CacheCodecUtils.BYTE_DECODER);
        registerFunctionPair(String.class, CacheCodecUtils.STRING_ENCODER, CacheCodecUtils.STRING_DECODER);
    }

    /**
     * Creates a mapping for a given class type. This creates a mapping from objects of the type represented by the
     * {@link Class} <code>clazz</code> and the en(de)coder functions given.
     *
     * @param clazz   the class type to add
     * @param encoder the encoder function
     * @param decoder the decoder function
     * @param <V>     the object type represented by {@link Class} <code>clazz</code>
     */
    public synchronized <V> void registerFunctionPair(final Class<V> clazz, final Function<V, byte[]> encoder, final BiFunction<byte[], ? extends Class<V>, V> decoder) {
        requireNonNull(clazz, "clazz");
        requireNonNull(encoder, "encoder");
        requireNonNull(decoder, "decoder");
        addMap.put(clazz, encoder);
        getMap.put(clazz, decoder);
    }

    /**
     * Removes a mapping for the given class type. This removes the codec from the codec registry. No error is produced
     * if the class mapping does not exist.
     *
     * @param clazz the class type to remove
     */
    public synchronized void unregisterCodec(final Class<?> clazz) {
        requireNonNull(clazz, "clazz");
        addMap.remove(clazz);
        getMap.remove(clazz);
    }

    /**
     * Find an appropriate encoder for a given class. Returns {@link CacheCodecUtils#DEFAULT_ENCODER} if no specific
     * encoder is registered.
     *
     * @param valueClass the class type to encode
     * @param <V>        type parameter for the <code>valueClass</code>
     * @return the encoder function
     */
    @SuppressWarnings("unchecked")
    public synchronized <V> Function<V, byte[]> getValueEncoder(final Class<V> valueClass) {
        requireNonNull(valueClass, "valueClass");
        return (Function<V, byte[]>) addMap.getOrDefault(valueClass, CacheCodecUtils.DEFAULT_ENCODER);
    }

    /**
     * Find an appropriate decoder for a given class. Returns {@link CacheCodecUtils#DEFAULT_DECODER} if no specific
     * decoder is registered.
     *
     * @param valueClass the class type to decode
     * @param <V>        type parameter for the <code>valueClass</code>
     * @return the decoder function
     */
    @SuppressWarnings("unchecked")
    public synchronized <V> BiFunction<byte[], Class<V>, V> getValueDecoder(final Class<V> valueClass) {
        requireNonNull(valueClass, "valueClass");
        return (BiFunction<byte[], Class<V>, V>) getMap.getOrDefault(valueClass, CacheCodecUtils.DEFAULT_DECODER);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CacheCodecRegistry that = (CacheCodecRegistry) o;

        return new EqualsBuilder()
                .append(addMap, that.addMap)
                .append(getMap, that.getMap)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(addMap)
                .append(getMap)
                .toHashCode();
    }
}
