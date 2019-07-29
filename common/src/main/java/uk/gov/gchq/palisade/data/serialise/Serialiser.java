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

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.stream.Stream;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = As.EXISTING_PROPERTY, property = "class")
public interface Serialiser<I> extends Serializable {

    /**
     * Serialises a {@link Stream} of objects to an {@link OutputStream}. If {@code objects} is {@code null}, then
     * nothing will be written.
     *
     * @param objects the stream of objects to be serialised
     * @param output  the output stream to write the serialised bytes to
     * @return this object
     * @throws IOException if something fails while writing the object stream
     */
    Serialiser<I> serialise(final Stream<I> objects, final OutputStream output) throws IOException;

    /**
     * Deserialise an {@link InputStream} into a {@link Stream} of objects.
     *
     * @param stream the input stream to deserialise
     * @return the deserialised object
     * @throws IOException if the input stream couldn't be read from.
     */
    Stream<I> deserialise(final InputStream stream) throws IOException;

    @JsonGetter("class")
    default String _getClass() {
        return getClass().getName();
    }

    @JsonSetter("class")
    default void _setClass(final String className) {
        // do nothing.
    }
}
