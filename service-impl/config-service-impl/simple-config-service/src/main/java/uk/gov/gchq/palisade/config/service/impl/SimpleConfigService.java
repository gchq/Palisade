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

import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.cache.service.CacheService;
import uk.gov.gchq.palisade.config.service.ConfigurationService;
import uk.gov.gchq.palisade.config.service.request.GetConfigRequest;
import uk.gov.gchq.palisade.service.Service;
import uk.gov.gchq.palisade.service.request.ServicesConfig;

import java.util.concurrent.CompletableFuture;

import static java.util.Objects.requireNonNull;

public class SimpleConfigService implements ConfigurationService {

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
    public CompletableFuture<ServicesConfig> get(final GetConfigRequest request) {
        requireNonNull(request, "request");
        if (request.getService().isPresent()) { //has a Service requested some config?
            return CompletableFuture.completedFuture(request.getService().map(this::getServiceConfig).get());
        } else if (request.getUser().isPresent()) { //has a user requested config?
            return CompletableFuture.completedFuture(request.getUser().map(this::getUserConfig).get());
        } else { //grab the anonymous client config
            return CompletableFuture.completedFuture(getAnonymousConfig());
        }
    }

    private ServicesConfig getAnonymousConfig() {
        return null; //TODO implement
    }

    private ServicesConfig getUserConfig(final User user) {
        return null; //TODO implement
    }

    private ServicesConfig getServiceConfig(final Class<? extends Service> clazz) {
        return null; //TODO implement
    }
}
