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
import uk.gov.gchq.palisade.user.service.UserService;
import uk.gov.gchq.palisade.user.service.impl.HashMapUserService;
import uk.gov.gchq.palisade.user.service.impl.ProxyRestUserService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
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
        final CacheService cache = new SimpleCacheService().backingStore(new HashMapBackingStore(true));
        final InitialConfigurationService configService = new SimpleConfigService(cache);
        //configure the single JVM settings
        AuditService audit = new LoggerAuditService();
        ResourceService resource = null;
        try {
            resource = new HDFSResourceService(new Configuration(), null, null).useSharedConnectionDetails(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        PolicyService policy = new HierarchicalPolicyService().cacheService(cache);
        UserService user = new HashMapUserService();
        PalisadeService palisade = new SimplePalisadeService()
                .resourceService(resource)
                .auditService(audit)
                .policyService(policy)
                .userService(user)
                .cacheService(cache);
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
     * Allows the bootstrapping of some configuration data for the multi-JVM example.
     *
     * @return the configuration service to provide the Palisade entry point
     */
    public static InitialConfigurationService setupMultiJVMConfigurationService() {
        //configure the multi JVM settings
        AuditService audit = new LoggerAuditService();
        CacheService cache = new SimpleCacheService().backingStore(new HashMapBackingStore(true));
        PalisadeService palisade = new ProxyRestPalisadeService("http://localhost:8080/palisade");
        PolicyService policy = new ProxyRestPolicyService("http://localhost:8081/policy");
        ResourceService resource = new ProxyRestResourceService("http://localhost:8082/resource");
        UserService user = new ProxyRestUserService("http://localhost:8083/user");
        ProxyRestConfigService configService = new ProxyRestConfigService("http://localhost:8085/config");

        InitialConfig multiJVMConfig = new InitialConfig()

                .put(AuditService.class.getCanonicalName(), audit.getClass().getTypeName())
                .put(AuditService.class.getCanonicalName() + ConfiguredServices.STATE, new String(JSONSerialiser.serialise(audit)))

                .put(PolicyService.class.getCanonicalName(), policy.getClass().getTypeName())
                .put(PolicyService.class.getCanonicalName() + ConfiguredServices.STATE, new String(JSONSerialiser.serialise(policy)))

                .put(UserService.class.getCanonicalName(), user.getClass().getTypeName())
                .put(UserService.class.getCanonicalName() + ConfiguredServices.STATE, new String(JSONSerialiser.serialise(user)))

                .put(CacheService.class.getCanonicalName(), cache.getClass().getTypeName())
                .put(CacheService.class.getCanonicalName() + ConfiguredServices.STATE, new String(JSONSerialiser.serialise(cache)))

                .put(PalisadeService.class.getCanonicalName(), palisade.getClass().getTypeName())
                .put(PalisadeService.class.getCanonicalName() + ConfiguredServices.STATE, new String(JSONSerialiser.serialise(palisade)))

                .put(InitialConfigurationService.class.getCanonicalName(), configService.getClass().getTypeName())
                .put(InitialConfigurationService.class.getCanonicalName() + ConfiguredServices.STATE, new String(JSONSerialiser.serialise(configService)));
        multiJVMConfig.put(ResourceService.class.getTypeName(), resource.getClass().getTypeName());
        resource.writeConfiguration(multiJVMConfig);
        //insert this into the cache manually so it can be created later
        configService.add((AddConfigRequest) new AddConfigRequest()
                .config(multiJVMConfig)
                .service(Optional.empty())).join();

        //create the services config that they will retrieve
        configureMultiJVMResourceService(configService);

        return configService;
    }

    /**
     * Allows the bootstrapping of some configuration data for the Docker example.
     *
     * @return the configuration service to provide the Palisade entry point
     */
    public static InitialConfigurationService setupDockerConfigurationService() {
        //configure the multi JVM settings
        AuditService audit = new LoggerAuditService();
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
        configureMultiJVMResourceService(configService);

        return configService;
    }

    /**
     * Sets the resource service configuration up that will be retrieved by the resource service from the configuration
     * service.
     *
     * @param configService the central configuration service
     */
    private static void configureMultiJVMResourceService(final InitialConfigurationService configService) {
        InitialConfig resourceConfig = new InitialConfig();
        HDFSResourceService resourceServer = null;
        try {
            resourceServer = new HDFSResourceService(new Configuration(), null, null).useSharedConnectionDetails(true);
            //set up the example object type
            final Map<String, ConnectionDetail> dataType = new HashMap<>();
            dataType.put(RESOURCE_TYPE, new ProxyRestConnectionDetail().url("http://localhost:8084/data").serviceClass(ProxyRestDataService.class));
            resourceServer.connectionDetail(null, dataType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //write class name
        resourceConfig.put(ResourceService.class.getTypeName(), resourceServer.getClass().getTypeName());
        //write the configured data
        resourceServer.writeConfiguration(resourceConfig);
        configService.add((AddConfigRequest) new AddConfigRequest()
                .config(resourceConfig)
                .service(Optional.of(ResourceService.class))).join();
    }
}
