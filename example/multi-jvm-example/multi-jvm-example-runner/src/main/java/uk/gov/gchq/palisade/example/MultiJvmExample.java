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

import io.etcd.jetcd.launcher.junit.EtcdClusterResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.cache.service.impl.EtcdBackingStore;
import uk.gov.gchq.palisade.cache.service.impl.SimpleCacheService;
import uk.gov.gchq.palisade.client.ConfiguredClientServices;
import uk.gov.gchq.palisade.config.service.ConfigurationService;
import uk.gov.gchq.palisade.config.service.impl.ProxyRestConfigService;
import uk.gov.gchq.palisade.data.service.impl.ProxyRestDataService;
import uk.gov.gchq.palisade.example.client.ExampleConfigurator;
import uk.gov.gchq.palisade.example.client.ExampleSimpleClient;
import uk.gov.gchq.palisade.resource.service.impl.ProxyRestResourceService;
import uk.gov.gchq.palisade.rest.ProxyRestConnectionDetail;
import uk.gov.gchq.palisade.service.impl.ProxyRestPalisadeService;
import uk.gov.gchq.palisade.service.impl.ProxyRestPolicyService;
import uk.gov.gchq.palisade.user.service.impl.ProxyRestUserService;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;

public class MultiJvmExample {
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiJvmExample.class);

    public static void main(final String[] args) throws Exception {
        if (args.length < 1) {
            System.out.printf("Usage: %s file\n", MultiJvmExample.class.getTypeName());
            System.out.println("\nfile\tfile containing serialised ExampleObj instances to read");
            System.exit(1);
        }

        String sourceFile = args[0];
        new MultiJvmExample().run(sourceFile);
    }

    public void run(final String sourceFile) throws Exception {
        EtcdClusterResource etcd = null;
        EtcdBackingStore store = null;
        try {
            etcd = new EtcdClusterResource("test-etcd", 1);
            etcd.cluster().start();
            List<String> etcdEndpointURLs = etcd.cluster().getClientEndpoints()
                    .stream()
                    .map(URI::toString)
                    .collect(Collectors.toList());
            store = new EtcdBackingStore().connectionDetails(etcdEndpointURLs);
            //this will write an initial configuration
            final ConfigurationService ics = ExampleConfigurator.setupMultiJVMConfigurationService(
                    new ProxyRestPolicyService("http://localhost:8081/policy"),
                    new ProxyRestUserService("http://localhost:8083/user"),
                    new ProxyRestResourceService("http://localhost:8082/resource"),
                    new ProxyRestPalisadeService("http://localhost:8080/palisade"),
                    new SimpleCacheService().backingStore(store),
                    new ProxyRestConfigService("http://localhost:8085/config"),
                    new ProxyRestConnectionDetail().url("http://localhost:8084/data").serviceClass(ProxyRestDataService.class)
            );
            final ConfiguredClientServices cs = new ConfiguredClientServices(ics);
            final ExampleSimpleClient client = new ExampleSimpleClient(cs, sourceFile);

            LOGGER.info("");
            LOGGER.info("Alice is reading file1...");
            final Stream<ExampleObj> aliceResults = client.read(sourceFile, "Alice", "Payroll");
            LOGGER.info("Alice got back: ");
            aliceResults.map(Object::toString).forEach(LOGGER::info);

            LOGGER.info("");
            LOGGER.info("Bob is reading file1...");
            final Stream<ExampleObj> bobResults = client.read(sourceFile, "Bob", "Payroll");
            LOGGER.info("Bob got back: ");
            bobResults.map(Object::toString).forEach(LOGGER::info);
        } finally {
            if (nonNull(etcd)) {
                etcd.cluster().close();
            }
            if (nonNull(store)) {
                store.close();
            }
        }
    }
}
