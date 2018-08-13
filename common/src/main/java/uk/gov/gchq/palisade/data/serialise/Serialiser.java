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

import java.io.InputStream;
import java.io.Serializable;
import java.util.stream.Stream;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = As.EXISTING_PROPERTY, property = "class")
public interface Serialiser<I> extends Serializable {

    /**
     * Serialises a {@link Stream} of objects into an {@link InputStream}.
     *
     * @param objects the stream of objects to be serialised
     * @return the serialised form
     */
    InputStream serialise(final Stream<I> objects);

    /**
     * Deserialise an {@link InputStream} into a {@link Stream} of objects.
     *
     * @param stream the input stream to deserialise
     * @return the deserialised object
     */
    Stream<I> deserialise(final InputStream stream);

    @JsonGetter("class")
    default String _getClass() {
        return getClass().getName();
    }

    @JsonSetter("class")
    default void _setClass(final String className) {
        // do nothing.
    }
}
