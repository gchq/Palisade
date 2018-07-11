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

package uk.gov.gchq.palisade.policy;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import uk.gov.gchq.palisade.ToStringBuilder;
import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;

import java.util.LinkedHashMap;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * This class is used to encapsulate the list of {@link Rule}s that apply to a
 * resource and is provided with a user friendly message to explain what the set of
 * rules are.
 *
 * @param <T> The type of data records that the rules will be applied to.
 */
@JsonPropertyOrder(value = {"message", "rules"}, alphabetic = true)
public class Rules<T> {
    private String message;
    private LinkedHashMap<String, Rule<T>> rules;

    /**
     * Constructs an instance of {@link Rules} with the message set to and empty string.
     */
    public Rules() {
        this("");
    }

    /**
     * Constructs an instance of {@link Rules} with the provided message.
     *
     * @param message user friendly message to explain what the set of
     *                rules are.
     */
    public Rules(final String message) {
        this(new LinkedHashMap<>(), message);
    }

    /**
     * Constructs an instance of {@link Rules} with the provided message.
     *
     * @param message user friendly message to explain what the set of
     *                rules are.
     * @param rules   the map of id to rule to apply.
     */
    @JsonCreator
    public Rules(@JsonProperty("rules") final LinkedHashMap<String, Rule<T>> rules, @JsonProperty("message") final String message) {
        this.rules = rules;
        this.message = message;
    }

    public LinkedHashMap<String, Rule<T>> getRules() {
        return rules;
    }

    public String getMessage() {
        return message;
    }

    /**
     * Sets a message.
     *
     * @param message user friendly message to explain what the set of
     *                rules are.
     * @return this Rules instance
     */
    public Rules<T> message(final String message) {
        this.message = message;
        return this;
    }

    /**
     * Adds a map of rules
     *
     * @param rules the rules to add
     * @return this Rules instance
     */
    public Rules<T> rules(final LinkedHashMap<String, Rule<T>> rules) {
        this.rules.putAll(rules);
        return this;
    }

    /**
     * Adds a rule.
     *
     * @param id   the unique rule id
     * @param rule the rule
     * @return this Rules instance
     */
    public Rules<T> rule(final String id, final Rule<T> rule) {
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
        rules.put(id, rule);
        return this;
    }

    /**
     * Adds a simple predicate rule that just takes the record and returns true or false.
     * Note - using this means your rule will not be given the User or Justification.
     *
     * @param id   the unique rule id
     * @param rule the simple predicate rule
     * @return this Rules instance
     */
    public Rules<T> simplePredicateRule(final String id, final Predicate<T> rule) {
        rules.put(id, new WrappedRule<>(rule));
        return this;
    }

    /**
     * Adds a simple function rule that just takes the record and returns a modified record or null if the record should be fully redacted.
     * Note - using this means your rule will not be given the User or Justification.
     *
     * @param id   the unique rule id
     * @param rule the simple function rule
     * @return this Rules instance
     */
    public Rules<T> simpleFunctionRule(final String id, final Function<T, T> rule) {
        rules.put(id, new WrappedRule<>(rule));
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        boolean rtn = (this == o);
        if (!rtn) {
            if (o != null && this.getClass() == o.getClass()) {

                final Rules<?> that = (Rules<?>) o;

                final Set<String> thisKeySet = this.rules.keySet();
                final Set<String> thatKeySet = that.getRules().keySet();
                final EqualsBuilder builder = new EqualsBuilder()
                        .append(message, that.message)
                        .append(thisKeySet, thatKeySet);

                if (builder.isEquals()) {
                    final String[] ruleNames = thisKeySet.toArray(new String[thisKeySet.size()]);
                    for (int i = 0; i < this.rules.size() && builder.isEquals(); i++) {
                        final Rule thisRule = this.rules.get(ruleNames[i]);
                        final Rule thatRule = that.getRules().get(ruleNames[i]);
                        builder.append(thisRule.getClass(), thatRule.getClass());
                        if (builder.isEquals()) {
                            // This is expensive - but we don't have any other way of doing it
                            builder.append(JSONSerialiser.serialise(thisRule), JSONSerialiser.serialise(thatRule));
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
