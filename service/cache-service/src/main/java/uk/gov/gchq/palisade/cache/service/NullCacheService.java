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

package uk.gov.gchq.palisade.cache.service;

import uk.gov.gchq.palisade.cache.service.request.AddCacheRequest;
import uk.gov.gchq.palisade.cache.service.request.GetCacheRequest;
import uk.gov.gchq.palisade.service.request.DataRequestConfig;

import java.util.concurrent.CompletableFuture;

/**
 * A null implementation of the {@link CacheService} that prevents hitting
 * {@link NullPointerException}s if your deployment does not require a
 * {@link CacheService}, but one is expected.
 */
public class NullCacheService implements CacheService {
    @Override
    public CompletableFuture<Boolean> add(final AddCacheRequest request) {
        // Nothing was cached, so return false.
        return CompletableFuture.completedFuture(false);
    }

    @Override
    public CompletableFuture<DataRequestConfig> get(final GetCacheRequest request) {
        return CompletableFuture.completedFuture(new DataRequestConfig());
    }
}
