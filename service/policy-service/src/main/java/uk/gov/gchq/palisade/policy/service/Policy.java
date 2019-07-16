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

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import uk.gov.gchq.palisade.ToStringBuilder;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.rule.PredicateRule;
import uk.gov.gchq.palisade.rule.Rule;
import uk.gov.gchq.palisade.rule.Rules;

import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

/**
 * This class is used to store the information that is required by the policy
 * service but not needed by the rest of the palisade services. That includes
 * separating the rules that need to be applied at the resource level or the record level.
 *
 * @param <RULE_DATA_TYPE> The Java class that the rules expect the records of
 *                         data to be in the format of.
 */
public class Policy<RULE_DATA_TYPE> {
    private Rules<RULE_DATA_TYPE> recordRules;
    private Rules<Resource> resourceRules;
    private User owner;

    // no-args constructor required
    public Policy() {
        recordRules = new Rules<>();
        resourceRules = new Rules<>();
    }

    public Policy<RULE_DATA_TYPE> recordRules(final Rules<RULE_DATA_TYPE> recordRules) {
        requireNonNull(recordRules, "The record level rules cannot be set to null.");
        this.recordRules.rules(recordRules.getRules());
        return this;
    }

    public Policy<RULE_DATA_TYPE> resourceRules(final Rules<Resource> resourceRules) {
        requireNonNull(resourceRules, "The resource level rules cannot be set to null.");
        this.resourceRules.rules(resourceRules.getRules());
        return this;
    }

    @JsonIgnore
    public String getMessage() {
        return "Resource level rules: " + getResourceRules().getMessage() + ", record level rules: " + getRecordRules().getMessage();
    }

    public Rules<RULE_DATA_TYPE> getRecordRules() {
        // will never be null
        return recordRules;
    }

    public void setRecordRules(final Rules<RULE_DATA_TYPE> recordRules) {
        recordRules(recordRules);
    }

    public Rules<Resource> getResourceRules() {
        // will never be null
        return resourceRules;
    }

    public void setResourceRules(final Rules<Resource> resourceRules) {
        resourceRules(resourceRules);
    }

    private static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    private void addMessage(final String newMessage, final Rules rules) {
        requireNonNull(newMessage, "Cannot add a null message.");
        requireNonNull(rules, "Cannot add a message to a null set of rules.");
        String currentMessage = rules.getMessage();
        if (currentMessage.equals(Rules.NO_RULES_SET)) {
            rules.message(newMessage);
        } else {
            rules.message(currentMessage + ", " + newMessage);
        }
    }

    public Policy<RULE_DATA_TYPE> recordLevelRule(final String message, final Rule<RULE_DATA_TYPE> rule) {
        requireNonNull(message, "The message cannot be null and should indicate what the rule is doing.");
        requireNonNull(rule, "Cannot set a null rule.");
        Rules<RULE_DATA_TYPE> recordRules = getRecordRules();
        recordRules.rule(generateUUID(), rule);
        addMessage(message, recordRules);
        return this;
    }

    public Policy<RULE_DATA_TYPE> recordLevelPredicateRule(final String message, final PredicateRule<RULE_DATA_TYPE> rule) {
        requireNonNull(message, "The message cannot be null and should indicate what the rule is doing.");
        requireNonNull(rule, "Cannot set a null rule.");
        Rules<RULE_DATA_TYPE> recordRules = getRecordRules();
        recordRules.rule(generateUUID(), rule);
        addMessage(message, recordRules);
        return this;
    }

    public Policy<RULE_DATA_TYPE> recordLevelSimplePredicateRule(final String message, final Predicate<RULE_DATA_TYPE> rule) {
        requireNonNull(message, "The message cannot be null and should indicate what the rule is doing.");
        requireNonNull(rule, "Cannot set a null rule.");
        Rules<RULE_DATA_TYPE> recordRules = getRecordRules();
        recordRules.simplePredicateRule(generateUUID(), rule);
        addMessage(message, recordRules);
        return this;
    }

    public Policy<RULE_DATA_TYPE> recordLevelSimpleFunctionRule(final String message, final Function<RULE_DATA_TYPE, RULE_DATA_TYPE> rule) {
        requireNonNull(message, "The message cannot be null and should indicate what the rule is doing.");
        requireNonNull(rule, "Cannot set a null rule.");
        Rules<RULE_DATA_TYPE> recordRules = getRecordRules();
        recordRules.simpleFunctionRule(generateUUID(), rule);
        addMessage(message, recordRules);
        return this;
    }

    public Policy<RULE_DATA_TYPE> resourceLevelRule(final String message, final Rule<Resource> rule) {
        requireNonNull(message, "The message cannot be null and should indicate what the rule is doing.");
        requireNonNull(rule, "Cannot set a null rule.");
        Rules<Resource> resourceRules = getResourceRules();
        resourceRules.rule(generateUUID(), rule);
        addMessage(message, resourceRules);
        return this;
    }

    public Policy<RULE_DATA_TYPE> resourceLevelPredicateRule(final String message, final PredicateRule<Resource> rule) {
        requireNonNull(message, "The message cannot be null and should indicate what the rule is doing.");
        requireNonNull(rule, "Cannot set a null rule.");
        Rules<Resource> resourceRules = getResourceRules();
        resourceRules.rule(generateUUID(), rule);
        addMessage(message, resourceRules);
        return this;
    }

    public Policy<RULE_DATA_TYPE> resourceLevelSimplePredicateRule(final String message, final Predicate<Resource> rule) {
        requireNonNull(message, "The message cannot be null and should indicate what the rule is doing.");
        requireNonNull(rule, "Cannot set a null rule.");
        Rules<Resource> resourceRules = getResourceRules();
        resourceRules.simplePredicateRule(generateUUID(), rule);
        addMessage(message, resourceRules);
        return this;
    }

    public Policy<RULE_DATA_TYPE> resourceLevelSimpleFunctionRule(final String message, final Function<Resource, Resource> rule) {
        requireNonNull(message, "The message cannot be null and should indicate what the rule is doing.");
        requireNonNull(rule, "Cannot set a null rule.");
        Rules<Resource> resourceRules = getResourceRules();
        resourceRules.simpleFunctionRule(generateUUID(), rule);
        addMessage(message, resourceRules);
        return this;
    }

    public User getOwner() {
        requireNonNull(owner, "The owner has not been set.");
        return owner;
    }

    public void setOwner(final User owner) {
        owner(owner);
    }

    public Policy<RULE_DATA_TYPE> owner(final User owner) {
        requireNonNull(owner, "The owner cannot be set to null.");
        this.owner = owner;
        return this;
    }

    @JsonGetter("owner")
    User getNullableOwner() {
        return owner;
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
