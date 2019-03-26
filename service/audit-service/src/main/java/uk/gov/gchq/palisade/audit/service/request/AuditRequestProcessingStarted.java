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
import uk.gov.gchq.palisade.ToStringBuilder;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.resource.LeafResource;

import static java.util.Objects.requireNonNull;

/**
 * This is one of the objects that is passed to the {@link uk.gov.gchq.palisade.audit.service.AuditService}
 * to be able to store an audit record. This class extends {@link AuditRequest} This class
 * is used for the indication to the Audit logs that processing is started.
 */
public class AuditRequestProcessingStarted extends AuditRequest {
    private User user;
    private LeafResource leafResource;
    private String howItWasProcessed;

    public AuditRequestProcessingStarted() {
    }

    @Override
    public String constructAuditLog() {
        final String msg = "AuditRequestProcessingStarted: " + getUser().getUserId().getId()
                + "' accessed '" + getLeafResource().getId()
                + "' for '" + getContext().getPurpose()
                + "' and it was processed using '" + getHowItWasProcessed();
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
        requireNonNull(user, "The user type cannot be null");
        this.user = user;
        return super.user(user);
    }

    /**
     * @param <T>      {@link AuditRequest} derived class from AuditRequest used for chaining
     * @param resource {@link LeafResource} which contains the relevant
     *                 details about the resource being accessed
     * @return the {@link AuditRequest}
     */
    public <T extends AuditRequest> T resource(final LeafResource resource) {
        requireNonNull(resource, "The leaf resource type cannot be null");
        this.leafResource = resource;
        return super.resource(resource);
    }

    /**
     * @param <T>               {@link AuditRequest} derived class from AuditRequest used for chaining
     * @param howItWasProcessed {@link String} is an explanation of what
     *                          filtering/transformations are being applied to
     *                          the data returned to the user
     * @return the {@link AuditRequest}
     */
    public <T extends AuditRequest> T howItWasProcessed(final String howItWasProcessed) {
        requireNonNull(user, "The howItWasProcessed type cannot be null");
        this.howItWasProcessed = howItWasProcessed;
        return super.howItWasProcessed(howItWasProcessed);
    }

    public User getUser() {
        requireNonNull(user, "The user has not been set.");
        return user;
    }

    public void setUser(final User user) {
        this.user = user;
    }

    public LeafResource getLeafResource() {
        requireNonNull(leafResource, "The leafResource has not been set.");
        return leafResource;
    }

    public void setLeafResource(final LeafResource leafResource) {
        this.leafResource = leafResource;
    }

    public String getHowItWasProcessed() {
        requireNonNull(howItWasProcessed, "The howItWasProcessed has not been set.");
        return howItWasProcessed;
    }

    public void setHowItWasProcessed(final String howItWasProcessed) {
        this.howItWasProcessed = howItWasProcessed;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final AuditRequestProcessingStarted that = (AuditRequestProcessingStarted) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(user, that.user)
                .append(leafResource, that.leafResource)
                .append(howItWasProcessed, that.howItWasProcessed)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(20, 39)
                .appendSuper(super.hashCode())
                .append(user)
                .append(leafResource)
                .append(howItWasProcessed)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("user", user)
                .append("leafResource", leafResource)
                .append("howItWasProcessed", howItWasProcessed)
                .toString();
    }

}
