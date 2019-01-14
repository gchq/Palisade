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

import uk.gov.gchq.palisade.cache.service.CacheService;
import uk.gov.gchq.palisade.cache.service.impl.EtcdBackingStore;
import uk.gov.gchq.palisade.cache.service.impl.SimpleCacheService;
import uk.gov.gchq.palisade.client.ConfiguredClientServices;
import uk.gov.gchq.palisade.config.service.ConfigurationService;
import uk.gov.gchq.palisade.config.service.impl.ProxyRestConfigService;
import uk.gov.gchq.palisade.config.service.request.AddConfigRequest;
import uk.gov.gchq.palisade.data.service.impl.ProxyRestDataService;
import uk.gov.gchq.palisade.example.client.ExampleConfigurator;
import uk.gov.gchq.palisade.example.client.ExampleSimpleClient;
import uk.gov.gchq.palisade.resource.service.impl.ProxyRestResourceService;
import uk.gov.gchq.palisade.rest.ProxyRestConnectionDetail;
import uk.gov.gchq.palisade.service.impl.ProxyRestPalisadeService;
import uk.gov.gchq.palisade.service.impl.ProxyRestPolicyService;
import uk.gov.gchq.palisade.service.request.ConnectionDetail;
import uk.gov.gchq.palisade.service.request.ServiceConfiguration;
import uk.gov.gchq.palisade.user.service.impl.ProxyRestUserService;

import java.io.File;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

public class MultiJVMDockerExample {
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiJVMDockerExample.class);
    //For the Docker build, this will have been created in the container before this executes
    protected static final String FILE = new File("/data/exampleObj_file1.txt").getAbsolutePath();

    public static void main(final String[] args) throws Exception {
        new MultiJVMDockerExample().run();
    }

    public void run() throws Exception {
        EtcdBackingStore store = null;
        try {
            store = new EtcdBackingStore().connectionDetails(Collections.singletonList("http://etcd:2379"));
            SimpleCacheService cache = new SimpleCacheService().backingStore(store);
            run(Optional.empty(), new ProxyRestConfigService("http://config-service:8080/config"),
                    new ProxyRestConnectionDetail().url("http://data-service:8080/data").serviceClass(ProxyRestDataService.class),
                    cache, cache);
        } finally {
            if (nonNull(store)) {
                store.close();
            }
        }
    }

    void run(final Optional<ServiceConfiguration> clientConfig, final ConfigurationService configService, final ConnectionDetail dataServiceConnection,
             final CacheService dockerCacheService, final CacheService localCacheService) throws Exception {
        requireNonNull(clientConfig, "clientConfig");
        requireNonNull(configService, "configService");
        requireNonNull(dataServiceConnection, "dataServiceConnection");
        requireNonNull(dockerCacheService, "dockerCacheService");
        requireNonNull(localCacheService, "localCacheService");
        //this will write an initial configuration
        ExampleConfigurator.setupMultiJVMConfigurationService(
                new ProxyRestPolicyService("http://policy-service:8080/policy"),
                new ProxyRestUserService("http://user-service:8080/user"),
                new ProxyRestResourceService("http://resource-service:8080/resource"),
                new ProxyRestPalisadeService("http://palisade-service:8080/palisade"),
                dockerCacheService,
                configService,
                dataServiceConnection,
                localCacheService
        );
        //override the client configuration
        clientConfig.ifPresent(config -> configService.add((AddConfigRequest) new AddConfigRequest()
                .config(config)
                .service(Optional.empty()))
                .join()
        );
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
