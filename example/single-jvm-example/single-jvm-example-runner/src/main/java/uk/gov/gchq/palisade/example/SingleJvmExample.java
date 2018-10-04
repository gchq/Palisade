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

import uk.gov.gchq.palisade.client.ConfiguredClientServices;
import uk.gov.gchq.palisade.config.service.InitialConfigurationService;
import uk.gov.gchq.palisade.example.client.ExampleConfigurator;
import uk.gov.gchq.palisade.example.client.ExampleSimpleClient;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.stream.Stream;

public class SingleJvmExample {
    protected static final String FILE = new File("exampleObj_file1.txt").getAbsolutePath();

    private static final Logger LOGGER = LoggerFactory.getLogger(SingleJvmExample.class);

    public static void main(final String[] args) throws Exception {
        new SingleJvmExample().run();
    }

    public void run() throws Exception {
        createDataPath();
        try {
            final InitialConfigurationService ics = ExampleConfigurator.setupSingleJVMConfigurationService();
            //request the client configuration by not specifying a service
            final ConfiguredClientServices cs = new ConfiguredClientServices(ics);
            final ExampleSimpleClient client = new ExampleSimpleClient(cs, FILE);

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
        } finally {
            FileUtils.deleteQuietly(new File(FILE));
        }
    }

    static void createDataPath() {
        try (final InputStream data = SingleJvmExample.class.getResourceAsStream("/example/exampleObj_file1.txt")) {
            Objects.requireNonNull(data, "couldn't load file: data/example/exampleObj_file1.txt");
            FileUtils.copyInputStreamToFile(data, new File(FILE));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
