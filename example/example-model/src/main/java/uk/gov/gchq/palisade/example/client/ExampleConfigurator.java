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
import uk.gov.gchq.palisade.cache.service.impl.HashMapBackingStore;
import uk.gov.gchq.palisade.cache.service.impl.SimpleCacheService;
import uk.gov.gchq.palisade.client.ConfiguredServices;
import uk.gov.gchq.palisade.config.service.InitialConfigurationService;
import uk.gov.gchq.palisade.config.service.impl.ProxyRestConfigService;
import uk.gov.gchq.palisade.config.service.impl.SimpleConfigService;
import uk.gov.gchq.palisade.config.service.request.AddConfigRequest;
import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.palisade.policy.service.PolicyService;
import uk.gov.gchq.palisade.policy.service.impl.HierarchicalPolicyService;
import uk.gov.gchq.palisade.resource.service.HDFSResourceService;
import uk.gov.gchq.palisade.resource.service.ResourceService;
import uk.gov.gchq.palisade.resource.service.impl.ProxyRestResourceService;
import uk.gov.gchq.palisade.service.PalisadeService;
import uk.gov.gchq.palisade.service.impl.ProxyRestPalisadeService;
import uk.gov.gchq.palisade.service.impl.ProxyRestPolicyService;
import uk.gov.gchq.palisade.service.impl.SimplePalisadeService;
import uk.gov.gchq.palisade.service.request.InitialConfig;
import uk.gov.gchq.palisade.user.service.UserService;
import uk.gov.gchq.palisade.user.service.impl.HashMapUserService;
import uk.gov.gchq.palisade.user.service.impl.ProxyRestUserService;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Convenience class for the examples to configure the config creation service.
 */
public final class ExampleConfigurator {

    private static CacheService cache = new SimpleCacheService().backingStore(new HashMapBackingStore());

    private ExampleConfigurator() {
    }

    /**
     * Create a properties backed config service for examples.
     *
     * @param backingStorePath the path to keep config in
     * @return the config service
     */
    public static SimpleConfigService createConfigService(final Path backingStorePath) {
        return new SimpleConfigService(cache);
    }

    /**
     * Allows the creation of some configuration data for the single JVM example.
     *
     * @return the configuration service that provides the entry to Palisade
     */
    public static InitialConfigurationService setupSingleJVMConfigurationService() {
        InitialConfigurationService configService = new SimpleConfigService(
                new SimpleCacheService()
                        .backingStore(new HashMapBackingStore(true))
        );
        //configure the single JVM settings
        AuditService audit = new LoggerAuditService();
        PolicyService policy = new HierarchicalPolicyService().cacheService(cache);
        ResourceService resource = null;
        try {
            resource = new HDFSResourceService(new Configuration(), null, null).useSharedConnectionDetails(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        CacheService cache = new SimpleCacheService().backingStore(new HashMapBackingStore(true));
        UserService user = new HashMapUserService();
        PalisadeService palisade = new SimplePalisadeService()
                .resourceService(resource)
                .auditService(audit)
                .policyService(policy)
                .userService(user)
                .cacheService(cache);
        //build a config for client
        InitialConfig singleJVMconfig = new InitialConfig()
                .put(ResourceService.class.getCanonicalName(), resource.getClass().getCanonicalName())
                .put(ResourceService.class.getCanonicalName() + ConfiguredServices.STATE, new String(JSONSerialiser.serialise(resource)))

                .put(AuditService.class.getCanonicalName(), audit.getClass().getCanonicalName())
                .put(AuditService.class.getCanonicalName() + ConfiguredServices.STATE, new String(JSONSerialiser.serialise(audit)))

                .put(PolicyService.class.getCanonicalName(), policy.getClass().getCanonicalName())
                .put(PolicyService.class.getCanonicalName() + ConfiguredServices.STATE, new String(JSONSerialiser.serialise(policy)))

                .put(UserService.class.getCanonicalName(), user.getClass().getCanonicalName())
                .put(UserService.class.getCanonicalName() + ConfiguredServices.STATE, new String(JSONSerialiser.serialise(user)))

                .put(CacheService.class.getCanonicalName(), cache.getClass().getCanonicalName())
                .put(CacheService.class.getCanonicalName() + ConfiguredServices.STATE, new String(JSONSerialiser.serialise(cache)))

                .put(PalisadeService.class.getCanonicalName(), palisade.getClass().getCanonicalName())
                .put(PalisadeService.class.getCanonicalName() + ConfiguredServices.STATE, new String(JSONSerialiser.serialise(palisade)))

                .put(InitialConfigurationService.class.getCanonicalName(), configService.getClass().getCanonicalName())
                .put(InitialConfigurationService.class.getCanonicalName() + ConfiguredServices.STATE, new String(JSONSerialiser.serialise(configService)));
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
                .put(ResourceService.class.getCanonicalName(), resource.getClass().getCanonicalName())
                .put(ResourceService.class.getCanonicalName() + ConfiguredServices.STATE, new String(JSONSerialiser.serialise(resource)))

                .put(AuditService.class.getCanonicalName(), audit.getClass().getCanonicalName())
                .put(AuditService.class.getCanonicalName() + ConfiguredServices.STATE, new String(JSONSerialiser.serialise(audit)))

                .put(PolicyService.class.getCanonicalName(), policy.getClass().getCanonicalName())
                .put(PolicyService.class.getCanonicalName() + ConfiguredServices.STATE, new String(JSONSerialiser.serialise(policy)))

                .put(UserService.class.getCanonicalName(), user.getClass().getCanonicalName())
                .put(UserService.class.getCanonicalName() + ConfiguredServices.STATE, new String(JSONSerialiser.serialise(user)))

                .put(CacheService.class.getCanonicalName(), cache.getClass().getCanonicalName())
                .put(CacheService.class.getCanonicalName() + ConfiguredServices.STATE, new String(JSONSerialiser.serialise(cache)))

                .put(PalisadeService.class.getCanonicalName(), palisade.getClass().getCanonicalName())
                .put(PalisadeService.class.getCanonicalName() + ConfiguredServices.STATE, new String(JSONSerialiser.serialise(palisade)))

                .put(InitialConfigurationService.class.getCanonicalName(), configService.getClass().getCanonicalName())
                .put(InitialConfigurationService.class.getCanonicalName() + ConfiguredServices.STATE, new String(JSONSerialiser.serialise(configService)));
        //insert this into the cache manually so it can be created later
        configService.add((AddConfigRequest) new AddConfigRequest()
                .config(multiJVMConfig)
                .service(Optional.empty())).join();
        return configService;
    }
}
