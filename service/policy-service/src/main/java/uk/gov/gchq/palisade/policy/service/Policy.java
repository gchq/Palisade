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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import uk.gov.gchq.palisade.ToStringBuilder;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.policy.PredicateRule;
import uk.gov.gchq.palisade.policy.Rule;
import uk.gov.gchq.palisade.policy.Rules;
import uk.gov.gchq.palisade.resource.Resource;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * This class is used to store the information that is required by the policy
 * service but not needed by the rest of the palisade services. That includes
 * separating the rules that need to be applied at the resource level or the record level.
 *
 * @param <RULE_DATA_TYPE> The Java class that the rules expect the records of
 *                        data to be in the format of.
 */
public class Policy<RULE_DATA_TYPE> {
    private Rules<RULE_DATA_TYPE> recordRules;
    private Rules<Resource> resourceRules;
    private User owner;

    // no-args constructor required
    public Policy() {
        this(new Rules<>(), new Rules<>());
    }

    /**
     * Default constructor
     *
     * @param recordRules the set of rules that need to be applied to the resource at the record level.
     * @param resourceRules the set of rules that need to be applied to the resource at the resource level.
     */
    public Policy(final Rules<RULE_DATA_TYPE> recordRules, final Rules<Resource> resourceRules) {
        Objects.requireNonNull(recordRules);
        Objects.requireNonNull(resourceRules);
        this.recordRules = recordRules;
        this.resourceRules = resourceRules;
    }

    public String getMessage() {
        return resourceRules.getMessage() + ", " + recordRules.getMessage();
    }

    public Rules<RULE_DATA_TYPE> getRecordRules() {
        return recordRules;
    }

    public void setRecordRules(final Rules<RULE_DATA_TYPE> recordRules) {
        this.recordRules = recordRules;
    }

    public Rules<Resource> getResourceRules() {
        return resourceRules;
    }

    public void setResourceRules(final Rules<Resource> resourceRules) {
        this.resourceRules = resourceRules;
    }

    private String generateUUID() {
        return UUID.randomUUID().toString();
    }

    private void addMessage(String newMessage, Rules rules) {
        String currentMessage = rules.getMessage();
        if (currentMessage == null || currentMessage.equals("")) {
            rules.message(newMessage);
        } else {
            rules.message(currentMessage + ", " + newMessage);
        }
    }

    public Policy<RULE_DATA_TYPE> recordLevelRule(final String message, final Rule<RULE_DATA_TYPE> rule) {
        recordRules.rule(generateUUID(), rule);
        addMessage(message, recordRules);
        return this;
    }

    public Policy<RULE_DATA_TYPE> recordLevelPredicateRule(final String message, final PredicateRule<RULE_DATA_TYPE> rule) {
        recordRules.predicateRule(generateUUID(), rule);
        addMessage(message, recordRules);
        return this;
    }

    public Policy<RULE_DATA_TYPE> recordLevelSimplePredicateRule(final String message, final Predicate<RULE_DATA_TYPE> rule) {
        recordRules.simplePredicateRule(generateUUID(), rule);
        addMessage(message, recordRules);
        return this;
    }

    public Policy<RULE_DATA_TYPE> recordLevelSimpleFunctionRule(final String message, final Function<RULE_DATA_TYPE, RULE_DATA_TYPE> rule) {
        recordRules.simpleFunctionRule(generateUUID(), rule);
        addMessage(message, recordRules);
        return this;
    }

    public Policy<RULE_DATA_TYPE> resourceLevelRule(final String message, final Rule<Resource> rule) {
        resourceRules.rule(generateUUID(), rule);
        addMessage(message, resourceRules);
        return this;
    }

    public Policy<RULE_DATA_TYPE> resourceLevelPredicateRule(final String message, final PredicateRule<Resource> rule) {
        resourceRules.predicateRule(generateUUID(), rule);
        addMessage(message, resourceRules);
        return this;
    }

    public Policy<RULE_DATA_TYPE> resourceLevelSimplePredicateRule(final String message, final Predicate<Resource> rule) {
        resourceRules.simplePredicateRule(generateUUID(), rule);
        addMessage(message, resourceRules);
        return this;
    }

    public Policy<RULE_DATA_TYPE> resourceLevelSimpleFunctionRule(final String message, final Function<Resource, Resource> rule) {
        resourceRules.simpleFunctionRule(generateUUID(), rule);
        addMessage(message, resourceRules);
        return this;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(final User owner) {
        this.owner = owner;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Policy<?> policy = (Policy<?>) o;

        return new EqualsBuilder()
                .append(resourceRules, policy.resourceRules)
                .append(recordRules, policy.recordRules)
                .append(owner, policy.owner)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(resourceRules)
                .append(recordRules)
                .append(owner)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("resourceRules", resourceRules)
                .append("recordRules", recordRules)
                .append("owner", owner)
                .toString();
    }
}
