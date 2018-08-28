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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;

public class MapReduceExampleIT {

    @Test
    public void shouldExitWithOneWithNoArgs() throws Exception {
        //Given
        final MapReduceExample example = new MapReduceExample();
        //When
        int exitCode = example.run(new String[0]);
        //Then
        assertEquals(1, exitCode);
    }

    @Test
    public void shouldRunWithoutErrors() throws Exception {
        // Given
        final MapReduceExample example = new MapReduceExample();
        final Path tempDir = Files.createTempDirectory("mapreduce-example");
        //remove this as it needs to be not present when the job runs
        Files.deleteIfExists(tempDir);
        try {
            // When
            example.main(tempDir.toAbsolutePath().toString());
            // Then - no exceptions
        } finally {
            //remove temporary output
            Files.walk(tempDir)
                    .map(Path::toFile)
                    .sorted((o1, o2) -> -o1.compareTo(o2))
                    .forEach(File::delete);
        }
    }

    @Test
    public void shouldProduceKnownResults() throws Exception {
        //Given
        final MapReduceExample example = new MapReduceExample();
        final Path tempDir = Files.createTempDirectory("mapreduce-example");
        //remove this as it needs to be not present when the job runs
        Files.deleteIfExists(tempDir);
        //read the expected results
        String expected = slurpStream(MapReduceExampleIT.class.getResourceAsStream("/expected_results.txt"));

        try {
            // When
            example.main(tempDir.toAbsolutePath().toString());
            //read actual results
            String actual = slurpStream(Files.newInputStream(tempDir.resolve("part-r-00000")));

            //Then
            assertEquals(expected, actual);
        } finally {
            //remove temporary output
            Files.walk(tempDir)
                    .map(Path::toFile)
                    .sorted((o1, o2) -> -o1.compareTo(o2))
                    .forEach(File::delete);
        }
    }

    /**
     * Read an entire file in one string.
     *
     * @param in the inputstream to slurp
     * @return the contents
     * @throws IOException if the scanner can't work
     */
    private static String slurpStream(InputStream in) throws IOException {
        return new Scanner(in).useDelimiter("\\x00").next();
    }
}
