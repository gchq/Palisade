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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.audit.service.AuditService;
import uk.gov.gchq.palisade.cache.service.CacheService;
import uk.gov.gchq.palisade.config.service.ConfigConsts;
import uk.gov.gchq.palisade.config.service.ConfigurationService;
import uk.gov.gchq.palisade.config.service.Configurator;
import uk.gov.gchq.palisade.exception.NoConfigException;
import uk.gov.gchq.palisade.policy.service.PolicyService;
import uk.gov.gchq.palisade.resource.service.ResourceService;
import uk.gov.gchq.palisade.service.PalisadeService;
import uk.gov.gchq.palisade.service.Service;
import uk.gov.gchq.palisade.service.ServiceState;
import uk.gov.gchq.palisade.user.service.UserService;

import java.time.Duration;
import java.util.Optional;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

public class ClientConfiguredServices {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientConfiguredServices.class);

    private final ConfigurationService configService;

    private final ServiceState config;

    private final Optional<ResourceService> resourceService;
    private final Optional<AuditService> auditService;
    private final Optional<PolicyService> policyService;
    private final Optional<UserService> userService;
    private final Optional<CacheService> cacheService;
    private final Optional<PalisadeService> palisadeService;

    public ClientConfiguredServices(final ConfigurationService configService, final Duration timeOut) {
        requireNonNull(configService, "configService");
        requireNonNull(timeOut, "timeOut");
        if (timeOut.isNegative()) {
            throw new IllegalArgumentException("timeOut cannnot be negative");
        }
        this.configService = configService;
        this.config = fetchClientState(timeOut);
        this.resourceService = createResourceService();
        this.auditService = createAuditService();
        this.policyService = createPolicyService();
        this.userService = createUserService();
        this.cacheService = createCacheService();
        this.palisadeService = createPalisadeService();
    }

    /**
     * Repeatedly attempt to get the client configuration from the configuration service. This method will try multiple times
     * with a delay to contact the configuration service to get the client configuration, up to the specified time out.
     *
     * @param timeout the time to wait before declaring failure
     * @return the client configuration
     */
    private ServiceState fetchClientState(final Duration timeout) {
        ServiceState clientConfig = null;
        //expiry time
        long timeExpire = System.currentTimeMillis() + timeout.toMillis();
        //while we don't have any config and time left, make attempt
        while (isNull(clientConfig) && System.currentTimeMillis() < timeExpire) {
            try {
                clientConfig = new Configurator(configService).retrieveConfig(Optional.empty());
            } catch (NoConfigException e) {
                //if that failed then wait and retry
                LOGGER.warn("No client configuration present, waiting...");
                //keep trying after short delay
                try {
                    Thread.sleep(ConfigConsts.DELAY);
                } catch (InterruptedException ignore) {
                }
            }
        }

        //check if we exited with nothing
        if (isNull(clientConfig)) {
            throw new RuntimeException("Couldn't retrieve client configuration. Is configuration service running? Is the configuration service populated?");
        }

        return clientConfig;
    }

    public ResourceService getResourceService() {
        return resourceService.orElseThrow(() -> new NoConfigException("the configuration returned from the configuration service did not contain any configuration for ResourceService"));
    }

    public AuditService getAuditService() {
        return auditService.orElseThrow(() -> new NoConfigException("the configuration returned from the configuration service did not contain any configuration for AuditService"));
    }

    public PolicyService getPolicyService() {
        return policyService.orElseThrow(() -> new NoConfigException("the configuration returned from the configuration service did not contain any configuration for PolicyService"));
    }

    public UserService getUserService() {
        return userService.orElseThrow(() -> new NoConfigException("the configuration returned from the configuration service did not contain any configuration for UserService"));
    }

    public CacheService getCacheService() {
        return cacheService.orElseThrow(() -> new NoConfigException("the configuration returned from the configuration service did not contain any configuration for CacheService"));
    }

    public PalisadeService getPalisadeService() {
        return palisadeService.orElseThrow(() -> new NoConfigException("the configuration returned from the configuration service did not contain any configuration for PalisadeService"));
    }

    /**
     * {@inheritDoc}
     *
     * @return the configuration service instance that was passed on construction
     */
    public ConfigurationService getConfigService() {
        return configService;
    }

    /**
     * Tries to create the given service class from the given state. If the class instance cannot be successfully created
     * then an empty optional is returned.
     *
     * @param serviceClass the class to create
     * @param state        the service state for the client
     * @param <T>          the Service type
     * @return an optional that may contain the created service class
     */
    protected static <T extends Service> Optional<T> attemptToCreate(final Class<T> serviceClass, final ServiceState state) {
        try {
            return Optional.of(Configurator.createFromConfig(serviceClass, state));
        } catch (NoConfigException e) {
            return Optional.empty();
        }
    }

    protected Optional<CacheService> createCacheService() {
        return attemptToCreate(CacheService.class, config);
    }

    protected Optional<ResourceService> createResourceService() {
        return attemptToCreate(ResourceService.class, config);
    }

    protected Optional<AuditService> createAuditService() {
        return attemptToCreate(AuditService.class, config);
    }

    protected Optional<PolicyService> createPolicyService() {
        return attemptToCreate(PolicyService.class, config);
    }

    protected Optional<UserService> createUserService() {
        return attemptToCreate(UserService.class, config);
    }

    protected Optional<PalisadeService> createPalisadeService() {
        return attemptToCreate(PalisadeService.class, config);
    }
}
