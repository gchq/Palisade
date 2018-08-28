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

import uk.gov.gchq.koryphe.tuple.ReflectiveTuple;
import uk.gov.gchq.koryphe.tuple.Tuple;
import uk.gov.gchq.koryphe.tuple.function.TupleAdaptedFunction;
import uk.gov.gchq.koryphe.tuple.predicate.TupleAdaptedPredicate;
import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.ToStringBuilder;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.rule.Rule;

import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

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
    private TupleAdaptedFunction<String, ?, ?> function;
    private TupleAdaptedPredicate<String, ?> predicate;

    private boolean isFunction;

    public TupleRule() {
        function = new TupleAdaptedFunction<>();
        function.setSelection(new String[0]);
        function.setProjection(new String[0]);

        predicate = new TupleAdaptedPredicate<>();
        predicate.setSelection(new String[0]);
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

        isFunction = null != function;
        if (isFunction) {
            this.function = new TupleAdaptedFunction<>(function);
            final String[] tmpSelection = null != selection ? selection : new String[0];
            this.function.setSelection(tmpSelection);
            this.function.setProjection(null != projection ? projection : tmpSelection);

            this.predicate = new TupleAdaptedPredicate<>();
            this.predicate.setSelection(new String[0]);
        } else {
            if (null != projection && projection.length > 0) {
                throw new IllegalArgumentException("Predicates cannot have a projection");
            }
            this.predicate = new TupleAdaptedPredicate<>(predicate, null != selection ? selection : new String[0]);

            this.function = new TupleAdaptedFunction<>();
            this.function.setSelection(new String[0]);
            this.function.setProjection(new String[0]);
        }
    }

    public TupleRule<T> selection(final String... selection) {
        this.predicate.setSelection(selection);
        this.function.setSelection(selection);
        if (isNull(this.function.getProjection()) || this.function.getProjection().length == 0) {
            this.function.setProjection(selection);
        }
        return this;
    }

    public TupleRule<T> predicate(final Predicate<?> predicate) {
        this.predicate.setPredicate((Predicate) predicate);
        isFunction = isNull(predicate);
        return this;
    }

    public TupleRule<T> function(final Function<?, ?> function) {
        this.function.setFunction((Function) function);
        isFunction = nonNull(function);
        return this;
    }

    public TupleRule<T> projection(final String... projection) {
        this.function.setProjection(projection);
        return this;
    }

    @Override
    public T apply(final T record, final User user, final Context context) {
        final T rtn;

        final boolean isTupleRecord = record instanceof Tuple;
        final Tuple<String> recordTuple;
        if (isTupleRecord) {
            recordTuple = (Tuple<String>) record;
        } else {
            recordTuple = new ReflectiveTuple(record);
        }

        final PalisadeTuple palisadeTuple = new PalisadeTuple().record(recordTuple).user(user).context(context);
        if (isFunction) {
            final Object updatedTuple = ((PalisadeTuple) function.apply(palisadeTuple)).getRecord();
            if (!isTupleRecord) {
                rtn = (T) ((ReflectiveTuple) updatedTuple).getRecord();
            } else {
                rtn = (T) updatedTuple;
            }
        } else {
            rtn = predicate.test(palisadeTuple) ? record : null;
        }
        return rtn;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = As.PROPERTY, property = "class")
    public Function<?, ?> getFunction() {
        return isFunction ? function.getFunction() : null;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = As.PROPERTY, property = "class")
    public Predicate<?> getPredicate() {
        return !isFunction ? predicate.getPredicate() : null;
    }

    public String[] getSelection() {
        return isFunction ? function.getSelection() : predicate.getSelection();
    }

    public String[] getProjection() {
        return isFunction ? function.getProjection() : null;
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
                .append("selection", isFunction ? function.getSelection() : predicate.getSelection())
                .append("function", function)
                .append("predicate", predicate)
                .append("projection", isFunction ? function.getProjection() : null)
                .toString();
    }
}
