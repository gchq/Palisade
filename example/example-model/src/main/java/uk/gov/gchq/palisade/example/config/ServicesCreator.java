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
package uk.gov.gchq.palisade.example.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.gchq.palisade.audit.service.AuditService;
import uk.gov.gchq.palisade.cache.service.CacheService;
import uk.gov.gchq.palisade.config.service.ConfigurationService;
import uk.gov.gchq.palisade.config.service.Configurator;
import uk.gov.gchq.palisade.policy.service.PolicyService;
import uk.gov.gchq.palisade.resource.service.ResourceService;
import uk.gov.gchq.palisade.service.PalisadeService;
import uk.gov.gchq.palisade.service.ServiceState;
import uk.gov.gchq.palisade.user.service.UserService;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class ServicesCreator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServicesCreator.class);
    private final ConfigurationService configService;

    private final ServiceState config;

    private final ResourceService resourceService;
    private final AuditService auditService;
    private final PolicyService policyService;
    private final UserService userService;
    private final CacheService cacheService;
    private final PalisadeService palisadeService;

    public ServicesCreator(final ConfigurationService configService) {
        LOGGER.info("EMR debug: ServicesCreator - at start of ServicesCreator");
        requireNonNull(configService, "configService");
        this.configService = configService;
        LOGGER.info("EMR debug: ServicesCreator - after this.configService");
        this.config = new Configurator(configService).retrieveConfig(Optional.empty());
        LOGGER.info("EMR debug: ServicesCreator - after this.config = new Configurator(configService).retrieveConfig(Optional.empty()");
        this.resourceService = createResourceService();
        this.auditService = createAuditService();
        this.policyService = createPolicyService();
        this.userService = createUserService();
        this.cacheService = createCacheService();
        this.palisadeService = createPalisadeService();
    }

    public ResourceService getResourceService() {
        return resourceService;
    }

    public AuditService getAuditService() {
        return auditService;
    }

    public PolicyService getPolicyService() {
        return policyService;
    }

    public UserService getUserService() {
        return userService;
    }

    public CacheService getCacheService() {
        return cacheService;
    }

    public PalisadeService getPalisadeService() {
        return palisadeService;
    }

    /**
     * {@inheritDoc}
     *
     * @return the configuration service instance that was passed on construction
     */
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
        return Configurator.createFromConfig(AuditService.class, config);
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
