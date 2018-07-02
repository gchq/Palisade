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
    private Optional<Duration> timeToLive;

    public AddCacheRequest() {
    }

    public AddCacheRequest(final RequestId requestId, final DataRequestConfig dataRequestConfig) {
        this(requestId, dataRequestConfig, Optional.empty());
    }

    public AddCacheRequest(final RequestId requestId, final DataRequestConfig dataRequestConfig, final long ttlMillis) {
        this(requestId, dataRequestConfig, toDuration(ttlMillis));
    }

    public AddCacheRequest(final RequestId requestId, final DataRequestConfig dataRequestConfig, final Temporal expiryPoint) {
        this(requestId, dataRequestConfig, until(expiryPoint));
    }

    public AddCacheRequest(final RequestId requestId, final DataRequestConfig dataRequestConfig, final Optional<Duration> timeToLive) {
        this.requestId = requestId;
        this.dataRequestConfig = dataRequestConfig;
        this.timeToLive = timeToLive;
    }

    public RequestId getRequestId() {
        return requestId;
    }

    public DataRequestConfig getDataRequestConfig() {
        return dataRequestConfig;
    }

    public Optional<Duration> getTimeToLive() {
        return timeToLive;
    }

    public void setRequestId(final RequestId requestId) {
        this.requestId = requestId;
    }

    public void setDataRequestConfig(final DataRequestConfig dataRequestConfig) {
        this.dataRequestConfig = dataRequestConfig;
    }

    /**
     * Sets the time to live for this cache entry. If the given ${@code Optional} is empty, then no time to live is
     * assumed.
     *
     * @param timeToLive the TTL for this cache entry
     * @throws IllegalArgumentException if the duration is negative
     */
    public void setTimeToLive(final Optional<Duration> timeToLive) {
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
                .append(requestId, that.requestId)
                .append(dataRequestConfig, that.dataRequestConfig)
                .append(timeToLive, that.timeToLive)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(23, 37)
                .append(requestId)
                .append(dataRequestConfig)
                .append(timeToLive)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("requestId", requestId)
                .append("dataRequestConfig", dataRequestConfig)
                .append("TTL", timeToLive)
                .toString();
    }
}
