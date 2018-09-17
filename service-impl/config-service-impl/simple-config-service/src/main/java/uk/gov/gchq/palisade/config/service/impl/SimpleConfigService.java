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

import uk.gov.gchq.palisade.cache.service.CacheService;
import uk.gov.gchq.palisade.cache.service.request.GetCacheRequest;
import uk.gov.gchq.palisade.config.service.InitialConfigurationService;
import uk.gov.gchq.palisade.config.service.exception.NoConfigException;
import uk.gov.gchq.palisade.config.service.request.GetConfigRequest;
import uk.gov.gchq.palisade.service.Service;
import uk.gov.gchq.palisade.service.request.InitialConfig;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static java.util.Objects.requireNonNull;

public class SimpleConfigService implements InitialConfigurationService {

    public static final String ANONYMOUS_CONFIG_KEY = "simple.config.anonymous";

    private CacheService cache;

    public SimpleConfigService(final CacheService cache) {
        requireNonNull(cache, "cache");
        this.cache = cache;
    }

    public CacheService getCache() {
        //never null
        return cache;
    }

    public void setCache(final CacheService cache) {
        cache(cache);
    }

    public SimpleConfigService cache(final CacheService cache) {
        requireNonNull(cache, "cache");
        this.cache = cache;
        return this;
    }

    @Override
    public CompletableFuture<InitialConfig> get(final GetConfigRequest request) throws NoConfigException {
        requireNonNull(request, "request");
        if (request.getService().isPresent()) { //has a Service requested some config?
            return CompletableFuture.completedFuture(request.getService().map(this::getServiceConfig).get());
        } else { //grab the anonymous client config
            return CompletableFuture.completedFuture(getAnonymousConfig());
        }
    }

    private InitialConfig getAnonymousConfig() {
        CompletableFuture<Optional<InitialConfig>> cachedObject = cache.get(new GetCacheRequest<InitialConfig>()
                .service(InitialConfigurationService.class)
                .key(ANONYMOUS_CONFIG_KEY));
        return cachedObject.join().orElseThrow(() -> new NoConfigException("no initial configuration could be found"));
    }

    private InitialConfig getServiceConfig(final Class<? extends Service> clazz) {
        return null; //TODO implement
    }
}
