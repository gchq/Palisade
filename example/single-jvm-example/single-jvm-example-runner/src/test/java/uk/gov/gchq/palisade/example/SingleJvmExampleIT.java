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

import org.apache.commons.io.FileUtils;

import org.junit.AfterClass;
import org.junit.Test;

import uk.gov.gchq.palisade.example.client.ExampleSimpleClient;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.gchq.palisade.util.JsonAssert.assertEquals;

public class SingleJvmExampleIT {
    private static final String FILE = createDataPath();

    @AfterClass
    public static void deleteFile() {
        FileUtils.deleteQuietly(new File(FILE));
    }

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
        final ExampleSimpleClient client = new ExampleSimpleClient(FILE);

        // When
        final Stream<ExampleObj> aliceResults = client.read(FILE, "Alice", "Payroll");

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
        final ExampleSimpleClient client = new ExampleSimpleClient(FILE);

        // When
        final Stream<ExampleObj> aliceResults = client.read(FILE, "Bob", "Payroll");

        // Then
        assertEquals(
                Arrays.asList(
                        new ExampleObj("redacted", "public", 20L),
                        new ExampleObj("redacted", "public", 20L)
                ),
                aliceResults.collect(Collectors.toList())
        );
    }

    private static String createDataPath() {
        final File targetFile = new File("data/example/exampleObj_file1.txt");
        try (final InputStream data = SingleJvmExample.class.getResourceAsStream("/example/exampleObj_file1.txt")) {
            Objects.requireNonNull(data, "couldn't load file: data/example/exampleObj_file1.txt");
            FileUtils.copyInputStreamToFile(data, targetFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return targetFile.getAbsolutePath();
    }
}
