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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.cache.service.CacheService;
import uk.gov.gchq.palisade.cache.service.request.AddCacheRequest;
import uk.gov.gchq.palisade.cache.service.request.GetCacheRequest;
import uk.gov.gchq.palisade.cache.service.request.ListCacheRequest;
import uk.gov.gchq.palisade.cache.service.request.RemoveCacheRequest;
import uk.gov.gchq.palisade.exception.NoConfigException;
import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.palisade.service.request.ServiceConfiguration;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

/**
 * The implementation for a {@link CacheService} that can persist various object types to arbitrary backing stores.
 * Instances of this class primarily add an instance of {@link BackingStore} to which the work of persistence is
 * delegated. This class is responsible for managing data into and out of any backing store so clients have a
 * transparent interface to a backing store and the actual backing store implementation is abstracted away.
 *
 * @apiNote no parameters may be <code>null</code>.
 */
public class SimpleCacheService implements CacheService {

    private static final String STORE_IMPL_KEY = "cache.svc.store";

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleCacheService.class);

    /**
     * The codec registry that knows how to encode objects.
     */
    private final CacheCodecRegistry codecs = new CacheCodecRegistry();

    /**
     * The store for our data.
     */
    private BackingStore store;

    /**
     * Create and empty backing store. Note that this is for use by serialisation mechanisms and any attempt to use an
     * instance of this class without first initialising a backing store will result in exceptions being thrown.
     */
    public SimpleCacheService() {
    }

    @Override
    public void applyConfigFrom(final ServiceConfiguration config) throws NoConfigException {
        requireNonNull(config, "config");
        //extract cache
        String serialised = config.getOrDefault(STORE_IMPL_KEY, null);
        if (nonNull(serialised)) {
            store = JSONSerialiser.deserialise(serialised.getBytes(JSONSerialiser.UTF8), BackingStore.class);
        } else {
            throw new NoConfigException("no backing store specified in configuration");
        }
    }

    @Override
    public void recordCurrentConfigTo(final ServiceConfiguration config) {
        requireNonNull(config, "config");
        config.put(CacheService.class.getTypeName(), getClass().getTypeName());
        String serialisedCache = new String(JSONSerialiser.serialise(store), JSONSerialiser.UTF8);
        config.put(STORE_IMPL_KEY, serialisedCache);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("codecs", codecs)
                .append("store", store)
                .toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SimpleCacheService that = (SimpleCacheService) o;

        return new EqualsBuilder()
                .append(getCodecs(), that.getCodecs())
                .append(store, that.store)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getCodecs())
                .append(store)
                .toHashCode();
    }

    /**
     * Set the backing store for this instance.
     *
     * @param store the backing store instance
     * @return this object
     */
    public SimpleCacheService backingStore(final BackingStore store) {
        requireNonNull(store, "store");
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
     * Get the codec registry.
     *
     * @return codec registry
     */
    public CacheCodecRegistry getCodecs() {
        return codecs;
    }

    /**
     * Get the backing store for this instance.
     *
     * @return the backing store
     */
    public BackingStore getBackingStore() {
        requireNonNull(store, "store must be initialised");
        return store;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> CompletableFuture<Boolean> add(final AddCacheRequest<V> request) {
        requireNonNull(request, "request");
        //make the final key name
        String baseKey = request.makeBaseName();
        LOGGER.debug("Add item with key {}", baseKey);

        V value = request.getValue();
        Class<V> valueClass = (Class<V>) request.getValue().getClass();
        Optional<Duration> timeToLive = request.getTimeToLive();

        //find encoder function
        Function<V, byte[]> encoder = codecs.getValueEncoder(valueClass);
        //encode value
        byte[] encodedValue = encoder.apply(value);
        //send to add
        return CompletableFuture.supplyAsync(() -> {
            LOGGER.debug("-> Backing store add {}", baseKey);
            boolean result = getBackingStore().add(baseKey, valueClass, encodedValue, timeToLive);
            LOGGER.debug("-> Backing store stored {} with result {}", baseKey, result);
            return result;
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> CompletableFuture<Optional<V>> get(final GetCacheRequest<V> request) {
        requireNonNull(request, "request");
        //make final key name
        String baseKey = request.makeBaseName();
        LOGGER.debug("Get item {}", baseKey);

        //get from add
        Supplier<Optional<V>> getFunction = () -> {
            LOGGER.debug("-> Backing store get {}", baseKey);
            SimpleCacheObject result = getBackingStore().get(baseKey);
            if (result.getValue().isPresent()) {
                LOGGER.debug("-> Backing store retrieved {}", baseKey);
            } else {
                LOGGER.debug("-> Backing store failed to get {}", baseKey);
            }

            //assign so Javac can infer the generic type
            BiFunction<byte[], Class<V>, V> decode = codecs.getValueDecoder((Class<V>) result.getValueClass());
            return result.getValue().map(x -> decode.apply(x, (Class<V>) result.getValueClass()));
        };
        return CompletableFuture.supplyAsync(getFunction);
    }

    @Override
    public CompletableFuture<Stream<String>> list(final ListCacheRequest request) {
        requireNonNull(request, "request");

        //make final key name
        String baseKey = request.makeBaseName();
        LOGGER.debug("List items with prefix {}", baseKey);

        int len = request.getServiceStringForm().length();

        //get from add
        return CompletableFuture.supplyAsync(() -> {
            LOGGER.debug("-> Backing store list {}", baseKey);

            //remove the service name from the list of keys
            Stream<String> ret = getBackingStore().list(baseKey).map(x -> x.substring(len + 1));
            LOGGER.debug("-> Backing store list returned for {}", baseKey);

            return ret;
        });
    }

    @Override
    public CompletableFuture<Boolean> remove(final RemoveCacheRequest request) {
        requireNonNull(request, "request");

        //make final key name
        String baseKey = request.makeBaseName();
        LOGGER.debug("Remove item key {}", baseKey);

        //create remove request for backing store
        return CompletableFuture.supplyAsync(() -> {
            LOGGER.debug("-> Backing store remove {}", baseKey);

            //remove the key
            boolean removed = getBackingStore().remove(baseKey);
            LOGGER.debug("-> Backing store removed {} with result {}", baseKey, removed);
            return removed;
        });
    }
}
