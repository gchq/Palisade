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

import uk.gov.gchq.palisade.Justification;
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
    private Justification justification;
    private User user;
    private Resource resource;
    private String howItWasProcessed;
    private Exception exception;

    // no-arg constructor required
    public AuditRequest() {
    }

    /**
     * The default constructor if there are no errors
     *
     * @param resource          {@link Resource} which contains the relevant
     *                          details about the resource being accessed
     * @param user              {@link User} which is the user accessing the
     *                          resource
     * @param justification     {@link Justification} is the reason for the
     *                          user accessing the resource
     * @param howItWasProcessed {@link String} is an explanation of what
     *                          filtering/transformations are being applied to
     *                          the data returned to the user
     */
    public AuditRequest(final Resource resource, final User user,
                        final Justification justification,
                        final String howItWasProcessed) {
        this(resource, user, justification, howItWasProcessed, null);
    }

    /**
     * The default constructor if there are errors
     *
     * @param resource          {@link Resource} which contains the relevant
     *                          details about the resource being accessed
     * @param user              {@link User} which is the user accessing the
     *                          resource
     * @param justification     {@link Justification} is the reason for the
     *                          user accessing the resource
     * @param howItWasProcessed {@link String} is an explanation of what
     *                          filtering/transformations are being applied to
     *                          the data returned to the user
     * @param exception         {@link Exception} thrown while trying to access the data
     */
    public AuditRequest(final Resource resource, final User user,
                        final Justification justification,
                        final String howItWasProcessed,
                        final Exception exception) {
        this.user = user;
        this.justification = justification;
        this.resource = resource;
        this.howItWasProcessed = howItWasProcessed;
        this.exception = exception;
    }

    public Justification getJustification() {
        return justification;
    }

    public void setJustification(final Justification justification) {
        this.justification = justification;
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
                .append(justification, that.justification)
                .append(user, that.user)
                .append(resource, that.resource)
                .append(howItWasProcessed, that.howItWasProcessed)
                .append(exception, that.exception)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 37)
                .append(justification)
                .append(user)
                .append(resource)
                .append(howItWasProcessed)
                .append(exception)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("justification", justification)
                .append("user", user)
                .append("resource", resource)
                .append("howItWasProcessed", howItWasProcessed)
                .append("exception", exception)
                .toString();
    }
}
