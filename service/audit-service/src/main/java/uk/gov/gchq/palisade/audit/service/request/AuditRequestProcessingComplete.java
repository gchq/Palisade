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

import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.ToStringBuilder;
import uk.gov.gchq.palisade.User;

import static java.util.Objects.requireNonNull;

/**
 * This is one of the objects that is passed to the {@link uk.gov.gchq.palisade.audit.service.AuditService}
 * to be able to store an audit record. This class extends {@link AuditRequest} This class
 * is used for the indication to the Audit logs that processing has been completed.
 */
public class AuditRequestProcessingComplete extends AuditRequest {
    private User user;
    RequestId requestId;

    public AuditRequestProcessingComplete() {
    }

    @Override
    public String constructAuditLog() {
        final String msg = "AuditRequestProcessingComplete: " + getUser().getUserId().getId()
                + "' accessed '" + getRequestId().getId();
        return msg;
    }

    /**
     * @param <T>     {@link AuditRequest} derived class from AuditRequest used for chaining
     * @param context {@link Context} is the reason for the
     *                user accessing the resource
     * @return the {@link AuditRequest}
     */
    public <T extends AuditRequest> T context(final Context context) {
        requireNonNull(context, "The context cannot be set to null.");
        return super.context(context);
    }

    /**
     * @param <T>  {@link AuditRequest} derived class from AuditRequest used for chaining
     * @param user {@link User} is the user for this resource
     * @return the {@link AuditRequest}
     */
    public <T extends AuditRequest> T user(final User user) {
        requireNonNull(user, "The user cannot be set to null.");
        this.user = user;
        return super.user(user);
    }

    /**
     * @param <T>       {@link AuditRequest} derived class from AuditRequest used for chaining
     * @param requestId {@link RequestId} is the requestId for this resource
     * @return the {@link AuditRequest}
     */
    public <T extends AuditRequest> T requestId(final RequestId requestId) {
        requireNonNull(requestId, "The requestId cannot be set to null.");
        this.requestId = requestId;
        return super.user(user);
    }

    public RequestId getRequestId() {
        requireNonNull(requestId, "The requestId has not been set.");
        return requestId;
    }

    public User getUser() {
        requireNonNull(user, "The user has not been set.");
        return user;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final AuditRequestProcessingComplete that = (AuditRequestProcessingComplete) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(user, that.user)
                .append(requestId, that.requestId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(20, 39)
                .appendSuper(super.hashCode())
                .append(user)
                .append(requestId)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("user", user)
                .append("requestId", requestId)
                .toString();
    }

}
