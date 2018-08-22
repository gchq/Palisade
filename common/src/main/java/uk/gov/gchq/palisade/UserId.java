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

package uk.gov.gchq.palisade;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import uk.gov.gchq.palisade.util.FieldGetter;
import uk.gov.gchq.palisade.util.FieldSetter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A {@link UserId} uniquely identifies a {@link User}.
 */
public class UserId implements Cloneable {

    public static final String NAMESPACE = "UserId";
    public static final String ID = "id";

    private static final Map<String, FieldGetter<UserId>> FIELD_GETTERS = createFieldGetters();
    private static final Map<String, FieldSetter<UserId>> FIELD_SETTERS = createFieldSetters();

    private String id;

    /**
     * Constructs an empty {@link UserId}.
     */
    public UserId() {
    }

    /**
     * Updates the id of the UserID
     *
     * @param id a non null String representing the id of the user
     * @return the UserId object
     */
    public UserId id(final String id) {
        Objects.requireNonNull(id, "The UserId id field can not be set null.");
        this.id = id;
        return this;
    }

    public void setId(final String id) {
        id(id);
    }

    public String getId() {
        Objects.requireNonNull(id, "The UserId id field has not been initialised.");
        return id;
    }

    /**
     * Adds a key value pair to a map which can then be used within the policy rules to make policy decisions.
     *
     * @param reference the key that references the value in the map
     * @param value the object that you want to store in the map
     * @return the UserId object
     */
    public UserId field(final String reference, final Object value) {
        Objects.requireNonNull(reference, "The reference parameter cannot be null.");
        Objects.requireNonNull(value, "The value parameter cannot be null.");
        Util.setField(this, FIELD_SETTERS, reference, value);
        return this;
    }

    public void setField(final String reference, final Object value) {
        field(reference, value);
    }

    public Object getField(final String reference) {
        Object field = Util.getField(this, FIELD_GETTERS, reference);
        Objects.requireNonNull(field, "The field for " + reference + " is not set.");
        return field;
    }

    public UserId clone() {
        UserId clone;
        try {
            clone = (UserId) super.clone();
        } catch (final CloneNotSupportedException e) {
            clone = new UserId();
        }
        clone.id = id;
        return clone;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final UserId userId = (UserId) o;

        return new EqualsBuilder()
                .append(id, userId.id)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 37)
                .append(id)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .toString();
    }

    private static Map<String, FieldGetter<UserId>> createFieldGetters() {
        Map<String, FieldGetter<UserId>> map = new HashMap<>();
        map.put(ID, (userId, subfield) -> userId.getId());
        return Collections.unmodifiableMap(map);
    }

    private static Map<String, FieldSetter<UserId>> createFieldSetters() {
        Map<String, FieldSetter<UserId>> map = new HashMap<>();
        map.put(ID, (userId, subfield, value) -> userId.setId(((String) value)));
        return Collections.unmodifiableMap(map);
    }
}
