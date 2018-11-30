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
import org.junit.Test;

import uk.gov.gchq.palisade.client.ConfiguredClientServices;
import uk.gov.gchq.palisade.config.service.ConfigurationService;
import uk.gov.gchq.palisade.example.client.ExampleConfigurator;
import uk.gov.gchq.palisade.example.client.ExampleUtils;
import uk.gov.gchq.palisade.example.client.ExampleSimpleClient;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.gchq.palisade.example.SingleJvmExample.DESTINATION;
import static uk.gov.gchq.palisade.util.JsonAssert.assertEquals;

public class SingleJvmExampleIT {

    public static final String TEST_FILE = "/test_ExampleObj.txt";
    private static ConfigurationService configService;

    @BeforeClass
    public static void createConfig() {
        configService = ExampleConfigurator.setupSingleJVMConfigurationService();
    }

    @AfterClass
    public static void deleteFile() {
        FileUtils.deleteQuietly(new File(DESTINATION));
    }

    @Before
    public void before() {
        ExampleUtils.createDataPath(TEST_FILE, DESTINATION, SingleJvmExample.class);
    }

    @Test
    public void shouldRunWithoutErrors() throws Exception {
        // Given
        final SingleJvmExample example = new SingleJvmExample();

        // When
        example.run(TEST_FILE);

        // Then - no exceptions
    }

    @Test
    public void shouldReadAsAlice() throws Exception {
        // Given
        final ConfiguredClientServices cs = new ConfiguredClientServices(configService);
        final ExampleSimpleClient client = new ExampleSimpleClient(cs, DESTINATION);

        // When
        final Stream<ExampleObj> aliceResults = client.read(DESTINATION, "Alice", "Payroll");

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
        final ConfiguredClientServices cs = new ConfiguredClientServices(configService);
        final ExampleSimpleClient client = new ExampleSimpleClient(cs, DESTINATION);

        // When
        final Stream<ExampleObj> aliceResults = client.read(DESTINATION, "Bob", "Payroll");

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
