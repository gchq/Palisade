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
package uk.gov.gchq.palisade.example.rule.function;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import uk.gov.gchq.koryphe.Since;
import uk.gov.gchq.koryphe.Summary;
import uk.gov.gchq.koryphe.function.KorypheFunction;
import uk.gov.gchq.koryphe.tuple.function.TupleAdaptedFunction;
import uk.gov.gchq.koryphe.tuple.predicate.IntegerTupleAdaptedPredicate;
import uk.gov.gchq.koryphe.tuple.predicate.TupleAdaptedPredicate;

import java.util.function.Function;
import java.util.function.Predicate;

import static uk.gov.gchq.palisade.Util.arr;

/**
 * An {@code If} is a {@link Function} that conditionally applies one of two functions to a provided input.
 * <p> Note that the <code>If</code> has both a number of constructors as well as a <code>SelectedBuilder</code>.
 * The use case for constructors would generally be for testing a single input. </p>
 * <p> The use case for the Builder allows greater flexibility,
 * mainly for allowing multiple inputs such as an Array of objects,
 * and control over which of these objects is tested by each function. </p>
 * <p> For example,
 * Given an input array of 3 objects, one may wish to test the first object in the array against the initial predicate,
 * then pass both the second and third objects to the resulting function, based on the outcome of the initial test.
 * This would require use of the <code>Builder</code>, passing a selection of 0 along with the first predicate,
 * and a selection of 1, 2 with the function. </p>
 * This would look something like:
 * <pre>
 *     final If this = new If()
 *          .predicate(0, firstPredicate)
 *          .then(1, thenFunction, 2)
 *          .otherwise(1,2 otherwiseFunction, 2)
 *          .build();
 * </pre>
 *
 * @param <I> the type of input to be validated
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
@Since("0.0.1")
@Summary("Conditionally applies a function")
public class If<I, O> extends KorypheFunction<I, O> {

    private Boolean condition;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class", defaultImpl = TupleAdaptedPredicate.class)
    private Predicate<? super I> predicate;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class", defaultImpl = TupleAdaptedFunction.class)
    private Function<? super I, O> then;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class", defaultImpl = TupleAdaptedPredicate.class)
    private Function<? super I, O> otherwise;

    /**
     * If the condition is not being used or has not been set,
     * then the provided predicate will test the input (assuming it is also not null).
     * If this resolves to true, the <code>then</code> function will be applied the input,
     * else the <code>otherwise</code> function will be applied.
     * The result of either of these being applied to the input is finally returned.
     *
     * @param input the input to be tested
     * @return the result from the conditional functions
     */
    @Override
    public O apply(final I input) {
        boolean conditionTmp;
        if (null == condition) {
            conditionTmp = null != predicate && predicate.test(input);
        } else {
            conditionTmp = condition;
        }

        if (conditionTmp) {
            if (null != then) {
                return then.apply(input);
            }
            return (O) input;
        }

        if (null != otherwise) {
            return otherwise.apply(input);
        }
        return (O) input;
    }

    public Boolean getCondition() {
        return condition;
    }

    public void setCondition(final boolean condition) {
        this.condition = condition;
    }

    public If<I, O> condition(final boolean condition) {
        this.condition = condition;
        return this;
    }

    public Predicate<? super I> getPredicate() {
        return predicate;
    }

    public void setPredicate(final Predicate<? super I> predicate) {
        this.predicate = predicate;
    }

    public If<I, O> predicate(final Predicate<? super I> predicate) {
        setPredicate(predicate);
        return this;
    }

    public If<I, O> predicate(final Integer selection, final Predicate<?> predicate) {
        return predicate(arr(selection), predicate);
    }

    public If<I, O> predicate(final Integer[] selection, final Predicate<?> predicate) {
        final IntegerTupleAdaptedPredicate current = new IntegerTupleAdaptedPredicate(predicate, selection);
        return predicate((Predicate) current);
    }

    public Function<? super I, O> getThen() {
        return then;
    }

    public void setThen(final Function<? super I, O> then) {
        this.then = then;
    }

    public If<I, O> then(final Function<? super I, O> then) {
        setThen(then);
        return this;
    }

    public If<I, O> then(final Integer selectionProjection, final Function<?, ?> then) {
        return then(selectionProjection, then, selectionProjection);
    }

    public If<I, O> then(final Integer selection, final Function<?, ?> then, final Integer projection) {
        return then(arr(selection), then, arr(projection));
    }

    public If<I, O> then(final Integer[] selectionProjection, final Function<?, ?> then) {
        return then(selectionProjection, then, selectionProjection);
    }

    public If<I, O> then(final Integer[] selection, final Function<?, ?> then, final Integer[] projection) {
        final TupleAdaptedFunction current = new TupleAdaptedFunction<>();
        current.setSelection(selection);
        current.setFunction(then);
        current.setProjection(projection);
        return then(current);
    }

    public Function<? super I, O> getOtherwise() {
        return otherwise;
    }

    public void setOtherwise(final Function<? super I, O> otherwise) {
        this.otherwise = otherwise;
    }

    public If<I, O> otherwise(final Function<? super I, O> otherwise) {
        setOtherwise(otherwise);
        return this;
    }

    public If<I, O> otherwise(final Integer selectionProjection, final Function<?, ?> then) {
        return otherwise(selectionProjection, then);
    }

    public If<I, O> otherwise(final Integer selection, final Function<?, ?> then, final Integer projection) {
        return otherwise(arr(selection), then, arr(projection));
    }

    public If<I, O> otherwise(final Integer[] selectionProjection, final Function<?, ?> then) {
        return otherwise(selectionProjection, then, selectionProjection);
    }

    public If<I, O> otherwise(final Integer[] selection, final Function<?, ?> then, final Integer[] projection) {
        final TupleAdaptedFunction current = new TupleAdaptedFunction<>();
        current.setSelection(selection);
        current.setFunction(then);
        current.setProjection(projection);
        setOtherwise(current);
        return this;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (null == obj || getClass() != obj.getClass()) {
            return false;
        }

        final If ifPredicate = (If) obj;

        return new EqualsBuilder()
                .append(condition, ifPredicate.condition)
                .append(predicate, ifPredicate.predicate)
                .append(then, ifPredicate.then)
                .append(otherwise, ifPredicate.otherwise)
                .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(11, 73)
                .append(condition)
                .append(predicate)
                .append(then)
                .append(otherwise)
                .build();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("condition", condition)
                .append("predicate", predicate)
                .append("then", then)
                .append("otherwise", otherwise)
                .toString();
    }
}
