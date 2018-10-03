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

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import uk.gov.gchq.palisade.client.ConfiguredServices;
import uk.gov.gchq.palisade.config.service.InitialConfigurationService;
import uk.gov.gchq.palisade.config.service.request.GetConfigRequest;
import uk.gov.gchq.palisade.example.client.ExampleConfigurator;
import uk.gov.gchq.palisade.example.client.ExampleSimpleClient;
import uk.gov.gchq.palisade.service.request.InitialConfig;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.gchq.palisade.example.SingleJvmExample.CACHE_FILE;
import static uk.gov.gchq.palisade.example.SingleJvmExample.FILE;
import static uk.gov.gchq.palisade.example.SingleJvmExample.createDataPath;
import static uk.gov.gchq.palisade.util.JsonAssert.assertEquals;

public class SingleJvmExampleIT {

    private static InitialConfig config;

    @BeforeClass
    public static void createConfig() {
        ExampleConfigurator.setupSingleJVMConfigurationService(Paths.get(CACHE_FILE));
        final InitialConfigurationService ics = ExampleConfigurator.createConfigService(Paths.get(CACHE_FILE));
        //request the client configuration by not specifying a service
        config = ics.get(new GetConfigRequest()
                .service(Optional.empty()))
                .join();
    }

    @AfterClass
    public static void deleteFile() {
        FileUtils.deleteQuietly(new File(CACHE_FILE));
        FileUtils.deleteQuietly(new File(FILE));
    }

    @Before
    public void before() {
        createDataPath();
    }

    @Test
    public void shouldRunWithoutErrors() throws Exception {
        // Given
        final SingleJvmExample example = new SingleJvmExample();

        // When
        example.run();

        // Then - no exceptions
    }

    @Test
    public void shouldReadAsAlice() throws Exception {
        // Given
        final ConfiguredServices cs = new ConfiguredServices(config);
        final ExampleSimpleClient client = new ExampleSimpleClient(cs, FILE);

        // When
        final Stream<ExampleObj> aliceResults = client.read(FILE, "Alice", "Payroll");

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
        final ConfiguredServices cs = new ConfiguredServices(config);
        final ExampleSimpleClient client = new ExampleSimpleClient(cs, FILE);

        // When
        final Stream<ExampleObj> aliceResults = client.read(FILE, "Bob", "Payroll");

        // Then
        assertEquals(
                Arrays.asList(
                        new ExampleObj("redacted", "public", 20L),
                        new ExampleObj("redacted", "public", 20L)
                ),
                aliceResults.collect(Collectors.toList())
        );
    }
}
