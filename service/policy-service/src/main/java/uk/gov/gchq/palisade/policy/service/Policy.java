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
import uk.gov.gchq.palisade.policy.PredicateRule;
import uk.gov.gchq.palisade.policy.Rule;
import uk.gov.gchq.palisade.policy.Rules;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * This class is used to encapsulate the {@link Rules} that apply to a
 * resource and is provided with a user friendly message to explain what the
 * policy is for/doing.
 *
 * @param <RULE_DATA_TYPE> The Java class that the rules expect the records of
 *                        data to be in the format of.
 */
public class Policy<RULE_DATA_TYPE> {
    private String message;
    private Rules<RULE_DATA_TYPE> rules;
    // TODO policies may need owners to control who can modify them.

    // no-args constructor required
    public Policy() {
        this("");
    }

    /**
     * @param message a user friendly message stating what the policy does.
     */
    public Policy(final String message) {
        this(new Rules<>(), message);
    }

    /**
     * @param rules the set of rules that need to be applied to the resource.
     */
    public Policy(final Rules<RULE_DATA_TYPE> rules) {
        this(rules, "");
    }

    /**
     * Default constructor
     *
     * @param rules the set of rules that need to be applied to the resource.
     * @param message a user friendly message stating what the policy does.
     */
    public Policy(final Rules<RULE_DATA_TYPE> rules, final String message) {
        Objects.requireNonNull(rules);
        Objects.requireNonNull(message);
        this.rules = rules;
        this.message = message;
    }

    public Rules getRules() {
        return rules;
    }

    public String getMessage() {
        return message;
    }

    public Policy<RULE_DATA_TYPE> message(final String message) {
        this.message = message;
        return this;
    }

    public Policy<RULE_DATA_TYPE> rule(final String ruleId, final Rule<RULE_DATA_TYPE> rule) {
        rules.rule(ruleId, rule);
        return this;
    }

    public Policy<RULE_DATA_TYPE> predicateRule(final String ruleId, final PredicateRule<RULE_DATA_TYPE> rule) {
        rules.predicateRule(ruleId, rule);
        return this;
    }

    public Policy<RULE_DATA_TYPE> simplePredicateRule(final String ruleId, final Predicate<RULE_DATA_TYPE> rule) {
        rules.simplePredicateRule(ruleId, rule);
        return this;
    }

    public Policy<RULE_DATA_TYPE> simpleFunctionRule(final String ruleId, final Function<RULE_DATA_TYPE, RULE_DATA_TYPE> rule) {
        rules.simpleFunctionRule(ruleId, rule);
        return this;
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
                .append(message, policy.message)
                .append(rules, policy.rules)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(message)
                .append(rules)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("message", message)
                .append("rules", rules)
                .toString();
    }
}
