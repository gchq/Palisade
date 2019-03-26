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

package uk.gov.gchq.palisade.audit.service.request;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.ToStringBuilder;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.service.request.Request;

import static java.util.Objects.requireNonNull;

/**
 * This is the abstract class that is passed to the {@link uk.gov.gchq.palisade.audit.service.AuditService}
 * to be able to store an audit record. The default information is what resources
 * was being accessed.
 */
public abstract class AuditRequest extends Request {
    private Context context;

    // no-arg constructor required
    public AuditRequest() {
    }

    /**
     * @return the auditLog representation of this class
     */
    public abstract String constructAuditLog();

    /**
     * @param <T>     {@link AuditRequest} derived class from AuditRequest used for chaining
     * @param context {@link Context} is the reason for the
     *                user accessing the resource
     * @return the {@link AuditRequest}
     */
    public <T extends AuditRequest> T context(final Context context) {
        requireNonNull(context, "The context cannot be set to null.");
        this.context = context;
        return (T) this;
    }

    /**
     * @param <T> {@link AuditRequest} derived class from AuditRequest used for chaining
     * @param ex  {@link Throwable} is the exception thrown
     * @return the {@link AuditRequest}
     */
    public <T extends AuditRequest> T exception(final Throwable ex) {
        //can be null
        return (T) this;
    }

    /**
     * @param <T>  {@link AuditRequest} derived class from AuditRequest used for chaining
     * @param user {@link User} is the user for this resource
     * @return the {@link AuditRequest}
     */
    public <T extends AuditRequest> T user(final User user) {
        //can be null
        return (T) this;
    }

    /**
     * @param <T>       {@link AuditRequest} derived class from AuditRequest used for chaining
     * @param requestId {@link RequestId} is the requestId for this resource
     * @return the {@link AuditRequest}
     */
    public <T extends AuditRequest> T requestId(final RequestId requestId) {
        //can be null
        return (T) this;
    }

    /**
     * @param <T>      {@link AuditRequest} derived class from AuditRequest used for chaining
     * @param resource {@link LeafResource} which contains the relevant
     *                 details about the resource being accessed
     * @return the {@link AuditRequest}
     */
    public <T extends AuditRequest> T resource(final LeafResource resource) {
        //can be null
        return (T) this;
    }

    /**
     * @param <T>               {@link AuditRequest} derived class from AuditRequest used for chaining
     * @param howItWasProcessed {@link String} is an explanation of what
     *                          filtering/transformations are being applied to
     *                          the data returned to the user
     * @return the {@link AuditRequest}
     */
    public <T extends AuditRequest> T howItWasProcessed(final String howItWasProcessed) {
        //can be null
        return (T) this;
    }

    public Context getContext() {
        requireNonNull(context, "The context has not been set.");
        return context;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final AuditRequest that = (AuditRequest) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(context, that.context)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 37)
                .appendSuper(super.hashCode())
                .append(context)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("purpose", context)
                .toString();
    }

}
