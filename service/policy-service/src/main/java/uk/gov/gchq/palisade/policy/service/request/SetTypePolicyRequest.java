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
import uk.gov.gchq.palisade.service.request.Request;

import static java.util.Objects.requireNonNull;

/**
 * This class is used in the request to set a {@link Policy} for a resource type.
 */
public class SetTypePolicyRequest extends Request {
    private String type;
    private Policy policy;

    // no-args constructor
    public SetTypePolicyRequest() {
    }

    /**
     * @param type the {@link String} to set the {@link Policy} for
     * @return the {@link SetTypePolicyRequest}
     */
    public SetTypePolicyRequest type(final String type) {
        requireNonNull(type, "The type cannot be set to null.");
        this.type = type;
        return this;
    }

    /**
     * @param policy the {@link Policy} to set for the resource type
     * @return the {@link SetTypePolicyRequest}
     */
    public SetTypePolicyRequest policy(final Policy policy) {
        requireNonNull(policy, "The policy cannot be set to null.");
        this.policy = policy;
        return this;
    }

    public String getType() {
        requireNonNull(type, "The type has not been set.");
        return type;
    }

    public void setType(final String type) {
        type(type);
    }

    public Policy getPolicy() {
        requireNonNull(policy, "The policy has not been set.");
        return policy;
    }

    public void setPolicy(final Policy policy) {
        policy(policy);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final SetTypePolicyRequest that = (SetTypePolicyRequest) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(type, that.type)
                .append(policy, that.policy)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 29)
                .appendSuper(super.hashCode())
                .append(type)
                .append(policy)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("type", type)
                .append("policy", policy)
                .toString();
    }
}
