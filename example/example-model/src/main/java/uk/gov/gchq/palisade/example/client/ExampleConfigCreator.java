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
import uk.gov.gchq.palisade.cache.service.request.AddCacheRequest;
import uk.gov.gchq.palisade.client.ConfiguredServices;
import uk.gov.gchq.palisade.config.service.InitialConfigurationService;
import uk.gov.gchq.palisade.config.service.impl.SimpleConfigService;
import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.palisade.policy.service.PolicyService;
import uk.gov.gchq.palisade.policy.service.impl.HierarchicalPolicyService;
import uk.gov.gchq.palisade.resource.service.HDFSResourceService;
import uk.gov.gchq.palisade.resource.service.ResourceService;
import uk.gov.gchq.palisade.service.PalisadeService;
import uk.gov.gchq.palisade.service.impl.SimplePalisadeService;
import uk.gov.gchq.palisade.service.request.InitialConfig;
import uk.gov.gchq.palisade.user.service.UserService;
import uk.gov.gchq.palisade.user.service.impl.HashMapUserService;

import java.io.IOException;
import java.util.function.Function;

/**
 * Convenience class for the examples to configure the config creation service.
 */
public final class ExampleConfigCreator {
    private ExampleConfigCreator() {
    }

    /**
     * Allows the bootstrapping of some configuration data for the single JVM example.
     *
     * @return the config. provider
     */
    public static InitialConfigurationService setupSingleJVMConfigurationService() {
        CacheService cache = new SimpleCacheService().backingStore(new HashMapBackingStore(true));
        //configure the single JVM settings
        AuditService audit = new LoggerAuditService();
        PolicyService policy = new HierarchicalPolicyService();
        ResourceService resource = null;
        try {
            resource = new HDFSResourceService(new Configuration(), null, null).useSharedConnectionDetails(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        UserService user = new HashMapUserService();
        PalisadeService palisade = new SimplePalisadeService()
                .resourceService(resource)
                .auditService(audit)
                .policyService(policy)
                .userService(user)
                .cacheService(cache);

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
                .put(PalisadeService.class.getCanonicalName() + ConfiguredServices.STATE, new String(JSONSerialiser.serialise(palisade)));
        //insert this into the cache manually so it can be created later
        cache.add(new AddCacheRequest<InitialConfig>()
                .service(InitialConfigurationService.class)
                .key(SimpleConfigService.ANONYMOUS_CONFIG_KEY)
                .value(singleJVMconfig))
                .join();

        return new SimpleConfigService(cache);
    }
}
