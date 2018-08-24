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

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import static java.util.Objects.requireNonNull;

/**
 * This class is used to request whether a user can access a resource for a given justification.
 */
public class CanAccessRequest extends Request {
    private User user;
    private Collection<Resource> resources;
    private Context context;

    // no-args constructor required
    public CanAccessRequest() {
    }

    /**
     * @param resources the collection of {@link Resource}'s to be accessed
     * @return the {@link CanAccessRequest}
     */
    public CanAccessRequest resources(final Collection<Resource> resources) {
        requireNonNull(resources, "The resources cannot be set to null.");
        this.resources = resources;
        return this;
    }

    /**
     * @param user the {@link User} wanting access to the resource
     * @return the {@link CanAccessRequest}
     */
    public CanAccessRequest user(final User user) {
        requireNonNull(user, "The user cannot be set to null.");
        this.user = user;
        return this;
    }

    /**
     * @param context containing contextual information such as justification or environmental data that can influence policies
     * @return the {@link CanAccessRequest}
     */
    public CanAccessRequest context(final Context context) {
        requireNonNull(context, "The context cannot be set to null.");
        this.context = context;
        return this;
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
            final String justification) {
        return CompletableFuture.allOf(futureResources, futureUser)
                .thenApply(t -> new CanAccessRequest().resources(futureResources.join()).user(futureUser.join()).context(new Context().justification(justification)));
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
            final String justification) {
        return futureUser.thenApply(auths -> new CanAccessRequest().resources(resources).user(futureUser.join()).context(new Context().justification(justification)));
    }

    public Collection<Resource> getResources() {
        requireNonNull(resources, "The resources have not been set.");
        return resources;
    }

    public void setResources(final Collection<Resource> resources) {
        resources(resources);
    }

    public User getUser() {
        requireNonNull(user, "The user has not been set.");
        return user;
    }

    public void setUser(final User user) {
        user(user);
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

        final CanAccessRequest that = (CanAccessRequest) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(user, that.user)
                .append(resources, that.resources)
                .append(context, that.context)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(11, 13)
                .appendSuper(super.hashCode())
                .append(user)
                .append(resources)
                .append(context)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("user", user)
                .append("resources", resources)
                .append("context", context)
                .toString();
    }
}
