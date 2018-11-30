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
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Scanner;

import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertEquals;
import static uk.gov.gchq.palisade.example.MapReduceExample.DEFAULT_OUTPUT_DIR;

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
        // When
        try {
            MapReduceExample.main(new String[]{"/test_ExampleObj.txt"});
        } finally {
            // Then - no exceptions
            FileUtils.deleteDirectory(new File(DEFAULT_OUTPUT_DIR));
        }
    }

    @Test
    public void shouldProduceKnownResults() throws Exception {
        try {
            //Given
            String expected = slurpStream(MapReduceExampleIT.class.getResourceAsStream("/expected_results.txt"));
            // When
            MapReduceExample.main(new String[]{"/test_ExampleObj.txt"});
            //read actual results
            requireNonNull(DEFAULT_OUTPUT_DIR, "The temp directory cannot be null.");
            String actual = slurpStream(Files.newInputStream(new File(DEFAULT_OUTPUT_DIR).toPath().resolve("part-r-00000")));
            //Then
            assertEquals(expected, actual);
        } finally {
            FileUtils.deleteDirectory(new File(DEFAULT_OUTPUT_DIR));
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
