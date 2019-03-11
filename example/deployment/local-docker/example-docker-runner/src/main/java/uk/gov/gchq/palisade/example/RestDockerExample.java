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

import uk.gov.gchq.palisade.ConfigConsts;
import uk.gov.gchq.palisade.config.service.ConfigurationService;
import uk.gov.gchq.palisade.config.service.Configurator;
import uk.gov.gchq.palisade.example.client.ExampleSimpleClient;
import uk.gov.gchq.palisade.example.hrdatagenerator.types.Employee;
import uk.gov.gchq.palisade.exception.NoConfigException;
import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.palisade.rest.RestUtil;
import uk.gov.gchq.palisade.service.PalisadeService;
import uk.gov.gchq.palisade.service.ServiceState;
import uk.gov.gchq.palisade.util.StreamUtil;

import java.io.File;
import java.io.InputStream;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.isNull;

public class RestDockerExample {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestDockerExample.class);
    //For the Docker build, this will have been created in the container before this executes
    protected static final String FILE = new File("/data/exampleObj_file1.txt").getAbsolutePath();

    public static void main(final String[] args) throws Exception {
        new RestDockerExample().run();
    }

    public void run() throws Exception {
        final InputStream stream = StreamUtil.openStream(this.getClass(), System.getProperty(RestUtil.CONFIG_SERVICE_PATH));
        ConfigurationService configService = JSONSerialiser.deserialise(stream, ConfigurationService.class);

        ServiceState clientConfig = null;
        int times = 0;
        while (isNull(clientConfig) && times < 30) {
            try {
                clientConfig = new Configurator(configService).retrieveConfig(Optional.empty());
            } catch (NoConfigException e) {
                LOGGER.warn("No client configuration present, waiting...");
                Thread.sleep(ConfigConsts.DELAY);
                times++;
            }
        }

        if (isNull(clientConfig)) {
            throw new RuntimeException("Couldn't retrieve client configuration. Is configuration service running?");
        }

        PalisadeService palisade = Configurator.createFromConfig(PalisadeService.class, clientConfig);

        final ExampleSimpleClient client = new ExampleSimpleClient(palisade);

        LOGGER.info("");
        LOGGER.info("Alice is reading file1...");
        final Stream<Employee> aliceResults = client.read(FILE, "Alice", "Payroll");
        LOGGER.info("Alice got back: ");
        aliceResults.map(Object::toString).forEach(LOGGER::info);

        LOGGER.info("");
        LOGGER.info("Bob is reading file1...");
        final Stream<Employee> bobResults = client.read(FILE, "Bob", "Payroll");
        LOGGER.info("Bob got back: ");
        bobResults.map(Object::toString).forEach(LOGGER::info);
    }
}
