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

package uk.gov.gchq.palisade.data.service.reader;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.tuple.ImmutablePair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.gchq.palisade.cache.service.CacheService;
import uk.gov.gchq.palisade.cache.service.request.AddCacheRequest;
import uk.gov.gchq.palisade.cache.service.request.GetCacheRequest;
import uk.gov.gchq.palisade.data.serialise.Serialiser;
import uk.gov.gchq.palisade.data.service.DataService;
import uk.gov.gchq.palisade.data.service.reader.request.DataReaderRequest;
import uk.gov.gchq.palisade.data.service.reader.request.DataReaderResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.tuple.ImmutablePair.of;

public abstract class CachedSerialisedDataReader extends SerialisedDataReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(CachedSerialisedDataReader.class);

    public static final String SERIALISER_KEY = "serialiser.map";

    /**
     * Cache service for storing serialisers.
     */
    private CacheService cacheService;

    public void retrieveSerialisersFromCache() {
        Map<ImmutablePair<String, String>, Serialiser<?>> newTypeMap = retrieveFromCache(getCacheService());
        setSerialisers(newTypeMap);
    }

    public CachedSerialisedDataReader cacheService(final CacheService cacheService) {
        requireNonNull(cacheService, "Cache service cannot be set to null.");
        this.cacheService = cacheService;
        return this;
    }

    public void setCacheService(final CacheService cacheService) {
        cacheService(cacheService);
    }

    public CacheService getCacheService() {
        requireNonNull(cacheService, "The cache service has not been set.");
        return cacheService;
    }

    /**
     * {@inheritDoc} Overridden to update the list of serialisers before attempting the read.
     */
    @Override
    public DataReaderResponse read(DataReaderRequest request) {
        retrieveSerialisersFromCache();
        return super.read(request);
    }

    private static Map<ImmutablePair<String, String>, Serialiser<?>> retrieveFromCache(final CacheService cache) {
        requireNonNull(cache, "cache");
        GetCacheRequest<Map<ImmutablePair<String, String>, Serialiser<?>>> request = new GetCacheRequest<>()
                .service(DataService.class)
                .key(SERIALISER_KEY);
        //go retrieve this from the cache
        Optional<Map<ImmutablePair<String, String>, Serialiser<?>>> map = cache.get(request).join();

        Map<ImmutablePair<String, String>, Serialiser<?>> newMap = map.orElseGet(HashMap::new);

        LOGGER.debug("Retrieved these serialisers from cache {}", newMap);
        //if there is nothing there then create a new map and return it
        return newMap;
    }

    public static CompletableFuture<Boolean> addSerialiser(final CacheService cache, final String dataType, final String dataFormat, final Serialiser<?> serialiser) {
        requireNonNull(cache, "cache");
        requireNonNull(dataType, "dataType");
        requireNonNull(dataFormat, "dataFormat");
        requireNonNull(serialiser, "serialiser");
        if (dataType.trim().isEmpty()) {
            throw new IllegalArgumentException("data type cannot be empty");
        }
        if (dataFormat.trim().isEmpty()) {
            throw new IllegalArgumentException("data format cannot be empty");
        }

        //get the current map
        Map<ImmutablePair<String, String>, Serialiser<?>> typeMap = retrieveFromCache(cache);

        //add the new flavour to it
        typeMap.put(of(dataType, dataFormat), serialiser);

        //now record this back into the cache
        AddCacheRequest<Map<ImmutablePair<String, String>, Serialiser<?>>> cacheRequest = new AddCacheRequest<>()
                .service(DataService.class)
                .key(SERIALISER_KEY)
                .value(typeMap);
        return cache.add(cacheRequest);
    }
}
