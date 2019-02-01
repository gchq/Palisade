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
import uk.gov.gchq.palisade.config.service.ConfigurationService;
import uk.gov.gchq.palisade.config.service.Configurator;
import uk.gov.gchq.palisade.policy.service.PolicyService;
import uk.gov.gchq.palisade.resource.service.ResourceService;
import uk.gov.gchq.palisade.service.PalisadeService;
import uk.gov.gchq.palisade.service.request.ServiceConfiguration;
import uk.gov.gchq.palisade.user.service.UserService;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class ConfiguredClientServices implements ServicesFactory {

    private final ConfigurationService configService;

    private final ServiceConfiguration config;

    private final ResourceService resourceService;
    private final AuditService auditService;
    private final PolicyService policyService;
    private final UserService userService;
    private final CacheService cacheService;
    private final PalisadeService palisadeService;

    public ConfiguredClientServices(final ConfigurationService configService) {
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
    public ConfigurationService getConfigService() {
        return configService;
    }

    protected CacheService createCacheService() {
        return Configurator.createFromConfig(CacheService.class, config);
    }

    protected ResourceService createResourceService() {
        return Configurator.createFromConfig(ResourceService.class, config);
    }

    protected AuditService createAuditService() {
        return null;
//        return Configurator.createFromConfig(AuditService.class, config);
    }

    protected PolicyService createPolicyService() {
        return Configurator.createFromConfig(PolicyService.class, config);
    }

    protected UserService createUserService() {
        return Configurator.createFromConfig(UserService.class, config);
    }

    protected PalisadeService createPalisadeService() {
        return Configurator.createFromConfig(PalisadeService.class, config);
    }
}
