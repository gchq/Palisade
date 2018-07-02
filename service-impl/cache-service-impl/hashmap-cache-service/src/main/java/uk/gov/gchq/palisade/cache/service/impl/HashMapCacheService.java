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

import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.cache.service.CacheService;
import uk.gov.gchq.palisade.cache.service.request.AddCacheRequest;
import uk.gov.gchq.palisade.cache.service.request.GetCacheRequest;
import uk.gov.gchq.palisade.service.request.DataRequestConfig;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A HashMapCacheService is a simple implementation of a {@link CacheService} that simply caches the objects in a
 * ConcurrentHashMap. By default the cache is static so it will be shared across the same JVM.
 */
public class HashMapCacheService implements CacheService {
    private static final Logger LOGGER = LoggerFactory.getLogger(HashMapCacheService.class);

    private static final ConcurrentHashMap<RequestId, DataRequestConfig> CACHE = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<RequestId, DataRequestConfig> cache;

    /**
     * Timer thread to remove cache entries after expiry.
     */
    private static final ScheduledExecutorService REMOVAL_TIMER = Executors.newSingleThreadScheduledExecutor();

    public HashMapCacheService() {
        this(true);
    }

    public HashMapCacheService(final boolean useStatic) {
        if (useStatic) {
            cache = CACHE;
        } else {
            cache = new ConcurrentHashMap<>();
        }
    }

    @Override
    public CompletableFuture<Boolean> add(final AddCacheRequest request) {
        LOGGER.debug("Adding to cache: {}", request);
        cache.put(request.getRequestId(), request.getDataRequestConfig());
        /*Here we set up a simple timer to deal with the removal of the item from the cache if a duration is present
         *This uses a single timer to remove elements, this is fine for this example, but in production we would want
         *something more performant.
         */
        request.getTimeToLive().ifPresent(duration -> {
            REMOVAL_TIMER.schedule(() -> cache.remove(request.getRequestId()), duration.toMillis(), TimeUnit.MILLISECONDS);
        });
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<DataRequestConfig> get(final GetCacheRequest request) {
        LOGGER.debug("Getting from cache: {}", request);
        final DataRequestConfig result = cache.get(request.getRequestId());
        LOGGER.debug("Got from cache: {}", result);
        return CompletableFuture.completedFuture(result);
    }
}
