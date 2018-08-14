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

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class SimpleStringSerialiserTest {
    @Test
    public void shouldDeserialise() throws IOException {
        // Given
        final SimpleStringSerialiser serialiser = new SimpleStringSerialiser();
        final byte[] input = "line1\nline2".getBytes();

        // When
        final Stream<String> result = serialiser.deserialise(new ByteArrayInputStream(input));

        // Then
        assertEquals(Arrays.asList("line1", "line2"), result.collect(Collectors.toList()));
    }

    @Test
    public void shouldSerialise() throws IOException {
        // Given
        final SimpleStringSerialiser serialiser = new SimpleStringSerialiser();
        final Stream<String> input = Stream.of("line1", "line2");

        // When
        final InputStream result = serialiser.serialise(input);

        // Then
        assertEquals(Arrays.asList("line1", "line2"), IOUtils.readLines(result));
    }
}
