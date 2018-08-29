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

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.Objects;
import java.util.Optional;

public class AddCacheRequest<K, V> extends CacheRequest {

    /**
     * An empty optional indicates no time to live specified.
     */
    private Optional<Duration> timeToLive = Optional.empty();

    private K key;

    private V value;

    public AddCacheRequest() {
    }

    public AddCacheRequest timeToLive(final long ttlMillis) {
        this.timeToLive = toDuration(ttlMillis);
        return this;
    }

    public AddCacheRequest timeToLive(final Temporal expiryPoint) {
        Objects.requireNonNull(expiryPoint, "expiryPoint");
        this.timeToLive = until(expiryPoint);
        return this;
    }

    public AddCacheRequest timeToLive(final Optional<Duration> timeToLive) {
        Objects.requireNonNull(timeToLive, "timeToLive");
        this.timeToLive = timeToLive;
        return this;
    }

    public Optional<Duration> getTimeToLive() {
        // can never be null
        return timeToLive;
    }

    public AddCacheRequest key(final K key) {
        Objects.requireNonNull(key, "key");
        this.key = key;
        return this;
    }

    public void setKey(final K key) {
        key(key);
    }

    public K getKey() {
        Objects.requireNonNull(key, "key cannot be null");
        return key;
    }

    public AddCacheRequest value(final V value) {
        Objects.requireNonNull(value, "value");
        this.value = value;
        return this;
    }

    public void setValue(final V value) {
        value(value);
    }

    public V getValue() {
        Objects.requireNonNull(value, "value cannot be null");
        return value;
    }

    /**
     * Sets the time to live for this cache entry. If the given {@code Optional} is empty, then no time to live is
     * assumed.
     *
     * @param timeToLive the TTL for this cache entry
     * @throws IllegalArgumentException if the duration is negative
     */
    public void setTimeToLive(final Optional<Duration> timeToLive) {
        Objects.requireNonNull(timeToLive, "timeToLive");
        timeToLive.ifPresent(x -> {
            if (x.isNegative()) {
                throw new IllegalArgumentException("negative time to live specified!");
            }
        });
        this.timeToLive = timeToLive;
    }

    public void setTimeToLive(final long ttlMillis) {
        setTimeToLive(toDuration(ttlMillis));
    }

    public void setTimeToLive(final Temporal expiryPoint) {
        setTimeToLive(until(expiryPoint));
    }

    public void cancelTimeToLive() {
        this.timeToLive = Optional.empty();
    }

    private static Optional<Duration> until(final Temporal pointInTime) {
        Duration ttl = Duration.between(LocalDateTime.now(), pointInTime);
        return Optional.of(ttl);
    }

    private static Optional<Duration> toDuration(final long ttlMillis) {
        if (ttlMillis < 0) {
            throw new IllegalArgumentException("negative time to live specified!");
        }
        return Optional.of(Duration.ofMillis(ttlMillis));
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("timeToLive", timeToLive)
                .append("key",key)
                .append("value", value)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        AddCacheRequest that = (AddCacheRequest) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(timeToLive, that.timeToLive)
                .append(key,that.key)
                .append(value,that.value)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(11, 47)
                .appendSuper(super.hashCode())
                .append(timeToLive)
                .append(key)
                .append(value)
                .toHashCode();
    }
}
