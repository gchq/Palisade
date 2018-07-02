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

package uk.gov.gchq.palisade.example;

import org.junit.Test;

import uk.gov.gchq.palisade.example.client.ExampleSimpleClient;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.gchq.palisade.util.JsonAssert.assertEquals;

public class SingleJvmExampleIT {
    @Test
    public void shouldRunWithoutErrors() throws Exception {
        // Given
        final SingleJvmExample example = new SingleJvmExample();

        // When
        example.run();

        // Then - no exceptions
    }

    @Test
    public void shouldReadAsAlice() throws Exception {
        // Given
        final ExampleSimpleClient client = new ExampleSimpleClient();

        // When
        final Stream<ExampleObj> aliceResults = client.read("file1", "Alice", "Payroll");

        // Then
        assertEquals(
                Arrays.asList(
                        new ExampleObj("item1c", "public", 20L),
                        new ExampleObj("item1d", "private", 20L),
                        new ExampleObj("item2c", "public", 20L),
                        new ExampleObj("item2d", "private", 20L)
                ),
                aliceResults.collect(Collectors.toList())
        );
    }

    @Test
    public void shouldReadAsBob() throws Exception {
        // Given
        final ExampleSimpleClient client = new ExampleSimpleClient();

        // When
        final Stream<ExampleObj> aliceResults = client.read("file1", "Bob", "Payroll");

        // Then
        assertEquals(
                Arrays.asList(
                        new ExampleObj("item1c", "public", 20L),
                        new ExampleObj("item2c", "public", 20L)
                ),
                aliceResults.collect(Collectors.toList())
        );
    }
}
