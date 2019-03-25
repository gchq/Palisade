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
import uk.gov.gchq.palisade.service.ServiceState;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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
 * <p>
 * {@link SimpleCacheService} implements a local cache for low time to live requests. The maximum time to live for locally
 * cacheable items is {@link SimpleCacheService#MAX_LOCAL_TTL}. Only positive cache hits are cached, not negative ones.
 *
 * @apiNote no parameters may be <code>null</code>.
 */
public class SimpleCacheService implements CacheService {

    private static final String STORE_IMPL_KEY = "cache.svc.store";
    private static final String MAX_LOCAL_TTL_KEY = "cache.svc.max.ttl";

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleCacheService.class);

    /**
     * The default maximum allowed time to live for entries that are marked as locally cacheable.
     */
    public static final Duration MAX_LOCAL_TTL = Duration.of(5, ChronoUnit.MINUTES);

    /**
     * The codec registry that knows how to encode objects.
     */
    private final CacheCodecRegistry codecs = new CacheCodecRegistry();

    /**
     * The store for our data.
     */
    private BackingStore store;

    /**
     * The maximum length of time for entries that are marked as locally cacheable.
     */
    private Duration maxLocalTTL = MAX_LOCAL_TTL;

    /**
     * The local store for retrieved objects.
     */
    private final Map<String, SimpleCacheObject> localObjects = new ConcurrentHashMap<>();

    /**
     * Timer thread to remove local cache entries after expiry.
     */
    private static final ScheduledExecutorService REMOVAL_TIMER = Executors.newSingleThreadScheduledExecutor();

    /**
     * Create and empty backing store. Note that this is for use by serialisation mechanisms and any attempt to use an
     * instance of this class without first initialising a backing store will result in exceptions being thrown.
     */
    public SimpleCacheService() {
    }

    @Override
    public void applyConfigFrom(final ServiceState config) throws NoConfigException {
        requireNonNull(config, "config");
        //extract cache
        String serialised = config.getOrDefault(STORE_IMPL_KEY, null);
        if (nonNull(serialised)) {
            setBackingStore(JSONSerialiser.deserialise(serialised.getBytes(StandardCharsets.UTF_8), BackingStore.class));
        } else {
            throw new NoConfigException("no backing store specified in configuration");
        }
        //extract max local TTL
        String serialisedDuration = config.getOrDefault(MAX_LOCAL_TTL_KEY, MAX_LOCAL_TTL.toString());
        maxLocalTTL = Duration.parse(serialisedDuration);
    }

    @Override
    public void recordCurrentConfigTo(final ServiceState config) {
        requireNonNull(config, "config");
        config.put(CacheService.class.getTypeName(), getClass().getTypeName());
        String serialisedCache = new String(JSONSerialiser.serialise(store), StandardCharsets.UTF_8);
        config.put(STORE_IMPL_KEY, serialisedCache);
        config.put(MAX_LOCAL_TTL_KEY, maxLocalTTL.toString());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("codecs", codecs)
                .append("store", store)
                .append("maxLocalTTL", maxLocalTTL)
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
                .append(maxLocalTTL, that.maxLocalTTL)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getCodecs())
                .append(store)
                .append(maxLocalTTL)
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
     * Get the backing store for this instance.
     *
     * @return the backing store
     */
    public BackingStore getBackingStore() {
        requireNonNull(store, "store must be initialised");
        return store;
    }

    /**
     * Sets the maximum amount of time to live allowed for cache entries that are cached locally.
     *
     * @param maxLocalCacheTime maxmimum time for local cache entries
     * @return this object
     * @throws IllegalArgumentException if {@code maxLocalCacheTime} is negative
     */
    public SimpleCacheService maximumLocalCacheDuration(final Duration maxLocalCacheTime) {
        requireNonNull(maxLocalCacheTime, "maxLocalCacheTime");
        if (maxLocalCacheTime.isNegative()) {
            throw new IllegalArgumentException("cannot be negative");
        }
        this.maxLocalTTL = maxLocalCacheTime;
        return this;
    }

    /**
     * Sets the maximum amount of time to live allowed for cache entries that are cached locally.
     *
     * @param maxLocalCacheTime maxmimum time for local cache entries
     * @throws IllegalArgumentException if {@code maxLocalCacheTime} is negative
     */
    public void setMaximumLocalCacheDuration(final Duration maxLocalCacheTime) {
        maximumLocalCacheDuration(maxLocalCacheTime);
    }

    /**
     * The maximum length of time to live for an entry that is can be put into the local cache.
     *
     * @return the max cache duration for local entries
     */
    public Duration getMaximumLocalCacheDuration() {
        return maxLocalTTL;
    }

    /**
     * Get the codec registry.
     *
     * @return codec registry
     */
    public CacheCodecRegistry getCodecs() {
        return codecs;
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
        boolean localCacheable = request.getLocallyCacheable();

        //is this locally cacheable? If so, check the TTL is present and below the maximum time
        if (localCacheable) {
            if (!timeToLive.isPresent() || (timeToLive.isPresent() && maxLocalTTL.compareTo(timeToLive.get()) <= 0)) {
                throw new IllegalArgumentException("time to live must be set and be below " + maxLocalTTL.getSeconds() + " seconds for locally cacheable values");
            }
        }

        //find encoder function
        Function<V, byte[]> encoder = codecs.getValueEncoder(valueClass);
        //encode value
        byte[] encodedValue = encoder.apply(value);
        //add any needed metadata
        byte[] metadataWrapped = CacheMetadata.addMetaData(encodedValue, request);
        //send to add
        return CompletableFuture.supplyAsync(() -> {
            LOGGER.debug("-> Backing store add {}", baseKey);
            boolean result = getBackingStore().add(baseKey, valueClass, metadataWrapped, timeToLive);
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

        Supplier<Optional<V>> getFunction = () -> {
            SimpleCacheObject result = doCacheRetrieve(baseKey, maxLocalTTL);

            //assign so Javac can infer the generic type
            BiFunction<byte[], Class<V>, V> decode = codecs.getValueDecoder((Class<V>) result.getValueClass());
            return result.getValue().map(x -> decode.apply(x, (Class<V>) result.getValueClass()));
        };
        return CompletableFuture.supplyAsync(getFunction);
    }

    /**
     * Performs the retrieval of the given key from the backing store or from the local cache if it is available. This method
     * encapsulates the actual logic for performing the retrieval. If the backing store indicates that the returned value
     * is suitable for local caching, then the cache object will be stored locally for the given time to live. Repeated retrieval
     * attempts within that time window will not make a request to the backing store, but to the local cache.
     *
     * @param baseKey       the complete key name to use with the backing store
     * @param localCacheTTL the time to live for a local storage entry
     * @return the raw cache object provided by the backing store
     */
    SimpleCacheObject doCacheRetrieve(final String baseKey, final Duration localCacheTTL) {
        LOGGER.debug("Get item {}", baseKey);
        //do we have this locally?
        SimpleCacheObject localRetrieve = localObjects.get(baseKey);
        if (localRetrieve != null) {
            LOGGER.debug("Retrieved from local cache {}", baseKey);
            localRetrieve.getMetadata().ifPresent(metadata -> metadata.setWasRetrievedLocally(true));
            return localRetrieve;
        } else {
            LOGGER.debug("-> Backing store get {}", baseKey);
            SimpleCacheObject remoteRetrieve = getBackingStore().get(baseKey);
            if (remoteRetrieve.getValue().isPresent()) {
                LOGGER.debug("-> Backing store retrieved {}", baseKey);

                CacheMetadata.populateMetaData(remoteRetrieve);

                //should this be cached?
                if (remoteRetrieve.getMetadata().get().canBeRetrievedLocally()) {
                    localObjects.put(baseKey, remoteRetrieve);
                    //set up a timer to remove it after the max TTL has elapsed
                    REMOVAL_TIMER.schedule(() -> localObjects.remove(baseKey), localCacheTTL.toMillis(), TimeUnit.MILLISECONDS);
                }
            } else {
                LOGGER.debug("-> Backing store failed to get {}", baseKey);
            }
            return remoteRetrieve;
        }
    }

    @Override
    public CompletableFuture<Stream<String>> list(final ListCacheRequest request) {
        requireNonNull(request, "request");

        //make final key name
        String baseKey = request.makeBaseName();
        LOGGER.debug("List items with prefix {}", baseKey);

        int len = request.getServiceStringForm().length();

        //get from cache
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
