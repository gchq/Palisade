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

package uk.gov.gchq.palisade.policy.tuple;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import uk.gov.gchq.koryphe.tuple.Tuple;
import uk.gov.gchq.koryphe.tuple.function.TupleAdaptedFunction;
import uk.gov.gchq.koryphe.tuple.predicate.TupleAdaptedPredicate;
import uk.gov.gchq.palisade.Justification;
import uk.gov.gchq.palisade.ToStringBuilder;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.policy.Rule;

import java.util.function.Function;
import java.util.function.Predicate;

import static uk.gov.gchq.palisade.Util.arr;

@JsonPropertyOrder(value = {"class", "selection", "function", "predicate", "projection"}, alphabetic = true)
/**
 * A {@link TupleRule} is helper implementation of {@link Rule} that uses
 * {@link PalisadeTuple} to wrap the record, user and justification into a
 * tuple. This allows users to write rules such as:
 * <pre>
 *     new TupleRule<>(select("Record.visibility", "User.auths"), new IsVisible())
 * </pre>
 * @param <T> The type of the record. In normal cases the raw data will be deserialised
 *            by the record reader before being passed to the {@link TupleRule#apply(Object, User, Justification)}.
 */
public class TupleRule<T> implements Rule<T> {
    private final TupleAdaptedFunction<String, ?, ?> function;
    private final TupleAdaptedPredicate<String, ?> predicate;

    public TupleRule(final String selection,
                     final Function<?, ?> function,
                     final String projection) {
        this(arr(selection), function, arr(projection));
    }

    public TupleRule(final String[] selection,
                     final Function<?, ?> function,
                     final String[] projection) {
        this(selection, function, null, projection);
    }

    public TupleRule(final String selection, final Predicate<?> predicate) {
        this(arr(selection), predicate);
    }

    public TupleRule(final String[] selection, final Predicate<?> predicate) {
        this(selection, null, predicate, null);
    }

    @JsonCreator
    TupleRule(
            @JsonProperty("selection") final String[] selection,
            @JsonProperty("function") final Function<?, ?> function,
            @JsonProperty("predicate") final Predicate<?> predicate,
            @JsonProperty("projection") final String[] projection) {
        if (null == function && null == predicate) {
            throw new IllegalArgumentException("A function or predicate is required");
        }
        if (null != function && null != predicate) {
            throw new IllegalArgumentException("Either provide a function or predicate, not both");
        }

        if (null != function) {
            this.function = new TupleAdaptedFunction<>(function);
            this.function.setSelection(null != selection ? selection : new String[0]);
            this.function.setProjection(null != projection ? projection : new String[0]);
            this.predicate = null;
        } else {
            if (null != projection && projection.length > 0) {
                throw new IllegalArgumentException("Predicates cannot have a projection");
            }
            this.predicate = new TupleAdaptedPredicate<>(predicate, null != selection ? selection : new String[0]);
            this.function = null;
        }
    }

    @Override
    public T apply(final T record, final User user, final Justification justification) {
        final boolean unwrap;
        final PalisadeTuple tuple;
        if (record instanceof Tuple) {
            tuple = new PalisadeTuple(((Tuple<String>) record), user, justification);
            unwrap = false;
        } else {
            tuple = new PalisadeTuple(new ReflectiveTuple(record), user, justification);
            unwrap = true;
        }

        final T rtn;
        if (null != function) {
            final Object updatedTuple = ((PalisadeTuple) function.apply(tuple)).getRecord();
            if (unwrap) {
                rtn = (T) ((ReflectiveTuple) updatedTuple).getRecord();
            } else {
                rtn = (T) updatedTuple;
            }
        } else {
            rtn = predicate.test(tuple) ? record : null;
        }
        return rtn;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = As.PROPERTY, property = "class")
    public Function<?, ?> getFunction() {
        return null != function ? function.getFunction() : null;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = As.PROPERTY, property = "class")
    public Predicate<?> getPredicate() {
        return null != predicate ? predicate.getPredicate() : null;
    }

    public String[] getSelection() {
        return null != function ? function.getSelection() : predicate.getSelection();
    }

    public String[] getProjection() {
        return null != function ? function.getProjection() : null;
    }

    @Override
    public String _getClass() {
        return null;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final TupleRule<?> tupleRule = (TupleRule<?>) o;

        return new EqualsBuilder()
                .append(function, tupleRule.function)
                .append(predicate, tupleRule.predicate)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(function)
                .append(predicate)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("selection", null != function ? function.getSelection() : predicate.getSelection())
                .append("function", function)
                .append("predicate", predicate)
                .append("projection", null != function ? function.getProjection() : null)
                .toString();
    }
}
