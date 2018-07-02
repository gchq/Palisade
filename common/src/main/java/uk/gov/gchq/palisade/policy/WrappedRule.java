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
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.commons.lang3.builder.EqualsBuilder;

import uk.gov.gchq.palisade.Justification;
import uk.gov.gchq.palisade.ToStringBuilder;
import uk.gov.gchq.palisade.User;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

@JsonPropertyOrder(value = {"class", "rule", "function", "predicate"}, alphabetic = true)
/**
 * A {@link WrappedRule} is helper implementation of {@link Rule}. It is useful
 * when you need to set simple rules that don't require the {@link User} or {@link Justification}.
 * @param <T> The type of the record. In normal cases the raw data will be deserialised
 *            by the record reader before being passed to the {@link Rule#apply(Object, User, Justification)}.
 */
public class WrappedRule<T> implements Rule<T> {
    public static final String WRAPPED_RULE_WAS_INITIALISED_WITH_NULL = "WrappedRule was initialised with null ";
    public static final String RULE = "rule";
    public static final String FUNCTION = "function";
    public static final String PREDICATE = "predicate";
    private Rule<T> rule;
    private Function<T, T> function;
    private Predicate<T> predicate;

    /**
     * Constructs a {@link WrappedRule} with a null rule.
     */
    public WrappedRule() {
    }

    /**
     * Constructs a {@link WrappedRule} with a given rule to wrap.
     *
     * @param rule the {@link Rule} to wrap.
     */
    public WrappedRule(final Rule<T> rule) {
        requireNonNull(rule, WRAPPED_RULE_WAS_INITIALISED_WITH_NULL + RULE);
        this.rule = rule;
    }

    /**
     * Constructs a {@link WrappedRule} with a given simple function rule to apply.
     * Note - using this means your rule will not be given the User or Justification.
     *
     * @param function the simple {@link Function} rule to wrap.
     */
    public WrappedRule(final Function<T, T> function) {
        requireNonNull(function, WRAPPED_RULE_WAS_INITIALISED_WITH_NULL + FUNCTION);
        this.function = function;
    }

    /**
     * Constructs a {@link WrappedRule} with a given simple predicate rule to apply.
     * Note - using this means your rule will not be given the User or Justification.
     *
     * @param predicate the simple {@link Predicate} rule to wrap.
     */
    public WrappedRule(final Predicate<T> predicate) {
        requireNonNull(predicate, WRAPPED_RULE_WAS_INITIALISED_WITH_NULL + PREDICATE);
        this.predicate = predicate;
    }

    @JsonCreator
    public WrappedRule(@JsonProperty("rule") final Rule<T> rule,
                       @JsonProperty("function") final Function<T, T> function,
                       @JsonProperty("predicate") final Predicate<T> predicate) {
        this.rule = rule;
        this.function = function;
        this.predicate = predicate;

        checkNullCount(rule, function, predicate);
    }

    private void checkNullCount(final Rule<T> rule, final Function<T, T> function, final Predicate<T> predicate) {
        //needs improving with Jackson
        int nullCount = 0;
        if (rule == null) {
            nullCount++;
        }
        if (function == null) {
            nullCount++;
        }
        if (predicate == null) {
            nullCount++;
        }
        if (nullCount < 2) {
            throw new IllegalArgumentException("Only one constructor parameter can be non-null");
        }
    }

    @Override
    public T apply(final T obj, final User user, final Justification justification) {
        final T rtn;
        if (nonNull(rule)) {
            rtn = rule.apply(obj, user, justification);
        } else if (nonNull(function)) {
            rtn = function.apply(obj);
        } else if (nonNull(predicate)) {
            final boolean test = predicate.test(obj);
            rtn = test ? obj : null;
        } else {
            rtn = obj;
        }
        return rtn;
    }


    public Rule<T> getRule() {
        return rule;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
    public Function<T, T> getFunction() {
        return function;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
    public Predicate<T> getPredicate() {
        return predicate;
    }

    @Override
    public boolean equals(final Object o) {
        final boolean rtn;

        if (isNull(o)) {
            rtn = false;
        } else if (this == o) {
            rtn = true;
        } else if (o instanceof WrappedRule) {
            final WrappedRule that = (WrappedRule) o;
            rtn = new EqualsBuilder()
                    .append(this.rule, that.rule)
                    .append(this.function, that.function)
                    .append(this.predicate, that.predicate)
                    .build();
        } else {
            rtn = false;
        }

        return rtn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rule, function, predicate);
    }

    @Override
    public String toString() {
        final ToStringBuilder tsb = new ToStringBuilder(this);
        if (nonNull(rule)) {
            tsb.appendToString(this.rule.toString());
        }
        if (nonNull(function)) {
            tsb.append(FUNCTION, function);
        }
        if (nonNull(predicate)) {
            tsb.append(PREDICATE, predicate);
        }
        return tsb.build();
    }
}
