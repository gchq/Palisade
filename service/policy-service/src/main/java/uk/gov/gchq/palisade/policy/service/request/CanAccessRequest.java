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

import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.ToStringBuilder;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.service.request.Request;

import java.util.concurrent.CompletableFuture;

/**
 * This class is used to request whether a user can access a resource for a given justification.
 */
public class CanAccessRequest extends Request {
    private User user;
    private Resource resource;
    private Context context;

    // no-args constructor required
    public CanAccessRequest() {
    }

    /**
     * @param resource the {@link Resource} to be accessed
     * @return the {@link CanAccessRequest}
     */
    public CanAccessRequest resource(final Resource resource) {
        this.resource = resource;
        return this;
    }

    /**
     * @param user the {@link User} wanting access to the resource
     * @return the {@link CanAccessRequest}
     */
    public CanAccessRequest user(final User user) {
        this.user = user;
        return this;
    }

    /**
     * @param justification the {@link Context} of why the user needs access to the resource
     * @return the {@link CanAccessRequest}
     */
    public CanAccessRequest justification(final String justification) {
        this.context.justification(justification);
        return this;
    }

    public CanAccessRequest context(final Context context) {
        this.context = context;
        return this;
    }

    /**
     * Utility method to allow the CanAccessRequest to be created as part of a
     * chain of asynchronous requests.
     *
     * @param futureResource a completable future that will return a {@link Resource}.
     * @param futureUser     a completable future that will return a {@link User}.
     * @param justification  the justification that the user stated for why they want access to the data.
     * @return a completable future containing the {@link CanAccessRequest}.
     */
    public static CompletableFuture<CanAccessRequest> create(
            final CompletableFuture<? extends Resource> futureResource,
            final CompletableFuture<User> futureUser,
            final String justification) {
        return CompletableFuture.allOf(futureResource, futureUser)
                .thenApply(t -> new CanAccessRequest().resource(futureResource.join()).user(futureUser.join()).justification(justification));
    }

    /**
     * Utility method to allow the CanAccessRequest to be created as part of a
     * chain of asynchronous requests.
     *
     * @param resource      a {@link Resource} that the user wants access to.
     * @param futureUser    a completable future that will return a {@link User}.
     * @param justification the justification that the user stated for why they want access to the data.
     * @return a completable future containing the {@link CanAccessRequest}.
     */
    public static CompletableFuture<CanAccessRequest> create(
            final Resource resource,
            final CompletableFuture<User> futureUser,
            final String justification) {
        return futureUser.thenApply(auths -> new CanAccessRequest().resource(resource).user(futureUser.join()).justification(justification));
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(final Resource resource) {
        this.resource = resource;
    }

    public User getUser() {
        return user;
    }

    public void setUser(final User user) {
        this.user = user;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(final Context context) {
        this.context = context;
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
                .appendSuper(super.equals(o))
                .append(user, that.user)
                .append(resource, that.resource)
                .append(context, that.context)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(11, 13)
                .appendSuper(super.hashCode())
                .append(user)
                .append(resource)
                .append(context)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("user", user)
                .append("resource", resource)
                .append("context", context)
                .toString();
    }
}
