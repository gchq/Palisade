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

import uk.gov.gchq.palisade.ToStringBuilder;
import uk.gov.gchq.palisade.policy.service.Policy;
import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.service.request.Request;

/**
 * This class is used in the request to set a {@link Policy} for a {@link Resource}.
 * That resource may be signifying a file, stream, directory or the system
 * (policy is applied to all requests to the Palisade system).
 */
public class SetPolicyRequest extends Request {
    private Resource resource;
    private Policy policy;

    // no-args constructor
    public SetPolicyRequest() {
    }

    /**
     * Default constructor
     *
     * @param resource The {@link Resource} to set the {@link Policy} for.
     * @param policy The {@link Policy} to set for the {@link Resource}.
     */
    public SetPolicyRequest(final Resource resource,
                            final Policy policy) {
        this.resource = resource;
        this.policy = policy;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(final Resource resource) {
        this.resource = resource;
    }

    public Policy getPolicy() {
        return policy;
    }

    public void setPolicy(final Policy policy) {
        this.policy = policy;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final SetPolicyRequest that = (SetPolicyRequest) o;

        return new EqualsBuilder()
                .append(resource, that.resource)
                .append(policy, that.policy)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 29)
                .append(resource)
                .append(policy)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("resource", resource)
                .append("policy", policy)
                .toString();
    }
}
