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

/**
 * This class is used to request that a {@link DataRequestConfig} relating to a
 * unique {@link RequestId}, that is set when the client registers there request
 * for data, is added to the cache in the cache service.
 */
public class AddCacheDataRequest extends AddCacheRequest {
    private RequestId requestId;
    private DataRequestConfig dataRequestConfig;

    public AddCacheDataRequest() {
    }

    public AddCacheDataRequest requestId(final RequestId requestId) {
        this.requestId = requestId;
        return this;
    }

    public AddCacheDataRequest dataRequestConfig(final DataRequestConfig dataRequestConfig) {
        this.dataRequestConfig = dataRequestConfig;
        return this;
    }

    public RequestId getRequestId() {
        return requestId;
    }

    public DataRequestConfig getDataRequestConfig() {
        return dataRequestConfig;
    }

    public void setRequestId(final RequestId requestId) {
        this.requestId = requestId;
    }

    public void setDataRequestConfig(final DataRequestConfig dataRequestConfig) {
        this.dataRequestConfig = dataRequestConfig;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final AddCacheDataRequest that = (AddCacheDataRequest) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(requestId, that.requestId)
                .append(dataRequestConfig, that.dataRequestConfig)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(23, 37)
                .appendSuper(super.hashCode())
                .append(requestId)
                .append(dataRequestConfig)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("requestId", requestId)
                .append("dataRequestConfig", dataRequestConfig)
                .toString();
    }
}
