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
import org.apache.commons.lang3.builder.HashCodeBuilder;

import uk.gov.gchq.palisade.Justification;
import uk.gov.gchq.palisade.ToStringBuilder;
import uk.gov.gchq.palisade.User;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;


/**
 * A {@link FunctionRule} is helper implementation of {@link Rule}. It is useful
 * when you need to set simple rules that don't require the {@link User} or {@link Justification}.
 *
 * @param <T> The type of the record. In normal cases the raw data will be deserialised
 *            by the record reader before being passed to the {@link Rule#apply(Object, User, Justification)}.
 */
@JsonPropertyOrder(alphabetic = true)
public class FunctionRule<T> implements Rule<T> {
    private final Function<T, T> function;

    /**
     * Constructs a {@link FunctionRule} with a given simple function rule to apply.
     * Note - using this means your rule will not be given the User or Justification.
     *
     * @param function the simple {@link Function} rule to wrap.
     */
    @JsonCreator
    public FunctionRule(@JsonProperty("function") final Function<T, T> function) {
        requireNonNull(function, "WrappedRule was initialised with a null function");
        this.function = function;
    }

    @Override
    public T apply(final T obj, final User user, final Justification justification) {
        return function.apply(obj);
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
    public Function<T, T> getFunction() {
        return function;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final FunctionRule<?> that = (FunctionRule<?>) o;

        return new EqualsBuilder()
                .append(function, that.function)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 37)
                .append(function)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("function", function)
                .toString();
    }
}
