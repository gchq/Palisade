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
import uk.gov.gchq.palisade.User;
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


import java.io.InputStream;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.isNull;

public class RestExample {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestExample.class);

    public static void main(final String[] args) throws Exception {
        if (args.length < 1) {
            System.out.printf("Usage: %s file\n", RestExample.class.getTypeName());
            System.out.println("\nfile\tfile containing serialised Employee instances to read");
            System.exit(1);
        }

        String sourceFile = args[0];
        new RestExample().run(sourceFile);
    }

    public void run(final String sourceFile) throws Exception {
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

        // userId(new UserId().id(userId)).context(new Context().purpose(purpose));

        final User alice = new User()
                .userId("Alice")
//                .auths("public", "private")
                .roles("HR", "PAYROLL");

        final User bob = new User()
                .userId("Bob")
//                .auths("public")
                .roles("ESTATES");

        final User eve = new User()
                .userId("Eve")
//                .auths("public")
                .roles("IT");

        LOGGER.info("");
        LOGGER.info("Alice [ " + alice.toString() + " } is reading the Employee file with a purpose of SALARY...");
        final Stream<Employee> aliceResults = client.read(sourceFile, "Alice", "SALARY");
        LOGGER.info("Alice got back: ");
        aliceResults.map(Object::toString).forEach(LOGGER::info);

        LOGGER.info("");
        LOGGER.info("Alice [ " + alice.toString() + " } is reading the Employee file with a purpose of DUTY_OF_CARE...");
        final Stream<Employee> aliceResults2 = client.read(sourceFile, "Alice", "DUTY_OF_CARE");
        LOGGER.info("Alice got back: ");
        aliceResults2.map(Object::toString).forEach(LOGGER::info);

        LOGGER.info("");
        LOGGER.info("Alice [ " + alice.toString() + " } is reading the Employee file with a purpose of STAFF_REPORT...");
        final Stream<Employee> aliceResults3 = client.read(sourceFile, "Alice", "STAFF_REPORT");
        LOGGER.info("Alice got back: ");
        aliceResults3.map(Object::toString).forEach(LOGGER::info);

        LOGGER.info("");
        LOGGER.info("Bob [ " + bob.toString() + " } is reading the Employee file with a purpose of DUTY_OF_CARE...");
        final Stream<Employee> bobResults1 = client.read(sourceFile, "Bob", "DUTY_OF_CARE");
        LOGGER.info("Bob got back: ");
        bobResults1.map(Object::toString).forEach(LOGGER::info);

        LOGGER.info("");
        LOGGER.info("Bob [ " + bob.toString() + " } is reading the Employee file with a purpose that is empty...");
        final Stream<Employee> bobResults2 = client.read(sourceFile, "Bob", "");
        LOGGER.info("Bob got back: ");
        bobResults2.map(Object::toString).forEach(LOGGER::info);

        LOGGER.info("");
        LOGGER.info("Eve [ " + eve.toString() + " } is reading the Employee file with a purpose that is empty...");
        final Stream<Employee> eveResults1 = client.read(sourceFile, "Eve", "");
        LOGGER.info("Eve got back: ");
        eveResults1.map(Object::toString).forEach(LOGGER::info);
    }
}
