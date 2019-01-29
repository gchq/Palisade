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

import java.io.File;
import java.util.stream.Stream;

public class MultiJVMDockerExample {
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiJVMDockerExample.class);
    //For the Docker build, this will have been created in the container before this executes
    protected static final String FILE = new File("/data/exampleObj_file1.txt").getAbsolutePath();

    public static void main(final String[] args) throws Exception {
        new MultiJVMDockerExample().run();
    }

    public void run() throws Exception {
        //this will write an initial configuration
//        DistributedServicesConfigurator.main(new String[]{"http://etcd:2379", "http://palisade-service:8080/palisade", "http://policy-service:8080/policy", "http://resource-service:8080/resource", "http://user-service:8080/user", "http://data-service:8080/data", "http://config-service:8080/config"});
        ConfigurationService configService = RestUtil.createService(ExampleSimpleClient.class, "configRest.json", ConfigurationService.class);
        final ConfiguredClientServices cs = new ConfiguredClientServices(configService);
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
