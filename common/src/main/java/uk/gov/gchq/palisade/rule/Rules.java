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

package uk.gov.gchq.palisade.rule;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import uk.gov.gchq.palisade.ToStringBuilder;
import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;

import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * This class is used to encapsulate the list of {@link Rule}s that apply to a resource and is provided with a user
 * friendly message to explain what the set of rules are.
 *
 * @param <T> The type of data records that the rules will be applied to.
 */
@JsonPropertyOrder(value = {"message", "rules"}, alphabetic = true)
public class Rules<T> {
    private static final String ID_CANNOT_BE_NULL = "The id field can not be null.";
    private static final String RULE_CANNOT_BE_NULL = "The rule can not be null.";
    public static final String NO_RULES_SET = "no rules set";

    private String message;
    private LinkedHashMap<String, Rule<T>> rules;

    /**
     * Constructs an empty instance of {@link Rules}.
     */
    public Rules() {
        rules = new LinkedHashMap<>();
        message = NO_RULES_SET;
    }

    /**
     * Overrides the rules with these new rules
     *
     * @param rules the rules to set
     * @return this Rules instance
     */
    public Rules<T> rules(final LinkedHashMap<String, Rule<T>> rules) {
        Objects.requireNonNull(rules, "Rules can not be set to null.");
        this.rules = rules;
        return this;
    }

    public void setRules(final LinkedHashMap<String, Rule<T>> rules) {
        rules(rules);
    }

    public LinkedHashMap<String, Rule<T>> getRules() {
        // no need for a null check as it can not be null
        return rules;
    }

    public Rules<T> addRules(final LinkedHashMap<String, Rule<T>> rules) {
        Objects.requireNonNull(rules, "Cannot add null to the existing rules.");
        this.rules.putAll(rules);
        return this;
    }

    /**
     * Sets a message.
     *
     * @param message user friendly message to explain what the set of rules are.
     * @return this Rules instance
     */
    public Rules<T> message(final String message) {
        Objects.requireNonNull(message, "The message can not be set to null.");
        this.message = message;
        return this;
    }

    public void setMessage(final String message) {
        message(message);
    }

    public String getMessage() {
        // The message will never be null
        return message;
    }

    /**
     * Adds a rule.
     *
     * @param id   the unique rule id
     * @param rule the rule
     * @return this Rules instance
     */
    public Rules<T> rule(final String id, final Rule<T> rule) {
        Objects.requireNonNull(id, ID_CANNOT_BE_NULL);
        Objects.requireNonNull(rule, RULE_CANNOT_BE_NULL);
        rules.put(id, rule);
        return this;
    }

    /**
     * Adds a predicate rule.
     *
     * @param id   the unique rule id
     * @param rule the predicate rule
     * @return this Rules instance
     */
    public Rules<T> predicateRule(final String id, final PredicateRule<T> rule) {
        Objects.requireNonNull(id, ID_CANNOT_BE_NULL);
        Objects.requireNonNull(rule, RULE_CANNOT_BE_NULL);
        rules.put(id, rule);
        return this;
    }

    /**
     * Adds a simple predicate rule that just takes the record and returns true or false. Note - using this means your
     * rule will not be given the User or Context.
     *
     * @param id   the unique rule id
     * @param rule the simple predicate rule
     * @return this Rules instance
     */
    public Rules<T> simplePredicateRule(final String id, final Predicate<T> rule) {
        Objects.requireNonNull(id, ID_CANNOT_BE_NULL);
        Objects.requireNonNull(rule, RULE_CANNOT_BE_NULL);
        rules.put(id, new WrappedRule<>(rule));
        return this;
    }

    /**
     * Adds a simple function rule that just takes the record and returns a modified record or null if the record should
     * be fully redacted. Note - using this means your rule will not be given the User or Context.
     *
     * @param id   the unique rule id
     * @param rule the simple function rule
     * @return this Rules instance
     */
    public Rules<T> simpleFunctionRule(final String id, final Function<T, T> rule) {
        Objects.requireNonNull(id, ID_CANNOT_BE_NULL);
        Objects.requireNonNull(rule, RULE_CANNOT_BE_NULL);
        rules.put(id, new WrappedRule<>(rule));
        return this;
    }

    /**
     * Tests if this rule set if empty.
     *
     * @return {@code true} if this rule set contains at least one rule
     */
    public boolean containsRules() {
        return !rules.isEmpty();
    }

    @Override
    public boolean equals(final Object o) {
        boolean rtn = (this == o);
        if (!rtn) {
            if (o != null && this.getClass() == o.getClass()) {

                final Rules<?> that = (Rules<?>) o;

                final EqualsBuilder builder = new EqualsBuilder()
                        .append(message, that.message)
                        .append(this.rules.keySet(), that.getRules().keySet());

                if (builder.isEquals()) {
                    for (final Entry<String, Rule<T>> entry : this.rules.entrySet()) {
                        final String ruleName = entry.getKey();
                        final Rule thisRule = entry.getValue();
                        final Rule thatRule = that.getRules().get(ruleName);

                        builder.append(thisRule.getClass(), thatRule.getClass());
                        if (builder.isEquals()) {
                            // This is expensive - but we don't have any other way of doing it
                            builder.append(JSONSerialiser.serialise(thisRule), JSONSerialiser.serialise(thatRule));
                        }

                        if (!builder.isEquals()) {
                            break;
                        }
                    }
                }
                rtn = builder.isEquals();
            }
        }

        return rtn;
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder builder = new HashCodeBuilder(17, 37)
                .append(message);
        rules.forEach((s, tRule) -> builder.append(s).append(JSONSerialiser.serialise(tRule)));
        return builder.toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("message", message)
                .append("rules", rules)
                .build();
    }
}
