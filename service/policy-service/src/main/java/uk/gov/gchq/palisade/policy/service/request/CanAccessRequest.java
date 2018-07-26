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

package uk.gov.gchq.palisade.policy.service.request;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import uk.gov.gchq.palisade.Justification;
import uk.gov.gchq.palisade.ToStringBuilder;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.service.request.Request;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * This class is used to request whether a user can access a resource for a given justification.
 */
public class CanAccessRequest extends Request {
    private User user;
    private Collection<Resource> resources;
    private Justification justification;

    // no-args constructor required
    public CanAccessRequest() {
    }

    /**
     * Default constructor
     *
     * @param resources A collection of {@link Resource}'s to be accessed.
     * @param user The {@link User} wanting access to the resource.
     * @param justification The {@link Justification} of why the user needs
     *                      access to the resource.
     */
    public CanAccessRequest(final Collection<Resource> resources, final User user, final Justification justification) {
        this.user = user;
        this.resources = resources;
        this.justification = justification;
    }

    /**
     * Utility method to allow the CanAccessRequest to be created as part of a
     * chain of asynchronous requests.
     *
     * @param futureResources a completable future that will return a collection of {@link Resource}'s.
     * @param futureUser a completable future that will return a {@link User}.
     * @param justification the justification that the user stated for why they want access to the data.
     * @return a completable future containing the {@link CanAccessRequest}.
     */
    public static CompletableFuture<CanAccessRequest> create(
            final CompletableFuture<? extends Collection<Resource>> futureResources,
            final CompletableFuture<User> futureUser,
            final Justification justification) {
        return CompletableFuture.allOf(futureResources, futureUser)
                .thenApply(t -> new CanAccessRequest(futureResources.join(), futureUser.join(), justification));
    }

    /**
     * Utility method to allow the CanAccessRequest to be created as part of a
     * chain of asynchronous requests.
     *
     * @param resources a collection of {@link Resource}'s that the user wants access to.
     * @param futureUser a completable future that will return a {@link User}.
     * @param justification the justification that the user stated for why they want access to the data.
     * @return a completable future containing the {@link CanAccessRequest}.
     */
    public static CompletableFuture<CanAccessRequest> create(
            final Collection<Resource> resources,
            final CompletableFuture<User> futureUser,
            final Justification justification) {
        return futureUser.thenApply(auths -> new CanAccessRequest(resources, futureUser.join(), justification));
    }

    public Collection<Resource> getResources() {
        return resources;
    }

    public void setResource(final Collection<Resource> resources) {
        this.resources = resources;
    }

    public User getUser() {
        return user;
    }

    public void setUser(final User user) {
        this.user = user;
    }

    public Justification getJustification() {
        return justification;
    }

    public void setJustification(final Justification justification) {
        this.justification = justification;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final CanAccessRequest that = (CanAccessRequest) o;

        return new EqualsBuilder()
                .append(user, that.user)
                .append(resources, that.resources)
                .append(justification, that.justification)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(11, 13)
                .append(user)
                .append(resources)
                .append(justification)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("user", user)
                .append("resources", resources)
                .append("justification", justification)
                .toString();
    }
}
