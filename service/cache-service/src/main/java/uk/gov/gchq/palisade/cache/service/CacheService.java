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
import uk.gov.gchq.palisade.cache.service.request.ListCacheRequest;
import uk.gov.gchq.palisade.service.Service;
import uk.gov.gchq.palisade.service.request.Request;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * The core API for the cache service. The purpose of the cache service is to store the information that the various
 * services will require. For exaple, you might have multiple instances of the palisade service running and the
 * registration request (from <code>registerDataRequest</code>) might go to a different palisade service to the
 * getDataRequestConfig() call. The data in the cache should be maintained by a time to live value rather than removing
 * after the get request as any scalable deployment would likely make multiple requests to the cache due to many data
 * services working on a subset of the list of resources in parallel.
 * <p>
 * The cache service will namespace items according to the {@link Service} that puts things in the cache.
 */
public interface CacheService extends Service {
    /**
     * Adds a record to the cache according to the contents of the {@link AddCacheRequest} parameter.
     *
     * @param request {@link uk.gov.gchq.palisade.cache.service.request.AddCacheRequest} containing a unique requestId
     *                and the information to be cached.
     * @param <V>     the value type being added to the cache
     * @return a {@link CompletableFuture} which would be true once the information has been cached.
     */
    <V> CompletableFuture<Boolean> add(final AddCacheRequest<V> request);

    /**
     * Retrieve an item from the cache. The <code>request</code> should be parameterized according to the expected type
     * of object to be retrieved. If the cache couldn't find an entry for the requested key, then the {@link Optional}
     * returned will be empty.
     *
     * @param request {@link uk.gov.gchq.palisade.cache.service.request.GetCacheRequest} containing the key.
     * @param <V>     the value type being retrieved
     * @return a {@link CompletableFuture} that contains an {@link Optional} for the cached key
     */
    <V> CompletableFuture<Optional<V>> get(final GetCacheRequest<V> request);

    /**
     * Returns a list of all cache entries available that match the given prefix. The cache will look in the namespace
     * for the service named in <code>request</code> and return all key names that start with the given prefix.
     *
     * @param request the request configured with a prefix
     * @return a list of strings that are cache keys
     */
    CompletableFuture<Stream<String>> list(final ListCacheRequest request);

    @Override
    default CompletableFuture<?> process(final Request request) {
        if (request instanceof AddCacheRequest) {
            add((AddCacheRequest) request);
            return null;
        }
        if (request instanceof GetCacheRequest) {
            get((GetCacheRequest) request);
            return null;
        }
        if (request instanceof ListCacheRequest) {
            list((ListCacheRequest) request);
            return null;
        }
        return Service.super.process(request);
    }
}
