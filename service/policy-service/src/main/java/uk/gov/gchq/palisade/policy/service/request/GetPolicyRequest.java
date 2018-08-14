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
import java.util.Collections;

/**
 * This class is used in the request to get the policies that apply to the
 * collection of {@link Resource}'s.
 */
public class GetPolicyRequest extends Request {
    private User user = new User();
    private Context context = new Context();
    private Collection<Resource> resources = Collections.emptyList();

    // no-args constructor
    public GetPolicyRequest() {
    }

    /**
     * @param user the {@link User} wanting access to the resource
     * @return the {@link GetPolicyRequest}
     */
    public GetPolicyRequest user(final User user) {
        this.user = user;
        return this;
    }

    /**
     * @param justification the {@link Context} of why the user needs
     * @return the {@link GetPolicyRequest}
     */
    public GetPolicyRequest justification(final String justification) {
        this.context.justification(justification);
        return this;
    }

    /**
     * @param resources a collection of {@link Resource}'s to be accessed
     * @return the {@link GetPolicyRequest}
     */
    public GetPolicyRequest resources(final Collection<Resource> resources) {
        this.resources = resources;
        return this;
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

    public GetPolicyRequest context(final Context context) {
        this.context = context;
        return this;
    }

    public Collection<Resource> getResources() {
        return resources;
    }

    public void setResources(final Collection<Resource> resources) {
        this.resources = resources;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final GetPolicyRequest that = (GetPolicyRequest) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(user, that.user)
                .append(context, that.context)
                .append(resources, that.resources)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 29)
                .appendSuper(super.hashCode())
                .append(user)
                .append(context)
                .append(resources)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("user", user)
                .append("context", context)
                .append("resources", resources)
                .toString();
    }
}
