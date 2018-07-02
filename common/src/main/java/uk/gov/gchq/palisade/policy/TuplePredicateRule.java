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
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.commons.lang3.builder.ToStringBuilder;

import uk.gov.gchq.koryphe.predicate.KoryphePredicate;
import uk.gov.gchq.koryphe.tuple.Tuple;
import uk.gov.gchq.koryphe.tuple.predicate.TupleAdaptedPredicate;
import uk.gov.gchq.palisade.Justification;
import uk.gov.gchq.palisade.User;

import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

/**
 * This rule filters input objects based on {@link KoryphePredicate} applied against field selections.
 * It treats the input as a {@link Tuple} by wrappings the object within a {@link ReflectiveTuple}, unless obj is already a Tuple.
 *
 * @param <T> The type the {@link KoryphePredicate} is applied to.
 */
@JsonPropertyOrder(value = {"class", "predicate", "selection"}, alphabetic = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "selection")
public class TuplePredicateRule<T> implements PredicateRule<T> {

    public static final String TUPLE_RULE_INITIALISED_WITH_NULL = "TupleRule initialised with null ";
    public static final String PREDICATE_STRING = "predicate";
    private final TupleAdaptedPredicate<String, ?> predicate;

    public TuplePredicateRule(final TupleAdaptedPredicate<String, ?> predicate) {
        requireNonNull(predicate, TUPLE_RULE_INITIALISED_WITH_NULL + PREDICATE_STRING);
        this.predicate = predicate;
    }

    @JsonCreator
    public TuplePredicateRule(@JsonProperty("predicate") final KoryphePredicate<?> predicate,
                              @JsonProperty("selection") final String[] selection) {
        this(new TupleAdaptedPredicate<>(predicate, selection));
    }

    /**
     * Tests input objects against the {@link KoryphePredicate} and selection.
     *
     * @param obj           object to be tested
     * @param user          the user
     * @param justification the justification
     * @return the result of the PredicateFilter against the input object.
     * @throws RuntimeException thrown when input obj can't be tested.
     */
    @Override
    public boolean test(final T obj, final User user, final Justification justification) {
        boolean result;
        try {
            result = predicate.test(getTupleObj(obj));
        } catch (final ClassCastException e) {
            result = predicate.test(new ReflectiveTuple(obj));
        }
        return result;
    }

    /**
     * @param obj object to return as {@link Tuple}
     * @return tuple could throw ClassCastException, if object was a tuple not of type {@code Tuple<String>}.
     */
    private Tuple<String> getTupleObj(final T obj) {
        Tuple<String> tuple;
        if (obj instanceof Tuple) {
            //noinspection unchecked
            tuple = (Tuple<String>) obj;
        } else {
            tuple = new ReflectiveTuple(obj);
        }
        return tuple;
    }

    @Override
    @JsonGetter("class")
    public String _getClass() {
        return null;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append(PREDICATE_STRING, predicate)
                .build();
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class", defaultImpl = TupleAdaptedPredicate.class)
    public Predicate getPredicate() {
        return predicate.getPredicate();
    }

    public String[] getSelection() {
        return predicate.getSelection();
    }
}
