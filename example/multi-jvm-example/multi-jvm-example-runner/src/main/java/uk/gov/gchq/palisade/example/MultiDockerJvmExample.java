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

import uk.gov.gchq.palisade.client.ConfiguredServices;
import uk.gov.gchq.palisade.config.service.InitialConfigurationService;
import uk.gov.gchq.palisade.config.service.request.GetConfigRequest;
import uk.gov.gchq.palisade.example.client.ExampleConfigCreator;
import uk.gov.gchq.palisade.example.client.ExampleSimpleClient;
import uk.gov.gchq.palisade.service.request.InitialConfig;

import java.io.File;
import java.util.Optional;
import java.util.stream.Stream;

public class MultiDockerJvmExample {
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiDockerJvmExample.class);
    //For the Docker build, this will have been created in the container before this executes
    protected static final String FILE = new File("/data/example/exampleObj_file1.txt").getAbsolutePath();

    public static void main(final String[] args) throws Exception {
        new MultiDockerJvmExample().run();
    }

    public void run() throws Exception {
        final InitialConfigurationService ics = ExampleConfigCreator.setupMultiJVMConfigurationService();
        //request the client configuration by not specifiying a service
        final InitialConfig config = ics.get(new GetConfigRequest()
                .service(Optional.empty()))
                .join();

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
    }
}
