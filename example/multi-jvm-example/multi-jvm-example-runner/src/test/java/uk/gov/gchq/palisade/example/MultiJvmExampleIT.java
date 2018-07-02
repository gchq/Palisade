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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.gov.gchq.palisade.data.service.impl.RestDataServiceV1;
import uk.gov.gchq.palisade.example.client.ExampleSimpleRestClient;
import uk.gov.gchq.palisade.policy.service.impl.RestPolicyServiceV1;
import uk.gov.gchq.palisade.resource.service.impl.RestResourceServiceV1;
import uk.gov.gchq.palisade.rest.EmbeddedHttpServer;
import uk.gov.gchq.palisade.service.impl.RestPalisadeServiceV1;
import uk.gov.gchq.palisade.user.service.impl.RestUserServiceV1;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class MultiJvmExampleIT {

    private static EmbeddedHttpServer palisadeServer;
    private static EmbeddedHttpServer policyServer;
    private static EmbeddedHttpServer resourceServer;
    private static EmbeddedHttpServer userServer;
    private static EmbeddedHttpServer dataServer;

    @BeforeClass
    public static void beforeClass() throws IOException {
        System.setProperty(RestPalisadeServiceV1.SERVICE_CONFIG, "palisadeConfig.json");
        palisadeServer = new EmbeddedHttpServer("http://localhost:8080/palisade/v1", new uk.gov.gchq.palisade.service.impl.ApplicationConfigV1());
        palisadeServer.startServer();

        System.setProperty(RestPolicyServiceV1.SERVICE_CONFIG, "policyConfig.json");
        policyServer = new EmbeddedHttpServer("http://localhost:8081/policy/v1", new uk.gov.gchq.palisade.policy.service.impl.ApplicationConfigV1());
        policyServer.startServer();

        System.setProperty(RestResourceServiceV1.SERVICE_CONFIG, "resourceConfig.json");
        resourceServer = new EmbeddedHttpServer("http://localhost:8082/resource/v1", new uk.gov.gchq.palisade.resource.service.impl.ApplicationConfigV1());
        resourceServer.startServer();

        System.setProperty(RestUserServiceV1.SERVICE_CONFIG, "userConfig.json");
        userServer = new EmbeddedHttpServer("http://localhost:8083/user/v1", new uk.gov.gchq.palisade.user.service.impl.ApplicationConfigV1());
        userServer.startServer();

        System.setProperty(RestDataServiceV1.SERVICE_CONFIG, "dataConfig.json");
        dataServer = new EmbeddedHttpServer("http://localhost:8084/data/v1", new uk.gov.gchq.palisade.data.service.impl.ApplicationConfigV1());
        dataServer.startServer();
    }

    @AfterClass
    public static void afterClass() {
        if (null != palisadeServer) {
            palisadeServer.stopServer();
        }
        if (null != policyServer) {
            policyServer.stopServer();
        }
        if (null != resourceServer) {
            resourceServer.stopServer();
        }
        if (null != userServer) {
            userServer.stopServer();
        }
        if (null != dataServer) {
            dataServer.stopServer();
        }
    }

    @Test
    public void shouldRunWithoutErrors() throws Exception {
        // Given
        final MultiJvmExample example = new MultiJvmExample();

        // When
        example.run();

        // Then - no exceptions
    }

    @Test
    public void shouldReadAsAlice() throws Exception {
        // Given
        final ExampleSimpleRestClient client = new ExampleSimpleRestClient();

        // When
        final Stream<ExampleObj> aliceResults = client.read("file1", "Alice", "Payroll");

        // Then
        assertEquals(
                Arrays.asList(
                        new ExampleObj("item1c", "public", 20L),
                        new ExampleObj("item1d", "private", 20L),
                        new ExampleObj("item2c", "public", 20L),
                        new ExampleObj("item2d", "private", 20L)
                ),
                aliceResults.collect(Collectors.toList())
        );
    }

    @Test
    public void shouldReadAsBob() throws Exception {
        // Given
        final ExampleSimpleRestClient client = new ExampleSimpleRestClient();

        // When
        final Stream<ExampleObj> aliceResults = client.read("file1", "Bob", "Payroll");

        // Then
        assertEquals(
                Arrays.asList(
                        new ExampleObj("item1c", "public", 20L),
                        new ExampleObj("item2c", "public", 20L)
                ),
                aliceResults.collect(Collectors.toList())
        );
    }
}
