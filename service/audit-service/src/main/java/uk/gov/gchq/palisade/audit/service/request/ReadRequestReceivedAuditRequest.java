/*
 * Copyright 2019 Crown Copyright
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
package uk.gov.gchq.palisade.audit.service.request;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.ToStringBuilder;
import uk.gov.gchq.palisade.resource.LeafResource;

import static java.util.Objects.requireNonNull;

/**
 * This is one of the objects that is passed to the {@link uk.gov.gchq.palisade.audit.service.AuditService}
 * to be able to store an audit record. This class extends {@link AuditRequest} This class
 * is used for the indication to the Audit logs that a ReqadRequest has been received.
 */
public class ReadRequestReceivedAuditRequest extends AuditRequest {
    private RequestId requestId;
    private LeafResource resource;


    public ReadRequestReceivedAuditRequest() {
    }

    /**
     * @param requestId {@link RequestId} is the requestId for the ReadRequest
     * @return the {@link ReadRequestReceivedAuditRequest}
     */
    public ReadRequestReceivedAuditRequest requestId(final RequestId requestId) {
        requireNonNull(requestId, "The request id cannot be set to null.");
        this.requestId = requestId;
        return this;
    }

    /**
     * @param resource {@link LeafResource} is the leafResource for the ReadRequest
     * @return the {@link ReadRequestReceivedAuditRequest}
     */
    public ReadRequestReceivedAuditRequest resource(final LeafResource resource) {
        requireNonNull(resource, "The resource cannot be set to null.");
        this.resource = resource;
        return this;
    }

    public RequestId getRequestId() {
        requireNonNull(requestId, "The request id has not been set.");
        return requestId;
    }

    public LeafResource getResource() {
        requireNonNull(resource, "The resource has not been set.");
        return resource;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ReadRequestReceivedAuditRequest that = (ReadRequestReceivedAuditRequest) o;
        return new EqualsBuilder()
        .appendSuper(super.equals(o))
        .append(requestId, that.requestId)
        .append(resource, that.resource)
        .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(20, 39)
        .appendSuper(super.hashCode())
        .append(requestId)
        .append(resource)
        .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
        .appendSuper(super.toString())
        .append("requestId", requestId)
        .append("resource", resource)
        .toString();
    }

}
