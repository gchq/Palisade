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

/**
 * This class is used in the request to get the policies that apply to the
 * collection of {@link Resource}'s.
 */
public class GetPolicyRequest extends Request {
    private User user;
    private Justification justification;
    private Collection<Resource> resources;

    // no-args constructor
    public GetPolicyRequest() {
    }

    /**
     *
     * @param user The {@link User} wanting access to the resource.
     * @param justification The {@link Justification} of why the user needs
     * @param resources A collection of {@link Resource}'s to be accessed.
     */
    public GetPolicyRequest(final User user, final Justification justification, final Collection<Resource> resources) {
        this.user = user;
        this.justification = justification;
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
                .append(user, that.user)
                .append(justification, that.justification)
                .append(resources, that.resources)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 29)
                .append(user)
                .append(justification)
                .append(resources)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("user", user)
                .append("justification", justification)
                .append("resources", resources)
                .toString();
    }
}
