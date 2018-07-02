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

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;

import uk.gov.gchq.palisade.Justification;
import uk.gov.gchq.palisade.User;

/**
 * <p>
 * A {@code Rule} is the fundamental interface for applying policy criteria.
 * It allows a record to redacted or modified based on a {@link User} and their
 * query {@link Justification}.
 * </p>
 * <p>
 * Please note, the justification is optional and may be null.
 * </p>
 * <p>
 * To work over the REST API implementations should be JSON serialisable.
 * The easiest way to achieve this is to have a default constructor and getters/setters for all fields.
 * </p>
 *
 * @param <T> The type of the record. In normal cases the raw data will be deserialised
 *            by the record reader before being passed to the {@link Rule#apply(Object, User, Justification)}.
 */
@FunctionalInterface
@JsonPropertyOrder(value = {"class"}, alphabetic = true)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = As.EXISTING_PROPERTY,
        property = "class"
)
public interface Rule<T> {
    /**
     * Applies the rule logic to redact or modify the record based on the user and justification.
     *
     * @param record        the record to be checked.
     * @param user          the user
     * @param justification the query justificaiton
     * @return the modified record or null if the record should be fully redacted.
     */
    T apply(final T record, final User user, final Justification justification);

    @JsonGetter("class")
    default String _getClass() {
        return getClass().getName();
    }

    @JsonSetter("class")
    default void _setClass(final String className) {
        // do nothing.
    }
}
