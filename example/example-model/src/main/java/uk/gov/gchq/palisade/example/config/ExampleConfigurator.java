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
package uk.gov.gchq.palisade.example.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.client.ClientConfiguredServices;
import uk.gov.gchq.palisade.config.service.ConfigConsts;
import uk.gov.gchq.palisade.config.service.ConfigurationService;
import uk.gov.gchq.palisade.example.common.ExamplePolicies;
import uk.gov.gchq.palisade.example.common.ExampleUsers;
import uk.gov.gchq.palisade.example.util.ExampleFileUtil;
import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.palisade.policy.service.request.SetResourcePolicyRequest;
import uk.gov.gchq.palisade.user.service.UserService;
import uk.gov.gchq.palisade.user.service.request.AddUserRequest;
import uk.gov.gchq.palisade.util.StreamUtil;

import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

/**
 * Convenience class for the examples to configure the users and data access policies for the example.
 */
public final class ExampleConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExampleConfigurator.class);
    private final String file;

    /**
     * Establishes policies and details for the examples and writes these into the configuration service.
     *
     * @param args command line arguments
     */
    public static void main(final String[] args) {
        final InputStream stream = StreamUtil.openStream(ExampleConfigurator.class, System.getenv(ConfigConsts.CONFIG_SERVICE_PATH));
        ConfigurationService configService = JSONSerialiser.deserialise(stream, ConfigurationService.class);
        ClientConfiguredServices cs = new ClientConfiguredServices(configService);
        new ExampleConfigurator(cs, args[0]);
    }

    public ExampleConfigurator(final ClientConfiguredServices services, final String file) {
        URI absoluteFileURI = ExampleFileUtil.convertToFileURI(file);
        this.file = absoluteFileURI.toString();
        initialiseExample(services);
    }

    private void initialiseExample(final ClientConfiguredServices services) {
        // The user authorisation owner or sys admin needs to add the user
        final UserService userService = services.getUserService();

        final CompletableFuture<Boolean> userAliceStatus = userService.addUser(
                new AddUserRequest().user(ExampleUsers.getAlice())
        );
        final CompletableFuture<Boolean> userBobStatus = userService.addUser(
                new AddUserRequest().user(ExampleUsers.getBob())
        );

        final CompletableFuture<Boolean> userEveStatus = userService.addUser(
                new AddUserRequest().user(ExampleUsers.getEve())
        );

        // Using Custom Rule implementations
        final SetResourcePolicyRequest customPolicies = ExamplePolicies.getExamplePolicy(file);

        final CompletableFuture<Boolean> policyStatus = services.getPolicyService().setResourcePolicy(
                customPolicies
        );
        // Wait for the users and policies to be loaded
        CompletableFuture.allOf(userAliceStatus, userBobStatus, userEveStatus, policyStatus).join();
        LOGGER.info("The example users and data access policies have been initialised.");
    }
}
