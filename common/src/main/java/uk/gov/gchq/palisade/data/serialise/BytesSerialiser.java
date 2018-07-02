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
package uk.gov.gchq.palisade.data.serialise;

public interface BytesSerialiser<T> extends Serialiser<byte[], T> {

    /**
     * Serialise some object into bytes.
     *
     * @param object the object to be serialised
     * @return the serialised bytes
     */
    byte[] serialise(final T object);

    /**
     * Deserialise some bytes into an object.
     *
     * @param bytes the bytes to deserialise
     * @return the deserialised object
     */
    T deserialise(final byte[] bytes);
}
