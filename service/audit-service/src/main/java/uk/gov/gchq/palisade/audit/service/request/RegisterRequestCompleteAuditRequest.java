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

import uk.gov.gchq.palisade.ToStringBuilder;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.resource.LeafResource;

import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * This is one of the objects that is passed to the {@link uk.gov.gchq.palisade.audit.service.AuditService}
 * to be able to store an audit record. This class extends {@link AuditRequest} This class
 * is used for the indication to the Audit logs that a RegisterDataRequest has been successfully processed and these are
 * the resources that this user is approved to read for this data access request.
 */
public class RegisterRequestCompleteAuditRequest extends AuditRequest {

    private User user;
    private Set<LeafResource> leafResources;

    // no-arg constructor required
    public RegisterRequestCompleteAuditRequest() {
    }

    /**
     * @param user {@link User} is the user that made the initial registration request to access data
     * @return the {@link RegisterRequestCompleteAuditRequest}
     */
    public RegisterRequestCompleteAuditRequest user(final User user) {
        requireNonNull(user, "The user type cannot be null");
        this.user = user;
        return this;
    }

    /**
     * @param leafResources a set of {@link LeafResource} which contains the relevant details about the resource being accessed
     * @return the {@link RegisterRequestCompleteAuditRequest}
     */
    public RegisterRequestCompleteAuditRequest leafResources(final Set<LeafResource> leafResources) {
        requireNonNull(leafResources, "The leaf resources cannot be null");
        this.leafResources = leafResources;
        return this;
    }

    public User getUser() {
        requireNonNull(user, "The user has not been set.");
        return user;
    }

    public void setUser(final User user) {
        user(user);
    }

    public Set<LeafResource> getLeafResources() {
        requireNonNull(leafResources, "The leafResources has not been set.");
        return leafResources;
    }

    public void setLeafResources(final Set<LeafResource> leafResources) {
        leafResources(leafResources);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final RegisterRequestCompleteAuditRequest that = (RegisterRequestCompleteAuditRequest) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(user, that.user)
                .append(leafResources, that.leafResources)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(23, 41)
                .appendSuper(super.hashCode())
                .append(user)
                .append(leafResources)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("user", user)
                .append("leafResources", leafResources)
                .toString();
    }
}
