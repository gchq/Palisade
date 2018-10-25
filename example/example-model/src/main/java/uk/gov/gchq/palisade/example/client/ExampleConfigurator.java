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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        UserService user = new HashMapUserService();
        AuditService audit = new LoggerAuditService();
        SimplePalisadeService palisade = new SimplePalisadeService()
                .auditService(audit)
                .policyService(policy)
                .userService(user)
                .cacheService(cache);

        HDFSResourceService resource = createSingleJVMResourceService(configService, cache, palisade);

        //build a config for client
        InitialConfig singleJVMconfig = new InitialConfig()
                .put(AuditService.class.getTypeName(), audit.getClass().getTypeName())
                .put(AuditService.class.getTypeName() + ConfiguredClientServices.STATE, new String(JSONSerialiser.serialise(audit)))

                .put(PolicyService.class.getTypeName(), policy.getClass().getTypeName())
                .put(PolicyService.class.getTypeName() + ConfiguredClientServices.STATE, new String(JSONSerialiser.serialise(policy)))

                .put(UserService.class.getTypeName(), user.getClass().getTypeName())
                .put(UserService.class.getTypeName() + ConfiguredClientServices.STATE, new String(JSONSerialiser.serialise(user)))

                .put(CacheService.class.getTypeName(), cache.getClass().getTypeName())
                .put(CacheService.class.getTypeName() + ConfiguredClientServices.STATE, new String(JSONSerialiser.serialise(cache)))

                .put(PalisadeService.class.getTypeName(), palisade.getClass().getTypeName())
                .put(PalisadeService.class.getTypeName() + ConfiguredClientServices.STATE, new String(JSONSerialiser.serialise(palisade)));

        singleJVMconfig.put(ResourceService.class.getTypeName(), resource.getClass().getTypeName());
        resource.writeConfiguration(singleJVMconfig);
        //insert this into the cache manually so it can be created later
        configService.add((AddConfigRequest) new AddConfigRequest()
                .config(singleJVMconfig)
                .service(Optional.empty())).join();
        return configService;
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
     * Allows the bootstrapping of some configuration data for the multi-JVM example.
     *
     * @param etcdEndpoints the list of etcd end points
     * @return the configuration service to provide the Palisade entry point
     */
    public static InitialConfigurationService setupMultiJVMConfigurationService(final List<String> etcdEndpoints) {
        //configure the multi JVM settings
        AuditService audit = new LoggerAuditService();
        CacheService cache = new SimpleCacheService().backingStore(new HashMapBackingStore(true));
        PalisadeService palisade = new ProxyRestPalisadeService("http://localhost:8080/palisade");
        PolicyService policy = new ProxyRestPolicyService("http://localhost:8081/policy");
        ResourceService resource = new ProxyRestResourceService("http://localhost:8082/resource");
        UserService user = new ProxyRestUserService("http://localhost:8083/user");
        ProxyRestConfigService configService = new ProxyRestConfigService("http://localhost:8085/config");

        InitialConfig multiJVMConfig = new InitialConfig()
                .put(AuditService.class.getTypeName(), audit.getClass().getTypeName())
                .put(AuditService.class.getTypeName() + ConfiguredClientServices.STATE, new String(JSONSerialiser.serialise(audit)))

                .put(PolicyService.class.getTypeName(), policy.getClass().getTypeName())
                .put(PolicyService.class.getTypeName() + ConfiguredClientServices.STATE, new String(JSONSerialiser.serialise(policy)))

                .put(UserService.class.getTypeName(), user.getClass().getTypeName())
                .put(UserService.class.getTypeName() + ConfiguredClientServices.STATE, new String(JSONSerialiser.serialise(user)))

                .put(CacheService.class.getTypeName(), cache.getClass().getTypeName())
                .put(CacheService.class.getTypeName() + ConfiguredClientServices.STATE, new String(JSONSerialiser.serialise(cache)))

                .put(PalisadeService.class.getTypeName(), palisade.getClass().getTypeName())
                .put(PalisadeService.class.getTypeName() + ConfiguredClientServices.STATE, new String(JSONSerialiser.serialise(palisade)));

        multiJVMConfig.put(ResourceService.class.getTypeName(), resource.getClass().getTypeName());
        resource.writeConfiguration(multiJVMConfig);
        //insert this into the cache manually so it can be created later
        configService.add((AddConfigRequest) new AddConfigRequest()
                .config(multiJVMConfig)
                .service(Optional.empty())).join();

        //create the services config that they will be able to find the example data
        //if there are no endpoints, assume we are using a HashMap backing store for now
        //at the moment only resource service is using the etcd cache, once all config management work is done, then all services will
        SimpleCacheService resourceCache = new SimpleCacheService();
        if (etcdEndpoints.isEmpty()) {
            resourceCache.backingStore(new HashMapBackingStore(true));
        } else {
            EtcdBackingStore etcdStore = new EtcdBackingStore().connectionDetails(etcdEndpoints);
            resourceCache.backingStore(etcdStore);
        }
        createMultiJVMResourceService(configService, resourceCache, Optional.empty());
        //close the resourceCache
        if (resourceCache.getBackingStore() instanceof EtcdBackingStore) {
            ((EtcdBackingStore) resourceCache.getBackingStore()).close();
        }

        return configService;
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
        InitialConfig resourceConfig = new InitialConfig();
        HDFSResourceService resourceServer;

        try {
            resourceServer = new HDFSResourceService(new Configuration(), cache);
            //set up the example object type
            final Map<String, ConnectionDetail> dataType = new HashMap<>();
            dataType.put(RESOURCE_TYPE, new ProxyRestConnectionDetail().url("http://localhost:8084/data").serviceClass(ProxyRestDataService.class));
            resourceServer.connectionDetail(null, dataType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //switch to containerised cache
        containerCache.ifPresent(resourceServer::setCacheService);

        //write class name
        resourceConfig.put(ResourceService.class.getTypeName(), resourceServer.getClass().getTypeName());
        //write the configured data
        resourceServer.writeConfiguration(resourceConfig);
        configService.add((AddConfigRequest) new AddConfigRequest()
                .config(resourceConfig)
                .service(Optional.of(ResourceService.class))).join();
        return resourceServer;
    }

    /**
     * Allows the bootstrapping of some configuration data for the Docker example.
     *
     * @return the configuration service to provide the Palisade entry point
     */
    public static InitialConfigurationService setupDockerConfigurationService() {
        //configure the multi JVM settings
        AuditService audit = new LoggerAuditService();
        //this is the cache that is used by the client to talk to the containerised etcd
        CacheService cache = new SimpleCacheService().backingStore(new EtcdBackingStore().connectionDetails(Collections.singletonList("http://localhost:2379")));
        PalisadeService palisade = new ProxyRestPalisadeService("http://localhost:8080/palisade");
        PolicyService policy = new ProxyRestPolicyService("http://localhost:8081/policy");
        ResourceService resource = new ProxyRestResourceService("http://localhost:8082/resource");
        UserService user = new ProxyRestUserService("http://localhost:8083/user");
        ProxyRestConfigService configService = new ProxyRestConfigService("http://localhost:8085/config");
        InitialConfig multiJVMConfig = new InitialConfig()
                .put(AuditService.class.getTypeName(), audit.getClass().getTypeName())
                .put(AuditService.class.getTypeName() + ConfiguredClientServices.STATE, new String(JSONSerialiser.serialise(audit)))

                .put(PolicyService.class.getTypeName(), policy.getClass().getTypeName())
                .put(PolicyService.class.getTypeName() + ConfiguredClientServices.STATE, new String(JSONSerialiser.serialise(policy)))

                .put(UserService.class.getTypeName(), user.getClass().getTypeName())
                .put(UserService.class.getTypeName() + ConfiguredClientServices.STATE, new String(JSONSerialiser.serialise(user)))

                .put(CacheService.class.getTypeName(), cache.getClass().getTypeName())
                .put(CacheService.class.getTypeName() + ConfiguredClientServices.STATE, new String(JSONSerialiser.serialise(cache)))

                .put(PalisadeService.class.getTypeName(), palisade.getClass().getTypeName())
                .put(PalisadeService.class.getTypeName() + ConfiguredClientServices.STATE, new String(JSONSerialiser.serialise(palisade)));

        multiJVMConfig.put(ResourceService.class.getTypeName(), resource.getClass().getTypeName());
        resource.writeConfiguration(multiJVMConfig);
        //insert this into the cache manually so it can be created later
        configService.add((AddConfigRequest) new AddConfigRequest()
                .config(multiJVMConfig)
                .service(Optional.empty())).join();

        //create the services config that they will retrieve
        //this is the cache that is used from within a container
        CacheService containerCache = new SimpleCacheService().backingStore(new EtcdBackingStore().connectionDetails(Collections.singletonList("http://etcd:2379"), false));
        createMultiJVMResourceService(configService, cache, Optional.of(containerCache));

        return configService;
    }
}
