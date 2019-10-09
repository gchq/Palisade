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

package uk.gov.gchq.palisade.example.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.client.ClientUtil;
import uk.gov.gchq.palisade.client.SimpleClient;
import uk.gov.gchq.palisade.data.serialise.AvroSerialiser;
import uk.gov.gchq.palisade.example.common.ExampleUsers;
import uk.gov.gchq.palisade.example.config.ServicesConfigurator;
import uk.gov.gchq.palisade.example.hrdatagenerator.types.Employee;
import uk.gov.gchq.palisade.example.runner.RestExample;
import uk.gov.gchq.palisade.example.util.ExampleFileUtil;
import uk.gov.gchq.palisade.service.PalisadeService;

import java.net.URI;
import java.util.stream.Stream;

public class ExampleSimpleClient extends SimpleClient<Employee> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestExample.class);

    public ExampleSimpleClient(final PalisadeService palisadeService) {
        super(palisadeService, new AvroSerialiser<>(Employee.class));
    }

    public static void main(final String[] args) throws InterruptedException {
        if (args.length == 3) {

            String userId = args[0];
            String filename = args[1];
            String purpose = args[2];
            PalisadeService palisade = ClientUtil.getPalisadeClientEntryPoint();
            User user = ExampleUsers.getUser(userId);
            LOGGER.info(user.getUserId().toString() + " is reading the Employee file with a purpose of " + purpose);
            final Stream<Employee> results = new ExampleSimpleClient(palisade).read(filename, user.getUserId().getId(), purpose);
            LOGGER.info(user.getUserId().toString() + " got back: ");
            results.map(Object::toString).forEach(LOGGER::info);

        } else {
            System.out.printf("Usage: %s userId resource purpose\n\n", ExampleSimpleClient.class.getSimpleName());
            System.out.println("userId\t\t the unique id of the user making this query");
            System.out.println("resource\t the name of the resource being requested");
            System.out.println("purpose\t\t purpose for accessing the resource");
        }
    }

    public Stream<Employee> read(final String filename, final String userId, final String purpose) {
        URI absoluteFileURI = ExampleFileUtil.convertToFileURI(filename);
        String absoluteFile = absoluteFileURI.toString();
        return super.read(absoluteFile, ServicesConfigurator.RESOURCE_TYPE, userId, purpose);
    }

}
