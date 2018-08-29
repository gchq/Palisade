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
package uk.gov.gchq.palisade.cache.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.cache.service.CacheService;
import uk.gov.gchq.palisade.cache.service.request.AddCacheRequest;
import uk.gov.gchq.palisade.cache.service.request.GetCacheRequest;
import uk.gov.gchq.palisade.cache.service.request.ListCacheRequest;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class BasicCacheService implements CacheService {
    Logger LOGGER = LoggerFactory.getLogger(BasicCacheService.class);

    private 

    @Override
    public <K, V> CompletableFuture<Boolean> add(final AddCacheRequest<K, V> request) {
        return null;
    }

    @Override
    public <V> CompletableFuture<V> get(final GetCacheRequest<V> request) {
        return null;
    }

    @Override
    public CompletableFuture<Collection<?>> list(final ListCacheRequest request) {
        return null;
    }
}
