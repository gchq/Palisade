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

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * The implementation for a {@link CacheService} that can persist various object types to arbitrary backing stores.
 * Instances of this class primarily store an instance of {@link BackingStore} to which the work of persistence is
 * delegated. This class is responsible for managing data into and out of any backing store so clients have a
 * transparent interface to a backing store and the actual backing store implementation is abstracted away.
 *
 * @apiNote no parameters may be <code>null</code>.
 */
public class BasicCacheService implements CacheService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicCacheService.class);

    /**
     * The store for our data.
     */
    private BackingStore store;

    /**
     * Create and empty backing store. Note that this is for use by serialisation mechanisms and any attempt to use an
     * instance of this class without first initialising a backing store will result in exceptions being thrown.
     */
    public BasicCacheService() {
    }

    /**
     * Set the backing store for this instance.
     *
     * @param store the backing store instance
     * @return this object
     */
    public BasicCacheService backingStore(final BackingStore store) {
        Objects.requireNonNull(store, "store");
        this.store = store;
        return this;
    }

    /**
     * Set the backing store for this instance.
     *
     * @param store the backing store instance
     */
    public void setBackingStore(final BackingStore store) {
        backingStore(store);
    }

    /**
     * Get the backing store for this instance.
     *
     * @return the backing store
     */
    public BackingStore getBackingStore() {
        Objects.requireNonNull(store, "store must be initialised");
        return store;
    }

    @Override
    public <V> CompletableFuture<Boolean> add(final AddCacheRequest<V> request) {
        Objects.requireNonNull(request, "request");
        //make the final key name
        String baseKey = request.makeBaseName();
        LOGGER.debug("Got request to store item with key {}", baseKey);

        V value = request.getValue();
        Class<? extends Object> valueClass = request.getValue().getClass();
        Optional<Duration> timeToLive = request.getTimeToLive();

        //encode value
        byte[] encodedValue = request.getValueEncoder().apply(value);
        //send to store
        return CompletableFuture.supplyAsync(() -> {
            LOGGER.debug("Requesting backing store to store {}", baseKey);
            boolean result = getBackingStore().store(baseKey, valueClass, encodedValue, timeToLive);
            LOGGER.debug("Backing store has stored {} with result {}", baseKey, result);
            return result;
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> CompletableFuture<Optional<V>> get(final GetCacheRequest<V> request) {
        Objects.requireNonNull(request, "request");
        //make final key name
        String baseKey = request.makeBaseName();
        LOGGER.debug("Got request to retrieve item {}", baseKey);

        //get from store
        return CompletableFuture.supplyAsync(() -> {
            LOGGER.debug("Requesting backing store to retrieve {}", baseKey);
            BasicCacheObject result = getBackingStore().retrieve(baseKey);
            if (result.getValue().isPresent()) {
                LOGGER.debug("Backing store successfully retrieved {}", baseKey);
            } else {
                LOGGER.debug("Backing store failed to retrieve {}", baseKey);
            }

            //assign so Javac can infer the generic type
            BiFunction<byte[], Class<V>, V> decode = request.getValueDecoder();

            return result.getValue().map(x -> decode.apply(x, (Class<V>) result.getValueClass()));
        });
    }

    @Override
    public CompletableFuture<Stream<String>> list(final ListCacheRequest request) {
        Objects.requireNonNull(request, "request");

        //make final key name
        String baseKey = request.makeBaseName();
        LOGGER.debug("Got request to list items with prefix {} ", baseKey);

        int len = request.getServiceStringForm().length();

        //retrieve from store
        return CompletableFuture.supplyAsync(() -> {
            LOGGER.debug("Sending list request to store {}", baseKey);

            //remove the service name from the list of keys
            Stream<String> ret = getBackingStore().list(baseKey).map(x -> x.substring(len + 1));
            LOGGER.debug("Store list returned for {}", baseKey);

            return ret;
        });
    }
}
