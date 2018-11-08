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
import uk.gov.gchq.palisade.cache.service.impl.BackingStore;
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
import uk.gov.gchq.palisade.user.service.impl.ProxyRestUserService;
import uk.gov.gchq.palisade.user.service.impl.SimpleUserService;

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
        CacheService cache = createCacheService(new HashMapBackingStore(true));
        InitialConfigurationService configService = new SimpleConfigService(cache);
        //configure the single JVM settings
        PolicyService policy = createPolicyService(configService, cache);
        UserService user = createUserService(configService, cache);
        AuditService audit = createAuditService(configService, cache);
        SimplePalisadeService palisade = createPalisadeService(configService, cache, policy, user, audit);

        HDFSResourceService resource;
        try {
            HDFSDataReader reader = new HDFSDataReader().conf(new Configuration());
            reader.addSerialiser(RESOURCE_TYPE, new ExampleObjSerialiser());
            resource = createResourceService(configService, cache);
            palisade.resourceService(resource);
            configureResourceConnectionDetails(resource, new SimpleConnectionDetail().service(new SimpleDataService().palisadeService(palisade).reader(reader)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //write each of these to the initial config
        Collection<Service> services = Stream.of(audit, user, resource, policy, palisade, cache).collect(Collectors.toList());
        return writeClientConfiguration(configService, services);
    }

    /**
     * Set up the example connection details in the resource service.
     *
     * @param resource             the resource service
     * @param exampleObjConnection connection details for the example resource
     */
    private static void configureResourceConnectionDetails(final HDFSResourceService resource, final ConnectionDetail exampleObjConnection) {
        final Map<String, ConnectionDetail> dataType = new HashMap<>();
        dataType.put(RESOURCE_TYPE, exampleObjConnection);
        resource.connectionDetail(null, dataType);
    }

    /**
     * TODO: remove this once gh-129 done
     */
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
        services.forEach(service -> service.recordCurrentConfigTo(initial));

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
    private static SimpleUserService createUserService(final InitialConfigurationService configService, final CacheService cacheService) {
        return new SimpleUserService().cacheService(cacheService);
    }

    /**
     * Makes a Palisade service.
     *
     * @param configService the configuration service this service can use
     * @param cache         the cache service this service should use
     * @param policy        the policy service this service should use
     * @param user          the user service this service should use
     * @param audit         the audit service this service should use
     * @return the Palisade service
     */
    private static SimplePalisadeService createPalisadeService(final InitialConfigurationService configService, final CacheService cache, final PolicyService policy, final UserService user, final AuditService audit) {
        return new SimplePalisadeService()
                .auditService(audit)
                .policyService(policy)
                .userService(user)
                .cacheService(cache);
    }

    /**
     * Makes a policy service.
     *
     * @param configService the configuration service
     * @param cacheService  the cache service the user service should use
     * @return the policy service
     */
    private static HierarchicalPolicyService createPolicyService(final InitialConfigurationService configService, final CacheService cacheService) {
        return new HierarchicalPolicyService().cacheService(cacheService);
    }

    /**
     * Makes an audit service.
     *
     * @param configService the configuration service
     * @param cacheService  the cache service
     * @return the audit service
     */
    private static LoggerAuditService createAuditService(final InitialConfigurationService configService, final CacheService cacheService) {
        return new LoggerAuditService();
    }

    /**
     * Makes a cache service.
     *
     * @param store the backing store
     * @return the cache service
     */
    private static SimpleCacheService createCacheService(final BackingStore store) {
        return new SimpleCacheService().backingStore(store);
    }

    /**
     * Makes a resource service.
     *
     * @param configService the Palisade configuration service
     * @param cache         the Palisade cache service
     * @return a configured resource service
     */
    private static HDFSResourceService createResourceService(final InitialConfigurationService configService, final CacheService cache) {
        try {
            HDFSResourceService resource = new HDFSResourceService().conf(new Configuration()).cacheService(cache);
            return resource;
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
        service.recordCurrentConfigTo(config);
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
     * @param etcdEndpoints     the list of etcd end points, if empty then a HashMap backing store is used
     * @param containerAudit    the {@link AuditService} that services should be told to use once they start
     * @param containerPolicy   the {@link PolicyService}  that services should be told to use once they start
     * @param containerResource the {@link ResourceService}  that services should be told to use once they start
     * @param containerUser     the {@link  UserService}  that services should be told to use once they start
     * @param containerCache    the {@link CacheService} that services should be told to use once they start
     *                          (potentially inside containers)
     * @return the configuration service to provide the Palisade entry point
     */
    public static InitialConfigurationService setupMultiJVMConfigurationService(final List<String> etcdEndpoints,
                                                                                final Optional<AuditService> containerAudit,
                                                                                final Optional<PolicyService> containerPolicy,
                                                                                final Optional<UserService> containerUser,
                                                                                final Optional<ResourceService> containerResource,
                                                                                final Optional<CacheService> containerCache) {
        requireNonNull(etcdEndpoints, "etcdEndpoints can not be null");
        requireNonNull(containerAudit, "containerAudit can not be null");
        requireNonNull(containerPolicy, "containerPolicy can not be null");
        requireNonNull(containerUser, "containerUser can not be null");
        requireNonNull(containerResource, "containerResource can not be null");
        requireNonNull(containerCache, "containerCache can not be null");

        EtcdBackingStore etcdStore = null;
        try {
            ProxyRestConfigService configService = new ProxyRestConfigService("http://localhost:8085/config");
            //if there are no endpoints, assume we are using a HashMap backing store
            BackingStore store;
            if (etcdEndpoints.isEmpty()) {
                store = new HashMapBackingStore(true);
            } else {
                etcdStore = new EtcdBackingStore().connectionDetails(etcdEndpoints);
                store = etcdStore;
            }

            SimpleCacheService cache = createCacheService(store);
            AuditService audit = createAuditService(configService, cache);
            PalisadeService palisade = new ProxyRestPalisadeService("http://localhost:8080/palisade");
            PolicyService policy = new ProxyRestPolicyService("http://localhost:8081/policy");
            ResourceService resource = new ProxyRestResourceService("http://localhost:8082/resource");
            UserService user = new ProxyRestUserService("http://localhost:8083/user");

            Collection<Service> services = Stream.of(audit, user, resource, policy, palisade, cache).collect(Collectors.toList());
            writeClientConfiguration(configService, services);

            //now populate cache with details for services to start up

            SimpleUserService remoteUser = createUserService(configService, cache);
            containerCache.ifPresent(remoteUser::setCacheService);
            writeConfiguration(configService, remoteUser, UserService.class);

            SimplePalisadeService remotePalisade = createPalisadeService(configService, cache, policy, user, audit);
            remotePalisade.resourceService(resource);
            containerAudit.ifPresent(remotePalisade::setAuditService);
            containerPolicy.ifPresent(remotePalisade::setPolicyService);
            containerUser.ifPresent(remotePalisade::setUserService);
            containerResource.ifPresent(remotePalisade::setResourceService);
            containerCache.ifPresent(remotePalisade::setCacheService);
            writeConfiguration(configService, remotePalisade, PalisadeService.class);

            HierarchicalPolicyService remotePolicy = createPolicyService(configService, cache);
            containerCache.ifPresent(remotePolicy::setCacheService);
            writeConfiguration(configService, remotePolicy, PolicyService.class);

            HDFSResourceService remoteResource = createResourceService(configService, cache);
            configureResourceConnectionDetails(remoteResource, new ProxyRestConnectionDetail().url("http://localhost:8084/data").serviceClass(ProxyRestDataService.class));
            containerCache.ifPresent(remoteResource::setCacheService);
            writeConfiguration(configService, remoteResource, ResourceService.class);

            //since the InitialConfigurationService will have already started, there is no extra configuration to
            //write for it, so there is no writeConfiguration call for the config. service

            return configService;
        } finally {
            if (etcdStore != null) {
                etcdStore.close();
            }
        }
    }
}
