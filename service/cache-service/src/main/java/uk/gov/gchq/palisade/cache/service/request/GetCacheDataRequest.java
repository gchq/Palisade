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

/**
 * This class is used for sending a request to get the
 * {@link uk.gov.gchq.palisade.service.request.DataRequestConfig} out of the
 * cache for the given {@link RequestId}.
 */
public class GetCacheDataRequest extends GetCacheRequest {
    private RequestId requestId;

    public GetCacheDataRequest() {
    }

    public GetCacheDataRequest requestId(final RequestId requestId) {
        this.requestId = requestId;
        return this;
    }

    public RequestId getRequestId() {
        return requestId;
    }

    public void setRequestId(final RequestId requestId) {
        this.requestId = requestId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final GetCacheDataRequest that = (GetCacheDataRequest) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(requestId, that.requestId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 29)
                .appendSuper(super.hashCode())
                .append(requestId)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("requestId", requestId)
                .toString();
    }
}
