///*
// * Copyright 2018 Crown Copyright
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package uk.gov.gchq.palisade.example;
//
//import io.etcd.jetcd.launcher.junit.EtcdClusterResource;
//import org.apache.commons.io.FileUtils;
//import org.junit.After;
//import org.junit.AfterClass;
//import org.junit.Before;
//import org.junit.BeforeClass;
//import org.junit.Test;
//
//import uk.gov.gchq.palisade.client.ConfiguredClientServices;
//import uk.gov.gchq.palisade.config.service.ConfigurationService;
//import uk.gov.gchq.palisade.config.service.impl.RestConfigServiceV1;
//import uk.gov.gchq.palisade.example.client.ExampleSimpleClient;
//import uk.gov.gchq.palisade.example.config.DistributedServicesConfigurator;
//import uk.gov.gchq.palisade.example.config.ExampleConfigurator;
//import uk.gov.gchq.palisade.example.util.ExampleFileUtil;
//import uk.gov.gchq.palisade.rest.EmbeddedHttpServer;
//import uk.gov.gchq.palisade.rest.RestUtil;
//
//import java.io.File;
//import java.io.IOException;
//import java.net.URI;
//import java.nio.file.Files;
//import java.util.Arrays;
//import java.util.concurrent.CompletionException;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;
//import static org.junit.Assert.fail;
//
//public class MultiJvmExampleIT {
//
//    public static final String TEST_FILE = "/test_ExampleObj.txt";
//    public static String TEMP_DESTINATION;
//
//    private static EmbeddedHttpServer palisadeServer;
//    private static EmbeddedHttpServer policyServer;
//    private static EmbeddedHttpServer resourceServer;
//    private static EmbeddedHttpServer userServer;
//    private static EmbeddedHttpServer dataServer;
//    private static EmbeddedHttpServer configServer;
//
//    private static ConfigurationService configService;
//
//    private static EtcdClusterResource etcd = null;
//
//    @BeforeClass
//    public static void beforeClass() throws IOException {
//        try {
//            etcd = new EtcdClusterResource("test-etcd", 1);
//            etcd.cluster().start();
//            String etcdEndpointURLs = etcd.cluster().getClientEndpoints()
//                    .stream()
//                    .map(URI::toString)
//                    .reduce((a, b) -> a + "," + b)
//                    .orElse(null);
//
//            System.setProperty(RestConfigServiceV1.BOOTSTRAP_CONFIG, "bootstrapConfig.json");
//            configServer = new EmbeddedHttpServer("http://localhost:8085/config/v1", new uk.gov.gchq.palisade.config.service.impl.ApplicationConfigV1());
//            configServer.startServer();
//
//            // write the initial configuration for the services to bootstrap from
//            DistributedServicesConfigurator.main(new String[]{etcdEndpointURLs, "http://localhost:8080/palisade", "http://localhost:8081/policy", "http://localhost:8082/resource", "http://localhost:8083/user", "http://localhost:8084/data", "http://localhost:8085/config"});
//            // initialise the user and data access policies required for this example
//            System.setProperty(RestUtil.CONFIG_SERVICE_PATH, "configRest.json");
//            configService = RestUtil.createService(ExampleSimpleClient.class, "configRest.json", ConfigurationService.class);
//            final ConfiguredClientServices cs = new ConfiguredClientServices(configService);
//            new ExampleConfigurator(cs, TEST_FILE);
//
//            palisadeServer = new EmbeddedHttpServer("http://localhost:8080/palisade/v1", new uk.gov.gchq.palisade.service.impl.ApplicationConfigV1());
//            palisadeServer.startServer();
//
//            policyServer = new EmbeddedHttpServer("http://localhost:8081/policy/v1", new uk.gov.gchq.palisade.policy.service.impl.ApplicationConfigV1());
//            policyServer.startServer();
//
//            resourceServer = new EmbeddedHttpServer("http://localhost:8082/resource/v1", new uk.gov.gchq.palisade.resource.service.impl.ApplicationConfigV1());
//            resourceServer.startServer();
//
//            userServer = new EmbeddedHttpServer("http://localhost:8083/user/v1", new uk.gov.gchq.palisade.user.service.impl.ApplicationConfigV1());
//            userServer.startServer();
//
//            dataServer = new EmbeddedHttpServer("http://localhost:8084/data/v1", new uk.gov.gchq.palisade.data.service.impl.ApplicationConfigV1());
//            dataServer.startServer();
//        } catch (final Exception e) {
//            e.printStackTrace();
//            if (etcd != null) {
//                etcd.cluster().close();
//            }
//        }
//    }
//
//    @AfterClass
//    public static void afterClass() {
//        if (null != palisadeServer) {
//            palisadeServer.stopServer();
//        }
//        if (null != policyServer) {
//            policyServer.stopServer();
//        }
//        if (null != resourceServer) {
//            resourceServer.stopServer();
//        }
//        if (null != userServer) {
//            userServer.stopServer();
//        }
//        if (null != dataServer) {
//            dataServer.stopServer();
//        }
//        if (null != dataServer) {
//            configServer.stopServer();
//        }
//        if (null != etcd) {
//            etcd.cluster().close();
//        }
//    }
//
//    @Before
//    public void before() throws Exception {
//        TEMP_DESTINATION = Files.createTempFile("exampleObj_", ".txt").toAbsolutePath().toString();
//        ExampleFileUtil.createDataPath(TEST_FILE, TEMP_DESTINATION, MultiJvmExampleIT.class);
//    }
//
//    @After
//    public void after() {
//        FileUtils.deleteQuietly(new File(TEMP_DESTINATION));
//    }
//
//    @Test
//    public void shouldRunWithoutErrors() throws Exception {
//        // Given
//        final MultiJvmExample example = new MultiJvmExample();
//
//        // When
//        example.run(TEMP_DESTINATION);
//
//        // Then - no exceptions
//    }
//
//    @Test
//    public void shouldReadAsAlice() throws Exception {
//        // Given
//        final ConfiguredClientServices cs = new ConfiguredClientServices(configService);
//        final ExampleSimpleClient client = new ExampleSimpleClient(cs, TEMP_DESTINATION);
//
//        // When
//        final Stream<ExampleObj> aliceResults = client.read(TEMP_DESTINATION, "Alice", "Payroll");
//
//        // Then
//        assertEquals(
//                Arrays.asList(
//                        new ExampleObj("item1c", "public", 20L),
//                        new ExampleObj("item1d", "private", 20L),
//                        new ExampleObj("item2c", "public", 20L),
//                        new ExampleObj("item2d", "private", 20L)
//                ),
//                aliceResults.collect(Collectors.toList())
//        );
//    }
//
//    @Test
//    public void shouldReadAsBob() throws Exception {
//        // Given
//        final ConfiguredClientServices cs = new ConfiguredClientServices(configService);
//        final ExampleSimpleClient client = new ExampleSimpleClient(cs, TEMP_DESTINATION);
//
//        // When
//        final Stream<ExampleObj> aliceResults = client.read(TEMP_DESTINATION, "Bob", "Payroll");
//
//        // Then
//        assertEquals(
//                Arrays.asList(
//                        new ExampleObj("redacted", "public", 20L),
//                        new ExampleObj("redacted", "public", 20L)
//                ),
//                aliceResults.collect(Collectors.toList())
//        );
//    }
//
//    @Test
//    public void proxyServiceShouldReturnActualExceptionThrownByUnderlyingService() throws Exception {
//        // Given
//        final ConfiguredClientServices cs = new ConfiguredClientServices(configService);
//        final ExampleSimpleClient client = new ExampleSimpleClient(cs, TEMP_DESTINATION);
//
//        // When / Then
//        try {
//            client.read("badscheme:///unknown_file/stuff", "Bob", "Payroll");
//            fail("Exception expected");
//        } catch (final CompletionException e) {
//            assertTrue("CompletionException cause should be an UnsupportedOperationException",
//                    e.getCause() instanceof UnsupportedOperationException);
//            assertTrue(e.getCause().getMessage(), e.getCause().getMessage().contains("resource ID is out of scope"));
//        } catch (final UnsupportedOperationException e) {
//            assertTrue(e.getMessage(), e.getMessage().contains("resource ID is out of scope"));
//        }
//    }
//}
