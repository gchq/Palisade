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

package uk.gov.gchq.palisade.policy.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import uk.gov.gchq.palisade.ToStringBuilder;
import uk.gov.gchq.palisade.policy.Rules;
import uk.gov.gchq.palisade.resource.Resource;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * This class contains the mapping of {@link Resource}'s to the applicable {@link Policy}
 */
public class MultiPolicy {
    private Map<Resource, Policy> policies;

    // no-args constructor required
    public MultiPolicy() {
        this(new HashMap<>());
    }

    /**
     * Default constructor
     *
     * @param policies a mapping of {@link Resource}'s to the applicable {@link Policy}
     */
    public MultiPolicy(final Map<Resource, Policy> policies) {
        this.policies = policies;
    }

    public Map<Resource, Policy> getPolicies() {
        return policies;
    }

    public void setPolicies(final Map<Resource, Policy> policies) {
        this.policies = policies;
    }

    /**
     * Retrieves the {@link Policy} associated with the given {@link Resource}.
     * If the resource does not exist then an empty {@link Policy} will be returned.
     *
     * @param resource the resource that you want the {@link Policy} for.
     * @return The {@link Policy} for the given {@link Resource}.
     */
    public Policy getPolicy(final Resource resource) {
        Objects.requireNonNull(resource);
        final Policy policy = policies.get(resource);
        if (null != policy) {
            return policy;
        }

        return new Policy();
    }

    /**
     * Sets the given {@link Policy} to the given {@link Resource} provided
     * there isn't already a {@link Policy} assigned to that {@link Resource}.
     *
     * @param resource the resource that you want the {@link Policy} for.
     * @param policy The {@link Policy} for the given {@link Resource}.
     */
    public void setPolicy(final Resource resource, final Policy policy) {
        Objects.requireNonNull(resource);
        Objects.requireNonNull(policy);
        if (policies.containsKey(resource)) {
            throw new IllegalArgumentException("Policy already exists for resource: " + resource);
        }

        policies.put(resource, policy);
    }

    /**
     * This extracts the list of rules from the {@link Policy} attached to each {@link Resource}.
     *
     * @return a mapping of the {@link Resource}'s to the {@link Rules} from the policies.
     */
    @JsonIgnore
    public Map<Resource, Rules> getRuleMap() {
        final Map<Resource, Rules> rules = new HashMap<>(getPolicies().size());
        getPolicies().forEach((r, p) -> rules.put(r, p.getRules()));
        return rules;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final MultiPolicy that = (MultiPolicy) o;

        return new EqualsBuilder()
                .append(policies, that.policies)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(47, 53)
                .append(policies)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("policies", policies)
                .toString();
    }
}
