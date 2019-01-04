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

package uk.gov.gchq.palisade.cache.service.heart;

import uk.gov.gchq.palisade.cache.service.CacheService;
import uk.gov.gchq.palisade.cache.service.request.ListCacheRequest;
import uk.gov.gchq.palisade.service.Service;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Monitors a list of currently alive instances for a given {@link uk.gov.gchq.palisade.service.Service} type by using
 * the cache service to look for heartbeats from active instances.
 *
 * @see Heartbeat
 */
public class Stethoscope {

    /**
     * The cache service we are using.
     */
    private CacheService cache;

    /**
     * The type of service that is being monitored.
     */
    private Class<? extends Service> serviceClass;

    /**
     * Create an instance.
     */
    public Stethoscope() {
    }

    /**
     * Set the cache service used as the co-ordination mechanism.
     *
     * @param cacheService the cache service to send messages to
     * @return this object
     */
    public Stethoscope cacheService(final CacheService cacheService) {
        requireNonNull(cacheService, "cacheService");
        this.cache = cacheService;
        return this;
    }

    /**
     * Set the cache service used as the co-ordination mechanism.
     *
     * @param cacheService the cache service to send messages to
     */
    public void setCacheService(final CacheService cacheService) {
        cacheService(cacheService);
    }

    /**
     * Get the current cache service.
     *
     * @return the cache service
     */
    public CacheService getCacheService() {
        requireNonNull(cache, "cache service must be set");
        return cache;
    }

    /**
     * Sets the service type for this stethoscope to monitor.
     *
     * @param serviceClass the type of service to monitor
     * @return this object
     */
    public Stethoscope serviceClass(final Class<? extends Service> serviceClass) {
        requireNonNull(serviceClass, "serviceClass");
        this.serviceClass = serviceClass;
        return this;
    }

    /**
     * Sets the service type for this stethoscope to monitor.
     *
     * @param serviceClass the type of service to monitor
     */
    public void setServiceClass(final Class<? extends Service> serviceClass) {
        serviceClass(serviceClass);
    }

    /**
     * Get the current service class type.
     *
     * @return the service class
     */
    public Class<? extends Service> getServiceClass() {
        requireNonNull(serviceClass, "serviceClass must be set");
        return serviceClass;
    }

    /**
     * Fetch a feed of active instances of the specific service type for this stethoscope.
     *
     * @return a stream of instance names
     * @apiNote To "auscultate" means to listen to the sound of the internal organs, usually for diagnosis.
     */
    public Stream<String> auscultate() {
        final CacheService localCache = getCacheService();
        final ListCacheRequest request = (ListCacheRequest) new ListCacheRequest()
                .prefix(HeartUtil.HEARTBEAT_SENTINEL)
                .service(getServiceClass());
        //make call to cache
        CompletableFuture<Stream<String>> futureList = localCache.list(request);
        return futureList.join()
                .map(s -> s.substring(HeartUtil.HEARTBEAT_SENTINEL.length()));
    }
}
