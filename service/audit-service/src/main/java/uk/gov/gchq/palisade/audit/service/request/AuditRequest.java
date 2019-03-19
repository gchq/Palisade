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

import uk.gov.gchq.palisade.AuditType;
import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.ToStringBuilder;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.service.request.Request;

import static java.util.Objects.requireNonNull;

/**
 * This is the object that is passed to the {@link uk.gov.gchq.palisade.audit.service.AuditService}
 * to be able to store an audit record. The default information is what resources
 * was being accessed, who is accessing the resource, why are they accessing it
 * and what filters/transformations are being applied to the data.
 * An optional extra is to add an exception to be able to log errors that might
 * occur so the audit log reflects that actually the user did not get access to
 * the data, and why that was.
 */
public class AuditRequest extends Request {
    private Context context;
    private User user;
    private AuditType auditType;
    private LeafResource resource;
    private String howItWasProcessed;
    private Exception exception;

    // no-arg constructor required
    public AuditRequest() {
    }

    /**
     * @param context {@link Context} is the reason for the
     *                user accessing the resource
     * @return the {@link AuditRequest}
     */
    public AuditRequest context(final Context context) {
        requireNonNull(context, "The context cannot be set to null.");
        this.context = context;
        return this;
    }

    /**
     * @param user {@link User} which is the user accessing the
     *             resource
     * @return the {@link AuditRequest}
     */
    public AuditRequest user(final User user) {
        requireNonNull(user, "The user cannot be set to null.");
        this.user = user;
        return this;
    }

    /**
     * @param auditType {@link Context} is the type of the request access
     * @return the {@link AuditRequest}
     */
    public AuditRequest auditType(final AuditType auditType) {
        requireNonNull(auditType, "The audit type cannot be null");
        this.auditType = auditType;
        return this;
    }

    /**
     * @param resource {@link LeafResource} which contains the relevant
     *                 details about the resource being accessed
     * @return the {@link AuditRequest}
     */
    public AuditRequest resource(final LeafResource resource) {
        requireNonNull(resource, "The resource cannot be set to null.");
        this.resource = resource;
        return this;
    }

    /**
     * @param howItWasProcessed {@link String} is an explanation of what
     *                          filtering/transformations are being applied to
     *                          the data returned to the user
     * @return the {@link AuditRequest}
     */
    public AuditRequest howItWasProcessed(final String howItWasProcessed) {
        requireNonNull(howItWasProcessed, "The how it was processed message cannot be set to null.");
        this.howItWasProcessed = howItWasProcessed;
        return this;
    }

    /**
     * @param exception {@link Exception} thrown while trying to access the data
     * @return the {@link AuditRequest}
     */
    public AuditRequest exception(final Exception exception) {
        requireNonNull(exception, "The exception can not be set to null.");
        this.exception = exception;
        return this;
    }

    public Context getContext() {
        requireNonNull(context, "The context has not been set.");
        return context;
    }

    public void setContext(final Context context) {
        context(context);
    }

    public User getUser() {
        requireNonNull(user, "The user has not been set.");
        return user;
    }

    public void setUser(final User user) {
        user(user);
    }

    public AuditType getAuditType() {
        requireNonNull(auditType, "The audit type cannot be null");
        return auditType;
    }

    public void setAuditType(final AuditType auditType) {
        this.auditType = auditType;
    }

    public LeafResource getResource() {
        requireNonNull(resource, "The resource have not been set.");
        return resource;
    }

    public String getHowItWasProcessed() {
        //This is acceptable being null
        return howItWasProcessed;
    }

    public Exception getException() {
        // this is acceptable being null
        return exception;
    }

    public void setResources(final LeafResource resource) {
        resource(resource);
    }

    public void setHowItWasProcessed(final String howItWasProcessed) {
        howItWasProcessed(howItWasProcessed);
    }

    public void setException(final Exception exception) {
        exception(exception);
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
                .append(user, that.user)
                .append(auditType, that.auditType)
                .append(resource, that.resource)
                .append(howItWasProcessed, that.howItWasProcessed)
                .append(exception, that.exception)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 37)
                .appendSuper(super.hashCode())
                .append(context)
                .append(user)
                .append(auditType)
                .append(resource)
                .append(howItWasProcessed)
                .append(exception)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("purpose", context)
                .append("user", user)
                .append("auditType", auditType)
                .append("resource", resource)
                .append("howItWasProcessed", howItWasProcessed)
                .append("exception", exception)
                .toString();
    }
}