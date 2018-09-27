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

import uk.gov.gchq.palisade.client.ConfiguredServices;
import uk.gov.gchq.palisade.config.service.InitialConfigurationService;
import uk.gov.gchq.palisade.config.service.impl.ProxyRestConfigService;
import uk.gov.gchq.palisade.config.service.request.GetConfigRequest;
import uk.gov.gchq.palisade.example.client.ExampleConfigurator;
import uk.gov.gchq.palisade.example.client.ExampleSimpleClient;
import uk.gov.gchq.palisade.service.request.InitialConfig;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class MultiJvmExample {
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiJvmExample.class);
    protected static final String FILE = new File("exampleObj_file1.txt").getAbsolutePath();
    protected static final String CACHE_FILE = new File("example_config.txt").getAbsolutePath();

    public static void main(final String[] args) throws Exception {
        new MultiJvmExample().run();
    }

    public void run() throws Exception {
        createDataPath();
        try {
            ExampleConfigurator.setupMultiJVMConfigurationService(Paths.get(CACHE_FILE));
            //request the client configuration by not specifiying a service
            final InitialConfigurationService ics = new ProxyRestConfigService("http://localhost:8085/config");
            final InitialConfig config = ics.get(new GetConfigRequest()
                    .service(Optional.empty()))
                    .join();

            System.err.println(config);

            final ConfiguredServices cs = new ConfiguredServices(config);

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
            FileUtils.deleteQuietly(new File(CACHE_FILE));
        }
    }

    static void createDataPath() {
        final File targetFile = new File(FILE);
        try (final InputStream data = MultiJvmExample.class.getResourceAsStream("/example/exampleObj_file1.txt")) {
            requireNonNull(data, "couldn't load file: " + FILE);
            FileUtils.copyInputStreamToFile(data, targetFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
