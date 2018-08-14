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
import uk.gov.gchq.palisade.ToStringBuilder;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.service.request.Request;

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
    private Resource resource;
    private String howItWasProcessed;
    private Exception exception;

    // no-arg constructor required
    public AuditRequest() {
    }

    /**
     * @param context {@link Context} is the reason for the
     *                      user accessing the resource
     * @return the {@link AuditRequest}
     */
    public AuditRequest context(final Context context) {
        this.context = context;
        return this;
    }

    /**
     * @param user {@link User} which is the user accessing the
     *             resource
     * @return the {@link AuditRequest}
     */
    public AuditRequest user(final User user) {
        this.user = user;
        return this;
    }

    /**
     * @param resource {@link Resource} which contains the relevant
     *                 details about the resource being accessed
     * @return the {@link AuditRequest}
     */
    public AuditRequest resource(final Resource resource) {
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
        this.howItWasProcessed = howItWasProcessed;
        return this;
    }

    /**
     * @param exception {@link Exception} thrown while trying to access the data
     * @return the {@link AuditRequest}
     */
    public AuditRequest exception(final Exception exception) {
        this.exception = exception;
        return this;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(final Context context) {
        this.context = context;
    }

    public User getUser() {
        return user;
    }

    public void setUser(final User user) {
        this.user = user;
    }

    public Resource getResource() {
        return resource;
    }

    public String getHowItWasProcessed() {
        return howItWasProcessed;
    }

    public Exception getException() {
        return exception;
    }

    public void setResource(final Resource resource) {
        this.resource = resource;
    }

    public void setHowItWasProcessed(final String howItWasProcessed) {
        this.howItWasProcessed = howItWasProcessed;
    }

    public void setException(final Exception exception) {
        this.exception = exception;
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
                .append(resource)
                .append(howItWasProcessed)
                .append(exception)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("justification", context)
                .append("user", user)
                .append("resource", resource)
                .append("howItWasProcessed", howItWasProcessed)
                .append("exception", exception)
                .toString();
    }
}
