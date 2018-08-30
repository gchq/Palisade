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

import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.ToStringBuilder;
import uk.gov.gchq.palisade.service.request.DataRequestConfig;
import uk.gov.gchq.palisade.service.request.Request;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * This class is used to request that a {@link DataRequestConfig} relating to a
 * unique {@link RequestId}, that is set when the client registers there request
 * for data, is added to the cache in the cache service.
 */
public class AddCacheRequest extends Request {
    private RequestId requestId;
    private DataRequestConfig dataRequestConfig;
    /**
     * An empty optional indicates no time to live specified.
     */
    private Optional<Duration> timeToLive = Optional.empty();

    public AddCacheRequest() {
    }

    public AddCacheRequest requestId(final RequestId requestId) {
        requireNonNull(requestId, "The request id cannot be set to null.");
        this.requestId = requestId;
        return this;
    }

    public AddCacheRequest dataRequestConfig(final DataRequestConfig dataRequestConfig) {
        requireNonNull(dataRequestConfig, "The data request config cannot be set to null.");
        this.dataRequestConfig = dataRequestConfig;
        return this;
    }

    public AddCacheRequest timeToLive(final long ttlMillis) {
        this.timeToLive = toDuration(ttlMillis);
        return this;
    }

    public AddCacheRequest timeToLive(final Temporal expiryPoint) {
        requireNonNull(expiryPoint, "The expiry point cannot be set to null.");
        this.timeToLive = until(expiryPoint);
        return this;
    }

    public AddCacheRequest timeToLive(final Optional<Duration> timeToLive) {
        requireNonNull(timeToLive, "The time to live cannot be set to null.");
        timeToLive.ifPresent(x -> {
            if (x.isNegative()) {
                throw new IllegalArgumentException("negative time to live specified!");
            }
        });
        this.timeToLive = timeToLive;
        return this;
    }

    public RequestId getRequestId() {
        requireNonNull(requestId, "The request id has not been set.");
        return requestId;
    }

    public DataRequestConfig getDataRequestConfig() {
        requireNonNull(dataRequestConfig, "The data request config has not been set.");
        return dataRequestConfig;
    }

    public Optional<Duration> getTimeToLive() {
        // this will never be null
        return timeToLive;
    }

    public void setRequestId(final RequestId requestId) {
        requestId(requestId);
    }

    public void setDataRequestConfig(final DataRequestConfig dataRequestConfig) {
        dataRequestConfig(dataRequestConfig);
    }

    /**
     * Sets the time to live for this cache entry. If the given ${@code Optional} is empty, then no time to live is
     * assumed.
     *
     * @param timeToLive the TTL for this cache entry
     * @throws IllegalArgumentException if the duration is negative
     */
    public void setTimeToLive(final Optional<Duration> timeToLive) {
        timeToLive(timeToLive);
    }

    public void setTimeToLive(final long ttlMillis) {
        timeToLive(ttlMillis);
    }

    public void setTimeToLive(final Temporal expiryPoint) {
       timeToLive(expiryPoint);
    }

    public void cancelTimeToLive() {
        this.timeToLive = Optional.empty();
    }

    private static Optional<Duration> toDuration(final long ttlMillis) {
        if (ttlMillis < 0) {
            throw new IllegalArgumentException("negative time to live specified!");
        }
        return Optional.of(Duration.ofMillis(ttlMillis));
    }

    private static Optional<Duration> until(final Temporal pointInTime) {
        Duration ttl = Duration.between(LocalDateTime.now(), pointInTime);
        return Optional.of(ttl);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final AddCacheRequest that = (AddCacheRequest) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(requestId, that.requestId)
                .append(dataRequestConfig, that.dataRequestConfig)
                .append(timeToLive, that.timeToLive)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(23, 37)
                .appendSuper(super.hashCode())
                .append(requestId)
                .append(dataRequestConfig)
                .append(timeToLive)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("requestId", requestId)
                .append("dataRequestConfig", dataRequestConfig)
                .append("TTL", timeToLive)
                .toString();
    }
}
