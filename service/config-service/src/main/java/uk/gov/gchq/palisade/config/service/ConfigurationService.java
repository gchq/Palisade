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
package uk.gov.gchq.palisade.config.service;

import uk.gov.gchq.palisade.config.service.request.AddConfigRequest;
import uk.gov.gchq.palisade.config.service.request.GetConfigRequest;
import uk.gov.gchq.palisade.exception.NoConfigException;
import uk.gov.gchq.palisade.service.Service;
import uk.gov.gchq.palisade.service.request.Request;
import uk.gov.gchq.palisade.service.request.ServiceConfiguration;

import java.util.concurrent.CompletableFuture;

/**
 * This is the service that provides other parts of Palisade and clients with their initial configuration necessary to
 * initialise themselves. For example, this could include the URLs for where other Palisade services are, or credentials
 * for various external services.
 * <p>
 * Clients should request their initial configuration by creating a {@link GetConfigRequest} with an empty {@code
 * service} field. This will cause the configuration service to respond with the configuration for a generic client.
 * <p>
 * Implementations of this interface should ensure that only the necessary configuration is provided for a request;
 * sensitive information could be leaked otherwise.
 * <p>
 * All methods in this class throw {@link NullPointerException} unless otherwise stated for <code>null</code>
 * parameters.
 */
public interface ConfigurationService extends Service {

    /**
     * Get the initial configuration from Palisade. This is the entry method into the Palisade system.
     *
     * @param request the request that specifies what part of the system to request configuration for
     * @return the configuration object
     * @throws NoConfigException if no configuration for the given request code be found
     */
    CompletableFuture<ServiceConfiguration> get(final GetConfigRequest request) throws NoConfigException;

    /**
     * Add the given initial configuration to the configuration service. This should not be used by clients and should
     * only be used by Palisade internal components.
     *
     * @param request the request giving the configuration to add
     * @return boolean indicating success
     */
    CompletableFuture<Boolean> add(final AddConfigRequest request);

    /**
     * {@inheritDoc}
     */
    @Override
    default CompletableFuture<?> process(final Request request) {
        if (request instanceof GetConfigRequest) {
            return get((GetConfigRequest) request);
        }
        if (request instanceof AddConfigRequest) {
            return add((AddConfigRequest) request);
        }
        return Service.super.process(request);
    }

    /**
     * Instructs this configuration service to query its cache for extra configuration data. This may be called as part
     * of a bootstrapping process. Once a {@link ConfigurationService} has been created with minimal configuration,
     * this method can be called to cause it to retrieve any extra configuration information from the cache. It is an error
     * to call this method before a cache service has been configured for an instance.
     *
     * @throws IllegalStateException if no cache has been configured
     */
    default void configureSelfFromConfig() {
    }
}
