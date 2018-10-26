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

import org.apache.hadoop.conf.Configuration;

import uk.gov.gchq.palisade.audit.service.AuditService;
import uk.gov.gchq.palisade.audit.service.impl.LoggerAuditService;
import uk.gov.gchq.palisade.cache.service.CacheService;
import uk.gov.gchq.palisade.cache.service.impl.EtcdBackingStore;
import uk.gov.gchq.palisade.cache.service.impl.HashMapBackingStore;
import uk.gov.gchq.palisade.cache.service.impl.SimpleCacheService;
import uk.gov.gchq.palisade.client.ConfiguredClientServices;
import uk.gov.gchq.palisade.config.service.InitialConfigurationService;
import uk.gov.gchq.palisade.config.service.impl.ProxyRestConfigService;
import uk.gov.gchq.palisade.config.service.impl.SimpleConfigService;
import uk.gov.gchq.palisade.config.service.request.AddConfigRequest;
import uk.gov.gchq.palisade.data.service.impl.ProxyRestDataService;
import uk.gov.gchq.palisade.data.service.impl.SimpleDataService;
import uk.gov.gchq.palisade.data.service.reader.HDFSDataReader;
import uk.gov.gchq.palisade.example.data.serialiser.ExampleObjSerialiser;
import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.palisade.policy.service.PolicyService;
import uk.gov.gchq.palisade.policy.service.impl.HierarchicalPolicyService;
import uk.gov.gchq.palisade.resource.service.HDFSResourceService;
import uk.gov.gchq.palisade.resource.service.ResourceService;
import uk.gov.gchq.palisade.resource.service.impl.ProxyRestResourceService;
import uk.gov.gchq.palisade.rest.ProxyRestConnectionDetail;
import uk.gov.gchq.palisade.service.PalisadeService;
import uk.gov.gchq.palisade.service.Service;
import uk.gov.gchq.palisade.service.impl.ProxyRestPalisadeService;
import uk.gov.gchq.palisade.service.impl.ProxyRestPolicyService;
import uk.gov.gchq.palisade.service.impl.SimplePalisadeService;
import uk.gov.gchq.palisade.service.request.ConnectionDetail;
import uk.gov.gchq.palisade.service.request.InitialConfig;
import uk.gov.gchq.palisade.service.request.SimpleConnectionDetail;
import uk.gov.gchq.palisade.user.service.UserService;
import uk.gov.gchq.palisade.user.service.impl.HashMapUserService;
import uk.gov.gchq.palisade.user.service.impl.ProxyRestUserService;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Convenience class for the examples to configure the config creation service. This is used to create an entire
 * configuration for Palisade for the examples from scratch.
 */
public final class ExampleConfigurator {

    protected static final String RESOURCE_TYPE = "exampleObj";

    private ExampleConfigurator() {
    }

    /**
     * Allows the creation of some configuration data for the single JVM example.
     *
     * @return the configuration service that provides the entry to Palisade
     */
    public static InitialConfigurationService setupSingleJVMConfigurationService() {
        CacheService cache = new SimpleCacheService().backingStore(new HashMapBackingStore(true));
        InitialConfigurationService configService = new SimpleConfigService(cache);
        //configure the single JVM settings
        PolicyService policy = new HierarchicalPolicyService().cacheService(cache);
        UserService user = createClientUserService(configService, cache);
        AuditService audit = createAuditService(configService, cache);
        SimplePalisadeService palisade = new SimplePalisadeService()
                .auditService(audit)
                .policyService(policy)
                .userService(user)
                .cacheService(cache);

        HDFSResourceService resource = createSingleJVMResourceService(configService, cache, palisade);

        //write each of these to the initial config
        Collection<Service> services = Stream.of(audit, user, resource).collect(Collectors.toList());
        return writeClientConfiguration(configService, services, new LegacyPair(PolicyService.class, policy), new LegacyPair(CacheService.class, cache), new LegacyPair(PalisadeService.class, palisade));
    }

    //TODO: remove this once gh-129 done
    static class LegacyPair {
        public final Service service;
        public final Class<? extends Service> clazz;

        LegacyPair(final Class<? extends Service> clazz, final Service service) {
            this.service = service;
            this.clazz = clazz;
        }
    }

    /**
     * Create an {@link InitialConfig} for the client and insert have each of the services write their configuration
     * into the service.
     *
     * @param configService  the configuration service
     * @param services       collection of services to write to the configuration service
     * @param legacyServices (TODO remove once gh-129 done) service that don't yet use the config service
     * @return the {@code configService} argument
     */
    private static InitialConfigurationService writeClientConfiguration(final InitialConfigurationService configService, final Collection<Service> services, final LegacyPair... legacyServices) {
        InitialConfig initial = new InitialConfig();

        //deal with the legacy services (TODO: to be deleted once gh-129 closed)
        for (LegacyPair s : legacyServices) {
            initial.put(s.clazz.getTypeName(), s.service.getClass().getTypeName())
                    .put(s.clazz.getTypeName() + ConfiguredClientServices.STATE, new String(JSONSerialiser.serialise(s.service)));
        }

        //each service to write their configuration into the initial configuration
        services.forEach(service -> service.writeConfiguration(initial));

        //insert this into the cache manually so it can be created later
        configService.add((AddConfigRequest) new AddConfigRequest()
                .config(initial)
                .service(Optional.empty())).join();
        return configService;
    }

    /**
     * Makes a user service.
     *
     * @param configService the configuration service
     * @param cacheService  the cache service the user service should use
     * @return the user service
     */
    private static UserService createClientUserService(final InitialConfigurationService configService, final CacheService cacheService) {
        return new HashMapUserService().cacheService(cacheService);
    }

    /**
     * Makes an audit service.
     *
     * @param configService the configuration service
     * @param cacheService  the cache service
     * @return the audit service
     */
    private static AuditService createAuditService(final InitialConfigurationService configService, final CacheService cacheService) {
        return new LoggerAuditService();
    }

    /**
     * Create resource service for the single JVM example. This method will configure the service as well with example
     * details.
     *
     * @param configService the Palisade configuration service
     * @param cache         the Palisade cache service
     * @param palisade      the Palisade service
     * @return a configured resource service
     */
    private static HDFSResourceService createSingleJVMResourceService(final InitialConfigurationService configService, final CacheService cache, final SimplePalisadeService palisade) {
        HDFSResourceService resource;
        HDFSDataReader reader;
        try {
            resource = new HDFSResourceService().conf(new Configuration()).cacheService(cache);
            reader = new HDFSDataReader().conf(new Configuration());
            reader.addSerialiser(ExampleConfigurator.RESOURCE_TYPE, new ExampleObjSerialiser());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //tell the palisade service about this resource service
        palisade.resourceService(resource);
        //write the connection details to the resource service, this will cause it to write these to the cache
        final Map<String, ConnectionDetail> dataType = new HashMap<>();
        dataType.put(ExampleConfigurator.RESOURCE_TYPE, new SimpleConnectionDetail().service(new SimpleDataService().palisadeService(palisade).reader(reader)));
        resource.connectionDetail(null, dataType);
        return resource;
    }

    /**
     * Sets the resource service configuration up that will be retrieved by the resource service from the configuration
     * service.
     *
     * @param configService  the Palisade configuration service
     * @param cache          the cache service to connect this resource service to outside of a container
     * @param containerCache the cache to use once running containerised
     * @return the configured resource service
     */
    private static HDFSResourceService createMultiJVMResourceService(final InitialConfigurationService configService, final CacheService cache, final Optional<CacheService> containerCache) {
        try {
            HDFSResourceService resourceServer = new HDFSResourceService(new Configuration(), cache);
            //set up the example object type
            final Map<String, ConnectionDetail> dataType = new HashMap<>();
            dataType.put(RESOURCE_TYPE, new ProxyRestConnectionDetail().url("http://localhost:8084/data").serviceClass(ProxyRestDataService.class));
            resourceServer.connectionDetail(null, dataType);
            //switch to containerised cache
            containerCache.ifPresent(resourceServer::setCacheService);
            writeConfiguration(configService, resourceServer, ResourceService.class);

            return resourceServer;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Write the given configuration to the configuration manager from the service.
     *
     * @param configService the configuration service to write to
     * @param service       the service that will write its configuration
     * @param serviceClass  the type of Palisade service
     */
    private static void writeConfiguration(final InitialConfigurationService configService, final Service service, final Class<? extends Service> serviceClass) {
        InitialConfig config = new InitialConfig();
        service.writeConfiguration(config);
        configService.add((AddConfigRequest) new AddConfigRequest()
                .config(config)
                .service(Optional.of(serviceClass))).join();
    }

    /**
     * Allows the bootstrapping of some configuration data for the multi-JVM examples. This can be used by the multi JVM
     * examples, including the containerised examples. If no etcd endpoints are given, then a hashmap cache backing
     * store is created and services are told to use that. This is useful in the integration tests. If an endpoint is
     * given, then this will be used instead. The {@code containerisedCache} argument is the cache service details that
     * will be put into the configuration given to various services when they startup. This may need to be different to
     * the cache used on the client side, due to changes in how containers contact other containers, e.g. different host
     * names in network URLs.
     *
     * @param etcdEndpoints      the list of etcd end points, if empty then a HashMap backing store is used
     * @param containerisedCache the {@link CacheService} that services should be told to use once they start
     *                           (potentially inside containers)
     * @return the configuration service to provide the Palisade entry point
     */
    public static InitialConfigurationService setupMultiJVMConfigurationService(final List<String> etcdEndpoints, final Optional<CacheService> containerisedCache) {
        requireNonNull(etcdEndpoints, "etcd endpoints can not be null");
        requireNonNull(containerisedCache, "containerisedCache can not be null");

        ProxyRestConfigService configService = new ProxyRestConfigService("http://localhost:8085/config");

        //if there are no endpoints, assume we are using a HashMap backing store
        SimpleCacheService cache = new SimpleCacheService();
        if (etcdEndpoints.isEmpty()) {
            cache.backingStore(new HashMapBackingStore(true));
        } else {
            EtcdBackingStore etcdStore = new EtcdBackingStore().connectionDetails(etcdEndpoints);
            cache.backingStore(etcdStore);
        }

        AuditService audit = createAuditService(configService, cache);

        PalisadeService palisade = new ProxyRestPalisadeService("http://localhost:8080/palisade");
        PolicyService policy = new ProxyRestPolicyService("http://localhost:8081/policy");
        ResourceService resource = new ProxyRestResourceService("http://localhost:8082/resource");
        UserService user = new ProxyRestUserService("http://localhost:8083/user");

        Collection<Service> services = Stream.of(audit, user, resource).collect(Collectors.toList());
        writeClientConfiguration(configService, services, new LegacyPair(PolicyService.class, policy), new LegacyPair(CacheService.class, cache), new LegacyPair(PalisadeService.class, palisade));

        writeConfiguration(configService, createClientUserService(configService, cache), UserService.class);
        createMultiJVMResourceService(configService, cache, containerisedCache);
        //TODO: must manually close this connection
        return configService;
    }
}
