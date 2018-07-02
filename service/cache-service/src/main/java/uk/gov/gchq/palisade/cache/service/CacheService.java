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
import uk.gov.gchq.palisade.service.Service;
import uk.gov.gchq.palisade.service.request.DataRequestConfig;
import uk.gov.gchq.palisade.service.request.Request;

import java.util.concurrent.CompletableFuture;

/**
 * The core API for the cache service.
 * The purpose of the cache service is to store the information that the data
 * service will require based on a 'registerDataRequest' made to the palisade
 * service. The reason this is required is because you might have multiple
 * instances of the palisade service running and the registration request might
 * go to a different palisade service to the getDataRequestConfig.
 * The data in the cache should be maintained by a time to live value rather than
 * removing after the get request as any scalable deployment would likely make
 * multiple requests to the cache due to many data services working on a subset
 * of the list of resources in parallel.
 */
public interface CacheService extends Service {
    /**
     * Adds a record to the cache according to the contents of the {@link AddCacheRequest}
     * parameter. The AddCacheRequest will contain a identifier that is unique
     * per client request for data and therefore it is a good candidate for a
     * lookup key.
     *
     * @param request {@link AddCacheRequest} containing a unique requestId and
     *                the information to be cached.
     * @return a {@link CompletableFuture} which would be true once the
     * information has been cached.
     */
    CompletableFuture<Boolean> add(final AddCacheRequest request);

    /**
     * Pull out of the cache the information related to the unique requestId
     * contained in the {@link GetCacheRequest} parameter.
     * This method should not remove the item from cache just because it has been
     * retrieved once. There may be many requests to get the cached data
     * instigated my the many data services that are accessing subsets of the list
     * of resource stemming from the single registered data request.
     *
     * @param request {@link GetCacheRequest} containing the unique requestId.
     * @return The {@link DataRequestConfig} relating to the unique requestId
     * that is stored in the cache.
     */
    CompletableFuture<DataRequestConfig> get(final GetCacheRequest request);

    @Override
    default CompletableFuture<?> process(final Request request) {
        if (request instanceof AddCacheRequest) {
            add((AddCacheRequest) request);
            return null;
        }
        return Service.super.process(request);
    }
}
