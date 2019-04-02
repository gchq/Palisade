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
package uk.gov.gchq.palisade.cache.service.request;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonIgnore;

import uk.gov.gchq.palisade.exception.ForbiddenException;
import uk.gov.gchq.palisade.service.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.Objects;
import java.util.Optional;

/**
 * This class represents requests for things to be added to the cache. Cache requests may be given an optional time to live.
 * If specified, then the backing store implementation is expected to remove items once the given duration has elapsed.
 * If the time to live is left empty, then cached items never expire. Some cache implementations also support locally caching
 * specific key/values. This avoids the need to repeatedly fetch the same key/value from the backing store which may involve a network request.
 * This is an optional operation which not all cache implementations support. Only certain key/values are safe to be stored locally,
 * so a hint flag is provided ({@link AddCacheRequest#locallyCacheable}) to state that this key/value may be stored locally by clients.
 * The default for this flag is {@code false} and must be explicitly set on each add request. The cache may also enforce a maximum time to live
 * on cache requests that are locally cacheable.
 * <p>
 * Note: Clients must be aware that locally cacheable key/values may become stale
 * if other clients update the value after they have made the request.
 * <p>
 * All methods in this class throw {@link NullPointerException} if parameters are unset.
 *
 * @param <V> the type of object being cached
 */
public class AddCacheRequest<V> extends CacheRequest {

    /**
     * An empty optional indicates no time to live specified.
     */
    private Optional<Duration> timeToLive = Optional.empty();

    /**
     * The object to cache.
     */
    private V value;

    /**
     * Hint as to whether this cache value may be stored locally by clients.
     */
    private boolean locallyCacheable;

    public AddCacheRequest() {
    }

    @JsonIgnore
    @Override
    public void setOriginalRequestId(final String originalRequestId) {
        throw new ForbiddenException("Should not call AddCacheRequest.setOriginalRequestId()");
    }

    @JsonIgnore
    @Override
    public String getOriginalRequestId() {
        throw new ForbiddenException("Should not call AddCacheRequest.getOriginalRequestId()");
    }

    /**
     * Set the time to live for this cache entry in milliseconds. If the cache supports time to live, it will be
     * automatically expired after this amount of time.
     *
     * @param ttlMillis milliseconds for this entry to stay in the cache
     * @return this object
     * @throws IllegalArgumentException if <code>ttlMillis</code> is negative
     */
    public AddCacheRequest timeToLive(final long ttlMillis) {
        this.timeToLive = toDuration(ttlMillis);
        return this;
    }

    /**
     * Set the time to live for this cache entry. If the cache supports time to live, it will be automatically expired
     * after this point in time.
     *
     * @param expiryPoint the time point after which this entry should expire
     * @return this object
     * @throws IllegalArgumentException if the expiry point is before now
     */
    public AddCacheRequest timeToLive(final Temporal expiryPoint) {
        Objects.requireNonNull(expiryPoint, "expiryPoint");
        this.timeToLive = until(expiryPoint);
        return this;
    }

    /**
     * Set the time to live for this cache entry. If the cache supports time to live, it will be automatically expired
     * after this amount of time. An empty {@link Optional} means the time to live is infinite.
     *
     * @param timeToLive the duration this should be alive in the cache
     * @return this object
     * @throws IllegalArgumentException if a negative duration is specified
     */
    public AddCacheRequest timeToLive(final Optional<Duration> timeToLive) {
        Objects.requireNonNull(timeToLive, "timeToLive");
        timeToLive.ifPresent(x -> {
            if (x.isNegative()) {
                throw new IllegalArgumentException("negative time to live specified!");
            }
        });
        this.timeToLive = timeToLive;
        return this;
    }

    /**
     * Is this cache request locally cacheable by clients?
     *
     * @return true if the value can be cached locally
     */
    public boolean getLocallyCacheable() {
        return locallyCacheable;
    }

    /**
     * Sets whether this cache request key/value can be locally cached by clients.
     *
     * @param locallyCacheable if true, then clients may cache the key/value locally upon retrieval
     * @return this object
     */
    public AddCacheRequest locallyCacheable(final boolean locallyCacheable) {
        this.locallyCacheable = locallyCacheable;
        return this;
    }

    /**
     * Sets whether this cache request key/value can be locally cached by clients.
     *
     * @param locallyCacheable if true, then clients may cache the key/value locally upon retrieval
     */
    public void setLocallyCacheable(final boolean locallyCacheable) {
        locallyCacheable(locallyCacheable);
    }

    /**
     * Get the length of time this entry should be alive in the cache.
     *
     * @return the time to live
     */
    public Optional<Duration> getTimeToLive() {
        // can never be null
        return timeToLive;
    }

    /**
     * Set the value to be cached.
     *
     * @param value object to be cached
     * @return this object
     */
    public AddCacheRequest value(final V value) {
        Objects.requireNonNull(value, "value");
        this.value = value;
        return this;
    }

    /**
     * Set value to be cached.
     *
     * @param value object to be cached
     */
    public void setValue(final V value) {
        value(value);
    }

    /**
     * Get the cached object.
     *
     * @return the object
     */
    public V getValue() {
        Objects.requireNonNull(value, "value cannot be null");
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AddCacheRequest key(final String key) {
        super.key(key);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AddCacheRequest service(final Class<? extends Service> service) {
        super.service(service);
        return this;
    }

    /**
     * Set the time to live for this cache entry. If the cache supports time to live, it will be automatically expired
     * after this amount of time. An empty {@link Optional} means the time to live is infinite.
     *
     * @param timeToLive the duration this should be alive in the cache
     * @throws IllegalArgumentException if a negative duration is specified
     */
    public void setTimeToLive(final Optional<Duration> timeToLive) {
        timeToLive(timeToLive);
    }

    /**
     * Set the time to live for this cache entry in milliseconds. If the cache supports time to live, it will be
     * automatically expired after this amount of time.
     *
     * @param ttlMillis milliseconds for this entry to stay in the cache
     * @throws IllegalArgumentException if <code>ttlMillis</code> is negative
     */
    public void setTimeToLive(final long ttlMillis) {
        timeToLive(ttlMillis);
    }

    /**
     * Set the time to live for this cache entry. If the cache supports time to live, it will be automatically expired
     * after this point in time.
     *
     * @param expiryPoint the time point after which this entry should expire
     * @throws IllegalArgumentException if the expiry point is before now
     */
    public void setTimeToLive(final Temporal expiryPoint) {
        timeToLive(expiryPoint);
    }

    /**
     * Converts a {@link Temporal} point to a {@link Duration}.
     *
     * @param pointInTime the point in time
     * @return the duration from now
     * @throws IllegalArgumentException if the resulting duration is negative
     */
    private static Optional<Duration> until(final Temporal pointInTime) {
        Duration ttl = Duration.between(LocalDateTime.now(), pointInTime);
        if (ttl.isNegative()) {
            throw new IllegalArgumentException("negative time to live specified!");
        }
        return Optional.of(ttl);
    }

    /**
     * Converts the given milliseconds into a {@link Duration}.
     *
     * @param ttlMillis the milliseconds to convert
     * @return a {@link Duration}
     * @throws IllegalArgumentException if <code>ttlMillis</code> is negative
     */
    private static Optional<Duration> toDuration(final long ttlMillis) {
        if (ttlMillis < 0) {
            throw new IllegalArgumentException("negative time to live specified!");
        }
        return Optional.of(Duration.ofMillis(ttlMillis));
    }

    /**
     * Cancels any time to live set on this cache request.
     */
    public void cancelTimeToLive() {
        this.timeToLive = Optional.empty();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("timeToLive", timeToLive)
                .append("value", value)
                .append("locallyCacheable", locallyCacheable)
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

        AddCacheRequest that = (AddCacheRequest) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(timeToLive, that.timeToLive)
                .append(value, that.value)
                .append(locallyCacheable, that.locallyCacheable)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(11, 47)
                .appendSuper(super.hashCode())
                .append(timeToLive)
                .append(value)
                .append(locallyCacheable)
                .toHashCode();
    }
}
