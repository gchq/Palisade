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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

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

/**
 * Adds behaviour to the {@link SerialisedDataReader} to keep details of the serialisers in the cache. This means
 * that the serialisers are global across the Palisade deployment. When a call to {@link CachedSerialisedDataReader#read(DataReaderRequest)}
 * is made, then the map of current serialisers is loaded from the cache and merged into the existing map. Therefore,
 * if a serialiser is added, then the {@link DataReader} will find it dynamically and does not need to be restarted.
 */
public abstract class CachedSerialisedDataReader extends SerialisedDataReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(CachedSerialisedDataReader.class);

    /**
     * The serialiser key in the configuration.
     */
    public static final String SERIALISER_KEY = "cached.serialiser.map";

    /**
     * Wrapping class to ensure JSON serialisation preserves types.
     */
    public static final class MapWrap {

        @JsonSerialize(keyUsing = DataFlavour.FlavourSerializer.class)
        @JsonDeserialize(keyUsing = DataFlavour.FlavourDeserializer.class)
        private final Map<DataFlavour, Serialiser<?>> instance;

        @JsonCreator
        public MapWrap(@JsonProperty("instance") final Map<DataFlavour, Serialiser<?>> instance) {
            this.instance = instance;
        }

        public Map<DataFlavour, Serialiser<?>> getInstance() {
            return instance;
        }
    }

    /**
     * Cache service for storing serialisers.
     */
    private CacheService cacheService;

    /**
     * Load the map of serialisers from the cache service. The map retrieved will be merged into the current list by calling
     * {@link SerialisedDataReader#addAllSerialisers(Map)}.
     */
    public void retrieveSerialisersFromCache() {
        Map<DataFlavour, Serialiser<?>> newTypeMap = retrieveFromCache(getCacheService());
        addAllSerialisers(newTypeMap);
    }

    /**
     * Set the cache service to use.
     *
     * @param cacheService new cache service
     * @return this object
     */
    public CachedSerialisedDataReader cacheService(final CacheService cacheService) {
        requireNonNull(cacheService, "Cache service cannot be set to null.");
        this.cacheService = cacheService;
        return this;
    }

    /**
     * Set the cache service to use.
     *
     * @param cacheService new cache service
     */
    public void setCacheService(final CacheService cacheService) {
        cacheService(cacheService);
    }

    /**
     * Get the current cache service.
     *
     * @return the current cache
     */
    public CacheService getCacheService() {
        requireNonNull(cacheService, "The cache service has not been set.");
        return cacheService;
    }

    /**
     * {@inheritDoc} Overridden to update the list of serialisers before attempting the read.
     */
    @Override
    public DataReaderResponse read(final DataReaderRequest request) {
        retrieveSerialisersFromCache();
        return super.read(request);
    }

    /**
     * Fetch a map of serialisers from the given cache service. This doesn't do anything other than create a new map
     * retrieved from the cache.
     *
     * @param cache the cache service to use
     * @return new mappings
     */
    private static Map<DataFlavour, Serialiser<?>> retrieveFromCache(final CacheService cache) {
        requireNonNull(cache, "cache");
        GetCacheRequest<MapWrap> request = new GetCacheRequest<>()
                .service(DataService.class)
                .key(SERIALISER_KEY);
        //go retrieve this from the cache
        Optional<MapWrap> map = cache.get(request).join();

        //unwrap the mapping or create a blank one
        Map<DataFlavour, Serialiser<?>> newMap = map.orElse(new MapWrap(new HashMap())).getInstance();

        LOGGER.debug("Retrieved these serialisers from cache {}", newMap);
        return newMap;
    }

    /**
     * Adds a new serialiser to the cache of serialisers. This method will associate the given {@link DataFlavour} with
     * the given {@link Serialiser} in the cache. The next time a {@link CachedSerialisedDataReader} subclass attempts to
     * read some data, it will see this new mapping. This method is not atomic, but retrieves the entire map, adds the
     * new mapping and then replaces it in the cache.
     *
     * @param cache      the cache service to use
     * @param flavour    the data flavour to apply
     * @param serialiser the serialiser
     * @return a boolean that will complete when the map is updated in the cache
     */
    public static CompletableFuture<Boolean> addSerialiserToCache(final CacheService cache, final DataFlavour flavour, final Serialiser<?> serialiser) {
        requireNonNull(cache, "cache");
        requireNonNull(flavour, "flavour");
        requireNonNull(serialiser, "serialiser");

        //get the current map
        Map<DataFlavour, Serialiser<?>> typeMap = retrieveFromCache(cache);

        //add the new flavour to it
        typeMap.put(flavour, serialiser);

        //now record this back into the cache
        AddCacheRequest<MapWrap> cacheRequest = new AddCacheRequest<>()
                .service(DataService.class)
                .key(SERIALISER_KEY)
                .value(new MapWrap(typeMap));

        LOGGER.debug("Adding {} for flavour {} to the cache", serialiser, flavour);
        return cache.add(cacheRequest);
    }
}
