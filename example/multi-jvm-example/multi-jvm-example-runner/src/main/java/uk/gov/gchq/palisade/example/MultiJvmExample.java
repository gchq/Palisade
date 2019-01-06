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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.client.ConfiguredClientServices;
import uk.gov.gchq.palisade.config.service.ConfigurationService;
import uk.gov.gchq.palisade.example.client.ExampleSimpleClient;
import uk.gov.gchq.palisade.rest.RestUtil;

import java.util.stream.Stream;

public class MultiJvmExample {
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiJvmExample.class);

    public static void main(final String[] args) throws Exception {
        if (args.length < 1) {
            System.out.printf("Usage: %s file\n", MultiJvmExample.class.getTypeName());
            System.out.println("\nfile\tfile containing serialised ExampleObj instances to read");
            System.exit(1);
        }

        String sourceFile = args[0];
        new MultiJvmExample().run(sourceFile);
    }

    public void run(final String sourceFile) throws Exception {

        final ConfiguredClientServices cs = new ConfiguredClientServices(RestUtil.createService(ExampleSimpleClient.class, RestUtil.CONFIG_SERVICE_PATH, ConfigurationService.class));

        final ExampleSimpleClient client = new ExampleSimpleClient(cs, sourceFile);

        LOGGER.info("");
        LOGGER.info("Alice is reading file1...");
        final Stream<ExampleObj> aliceResults = client.read(sourceFile, "Alice", "Payroll");
        LOGGER.info("Alice got back: ");
        aliceResults.map(Object::toString).forEach(LOGGER::info);

        LOGGER.info("");
        LOGGER.info("Bob is reading file1...");
        final Stream<ExampleObj> bobResults = client.read(sourceFile, "Bob", "Payroll");
        LOGGER.info("Bob got back: ");
        bobResults.map(Object::toString).forEach(LOGGER::info);

    }
}
