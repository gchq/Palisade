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
package uk.gov.gchq.palisade.config.service.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.cache.service.CacheService;
import uk.gov.gchq.palisade.cache.service.request.AddCacheRequest;
import uk.gov.gchq.palisade.cache.service.request.GetCacheRequest;
import uk.gov.gchq.palisade.config.service.ConfigurationService;
import uk.gov.gchq.palisade.config.service.request.AddConfigRequest;
import uk.gov.gchq.palisade.config.service.request.GetConfigRequest;
import uk.gov.gchq.palisade.exception.NoConfigException;
import uk.gov.gchq.palisade.service.Service;
import uk.gov.gchq.palisade.service.request.ServiceConfiguration;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static java.util.Objects.requireNonNull;

/**
 * An implementation of {@link ConfigurationService} that uses the {@link CacheService} to provide all of its
 * storage.
 * <p>
 * All methods throw {@link NullPointerException} for any {@code null} parameters.
 */
public class SimpleConfigService implements ConfigurationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleConfigService.class);

    /**
     * Key used to specify the "client" configuration.
     */
    public static final String ANONYMOUS_CONFIG_KEY = "simple.config.anonymous";

    /**
     * The storage service for all data.
     */
    private CacheService cache;

    /**
     * Create a configuration service.
     *
     * @param cache the cache service
     */
    @JsonCreator
    public SimpleConfigService(@JsonProperty("cache") final CacheService cache) {
        requireNonNull(cache, "cache");
        this.cache = cache;
    }

    @Override
    public void applyConfigFrom(final ServiceConfiguration config) throws NoConfigException {
        requireNonNull(config, "config cannot be null");
        LOGGER.debug("Configure called: no-op");
    }

    @Override
    public void recordCurrentConfigTo(final ServiceConfiguration config) {
        requireNonNull(config, "config cannot be null");
        LOGGER.debug("Write configuration called: no-op");
    }

    /**
     * Get the cache service being used.
     *
     * @return the cache
     */
    public CacheService getCache() {
        //never null
        return cache;
    }

    /**
     * Set the cache service to use.
     *
     * @param cache the caching provider
     */
    public void setCache(final CacheService cache) {
        cache(cache);
    }

    /**
     * Set the cache service to use.
     *
     * @param cache the caching provider
     * @return this object
     */
    public SimpleConfigService cache(final CacheService cache) {
        requireNonNull(cache, "cache");
        this.cache = cache;
        return this;
    }

    @Override
    public void configureSelfFromCache() {
        try {
            LOGGER.debug("Getting auxiliary configuration from cache");
            ServiceConfiguration selfConfig = getServiceConfig(ConfigurationService.class);
            applyConfigFrom(selfConfig);
        } catch (NoConfigException e) {
            LOGGER.debug("No auxiliary configuration available", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<ServiceConfiguration> get(final GetConfigRequest request) throws NoConfigException {
        requireNonNull(request, "request");
        if (request.getService().isPresent()) { //has a Service requested some config?
            return CompletableFuture.completedFuture(request.getService().map(this::getServiceConfig).get());
        } else { //grab the anonymous client config
            return CompletableFuture.completedFuture(getAnonymousConfig());
        }
    }

    /**
     * Allows new configuration data to be added to the cache.
     *
     * @param request the request to put in the cache
     * @return a boolean that becomes {@code true} when the request is added to cache
     */
    public CompletableFuture<Boolean> add(final AddConfigRequest request) {
        requireNonNull(request, "request");
        final Optional<Class<? extends Service>> clazz = request.getService();
        final AddCacheRequest<ServiceConfiguration> addRequest = new AddCacheRequest<>()
                .service(ConfigurationService.class)
                .value(request.getConfig());
        //are we setting anonymous config
        if (clazz.isPresent()) {
            addRequest.key(clazz.get().getTypeName());
        } else {
            addRequest.key(ANONYMOUS_CONFIG_KEY);
        }
        //put in the cache
        return cache.add(addRequest);
    }

    /**
     * Creates the anonymous configuration that can be provided to clients. This only needs to contain the details for
     * the minimum necessary to interact with Palisade.
     *
     * @return the client configuration
     * @throws NoConfigException if no client configuration could be found
     */
    private ServiceConfiguration getAnonymousConfig() throws NoConfigException {
        CompletableFuture<Optional<ServiceConfiguration>> cachedObject = cache.get(new GetCacheRequest<ServiceConfiguration>()
                .service(ConfigurationService.class)
                .key(ANONYMOUS_CONFIG_KEY));
        return cachedObject.join().orElseThrow(() -> new NoConfigException("no initial configuration could be found"));
    }

    /**
     * Creates the configuration for the given service class. This should not be given to clients and may provide
     * different configurations to different services. This will first try to find the configuration for the given
     * service class and if that fails it looks for generic "service" configuration. If that fails an exception is
     * thrown.
     *
     * @param clazz the service class type
     * @return the service configuration
     * @throws NoConfigException if no configuration could be found
     */
    private ServiceConfiguration getServiceConfig(final Class<? extends Service> clazz) throws NoConfigException {
        requireNonNull(clazz, "clazz");
        //first can we find anything for this class specifically?
        final GetCacheRequest<ServiceConfiguration> serviceRequest = new GetCacheRequest<>()
                .service(ConfigurationService.class)
                .key(clazz.getTypeName());
        Optional<ServiceConfiguration> result = cache.get(serviceRequest).join();
        //if we get an object back, then return it
        if (result.isPresent()) {
            return result.get();
        }
        //make a call for the generic object
        final GetCacheRequest<ServiceConfiguration> genericRequest = new GetCacheRequest<>()
                .service(ConfigurationService.class)
                .key(Service.class.getTypeName());
        Optional<ServiceConfiguration> genericResult = cache.get(genericRequest).join();
        if (genericResult.isPresent()) {
            return genericResult.get();
        } else {
            throw new NoConfigException("no service configuration could be found");
        }
    }
}
