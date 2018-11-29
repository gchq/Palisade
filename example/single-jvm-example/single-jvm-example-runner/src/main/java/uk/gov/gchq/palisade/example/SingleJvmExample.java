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
import uk.gov.gchq.palisade.config.service.ConfigurationService;
import uk.gov.gchq.palisade.example.client.ExampleConfigurator;
import uk.gov.gchq.palisade.example.client.ExampleSimpleClient;
import uk.gov.gchq.palisade.example.client.ExampleUtils;

import java.io.File;
import java.util.stream.Stream;

public class SingleJvmExample {
    protected static final String DESTINATION = new File("exampleObj_file1.txt").getAbsolutePath();
    protected static final String FILE = new File("example/exampleObj_file1.txt").getPath();

    private static final Logger LOGGER = LoggerFactory.getLogger(SingleJvmExample.class);

    public static void main(final String[] args) throws Exception {
        new SingleJvmExample().run();
    }

    public void run() throws Exception {
        ExampleUtils.createDataPath(FILE, DESTINATION, this.getClass());
        try {
            final ConfigurationService ics = ExampleConfigurator.setupSingleJVMConfigurationService();
            //request the client configuration by not specifying a service
            final ConfiguredClientServices cs = new ConfiguredClientServices(ics);
            final ExampleSimpleClient client = new ExampleSimpleClient(cs, DESTINATION);

            LOGGER.info("");
            LOGGER.info("Alice is reading file1...");
            final Stream<ExampleObj> aliceResults = client.read(DESTINATION, "Alice", "Payroll");
            LOGGER.info("Alice got back: ");
            aliceResults.map(Object::toString).forEach(LOGGER::info);

            LOGGER.info("");
            LOGGER.info("Bob is reading file1...");
            final Stream<ExampleObj> bobResults = client.read(DESTINATION, "Bob", "Payroll");
            LOGGER.info("Bob got back: ");
            bobResults.map(Object::toString).forEach(LOGGER::info);
        } finally {
            FileUtils.deleteQuietly(new File(DESTINATION));
        }
    }
}
