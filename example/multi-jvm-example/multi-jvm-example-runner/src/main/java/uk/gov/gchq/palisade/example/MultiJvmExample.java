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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.example.client.ExampleSimpleRestClient;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class MultiJvmExample {
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiJvmExample.class);
    public static final String FILE = createDataPath();

    public static void main(final String[] args) throws Exception {
        new MultiJvmExample().run();
    }

    public void run() throws Exception {
        final ExampleSimpleRestClient client = new ExampleSimpleRestClient(FILE);

        LOGGER.info("");
        LOGGER.info("Alice is reading file1...");
        final Stream<ExampleObj> aliceResults = client.read(FILE, "Alice", "Payroll");
        LOGGER.info("Alice got back: ");
        aliceResults.map(Object::toString).forEach(LOGGER::info);

        LOGGER.info("");
        LOGGER.info("Bob is reading file1...");
        final Stream<ExampleObj> bobResults = client.read(FILE, "Bob", "Payroll");
        LOGGER.info("Bob got back: ");
        bobResults.map(Object::toString).forEach(LOGGER::info);

        FileUtils.deleteQuietly(new File(FILE));
    }

    private static String createDataPath() {
        final File targetFile = new File("data/example/exampleObj_file1.txt");
        try (final InputStream data = MultiJvmExample.class.getResourceAsStream("/example/exampleObj_file1.txt")) {
            requireNonNull(data, "couldn't load file: data/example/exampleObj_file1.txt");
            FileUtils.copyInputStreamToFile(data, targetFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return targetFile.getAbsolutePath();
    }
}
