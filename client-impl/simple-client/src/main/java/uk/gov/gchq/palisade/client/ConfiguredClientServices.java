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
package uk.gov.gchq.palisade.client;

import uk.gov.gchq.palisade.audit.service.AuditService;
import uk.gov.gchq.palisade.cache.service.CacheService;
import uk.gov.gchq.palisade.config.service.Configurator;
import uk.gov.gchq.palisade.config.service.InitialConfigurationService;
import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.palisade.policy.service.PolicyService;
import uk.gov.gchq.palisade.resource.service.ResourceService;
import uk.gov.gchq.palisade.service.PalisadeService;
import uk.gov.gchq.palisade.service.Service;
import uk.gov.gchq.palisade.service.request.InitialConfig;
import uk.gov.gchq.palisade.user.service.UserService;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class ConfiguredClientServices implements ServicesFactory {
    //TODO: remove after gh-129 done
    public static final String STATE = ".state";

    private final InitialConfigurationService configService;

    private final InitialConfig config;

    private final ResourceService resourceService;
    private final AuditService auditService;
    private final PolicyService policyService;
    private final UserService userService;
    private final CacheService cacheService;
    private final PalisadeService palisadeService;

    public ConfiguredClientServices(final InitialConfigurationService configService) {
        requireNonNull(configService, "configService");
        this.configService = configService;

        this.config = new Configurator(configService).retrieveConfig(Optional.empty());
        this.resourceService = createResourceService();
        this.auditService = createAuditService();
        this.policyService = createPolicyService();
        this.userService = createUserService();
        this.cacheService = createCacheService();
        this.palisadeService = createPalisadeService();
    }

    @Override
    public ResourceService getResourceService() {
        return resourceService;
    }

    @Override
    public AuditService getAuditService() {
        return auditService;
    }

    @Override
    public PolicyService getPolicyService() {
        return policyService;
    }

    @Override
    public UserService getUserService() {
        return userService;
    }

    @Override
    public CacheService getCacheService() {
        return cacheService;
    }

    @Override
    public PalisadeService getPalisadeService() {
        return palisadeService;
    }

    /**
     * {@inheritDoc}
     *
     * @return the configuration service instance that was passed on construction
     */
    @Override
    public InitialConfigurationService getConfigService() {
        return configService;
    }

    protected CacheService createCacheService() {
        return legacyCreate(CacheService.class);
    }

    protected ResourceService createResourceService() {
        return new Configurator(configService).createFromConfig(ResourceService.class, config);
    }

    protected AuditService createAuditService() {
        return new Configurator(configService).createFromConfig(AuditService.class, config);
    }

    protected PolicyService createPolicyService() {
        return new Configurator(configService).createFromConfig(PolicyService.class, config);
    }

    protected UserService createUserService() {
        return new Configurator(configService).createFromConfig(UserService.class, config);
    }

    protected PalisadeService createPalisadeService() {
        return legacyCreate(PalisadeService.class);
    }

    /*
     * This can eventually be removed once all services are configuring themselves based on the InitialConfig.
     * TODO: remove after gh-129 done
     */
    private <S extends Service> S legacyCreate(final Class<? extends Service> serviceClass) {
        requireNonNull(serviceClass, "serviceClass");
        try {
            String servClass = config.get(serviceClass.getTypeName());
            String jsonState = config.get(serviceClass.getTypeName() + STATE);

            //try to create class type
            Class<S> classImpl = (Class<S>) Class.forName(servClass).asSubclass(Service.class);

            //create it
            S instance = JSONSerialiser.deserialise(jsonState, classImpl);

            return instance;
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("couldn't create service class " + serviceClass, e);
        }
    }
}
