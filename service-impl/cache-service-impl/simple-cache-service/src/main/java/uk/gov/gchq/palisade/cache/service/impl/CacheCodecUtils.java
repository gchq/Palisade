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

import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;

import java.nio.charset.Charset;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class CacheCodecUtils {
    private CacheCodecUtils() {
    }

    /**
     * Character set for String encoding.
     */
    public static final Charset UTF_8 = Charset.forName("UTF8");

    /**
     * JSON encoder.
     */
    public static final Function<?, byte[]> DEFAULT_ENCODER = x -> JSONSerialiser.serialise(x);
    /**
     * JSON decoder.
     */
    public static final BiFunction<byte[], ? extends Class<?>, ?> DEFAULT_DECODER = (ob, expectedClass) -> JSONSerialiser.deserialise(ob, expectedClass);

    /**
     * String encoder, just converts to bytes in UTF-8.
     */
    public static final Function<String, byte[]> STRING_ENCODER = x -> x.getBytes(UTF_8);

    /**
     * String decoder.
     */
    public static final BiFunction<byte[], ? extends Class<String>, String> STRING_DECODER = (ob, expectedClass) -> new String(ob, UTF_8);
    /**
     * Byte array encoder is just the identity function.
     */
    public static final Function<byte[], byte[]> BYTE_ENCODER = Function.identity();
    /**
     * Byte array decoder just returns the array.
     */
    public static final BiFunction<byte[], ? extends Class<byte[]>, byte[]> BYTE_DECODER = (ob, ignore) -> ob;
}
