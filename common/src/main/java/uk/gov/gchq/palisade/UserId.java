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

/**
 * A {@link UserId} uniquely identifies a {@link User}. By default the ID will
 * be set to "UNKNOWN".
 */
public class UserId implements Cloneable {
    /**
     * The default user ID - "UNKNOWN".
     */
    public static final String UNKNOWN_USER_ID = "UNKNOWN";
    public static final String NAMESPACE = "UserId";
    public static final String ID = "id";

    private static final Map<String, FieldGetter<UserId>> FIELD_GETTERS = createFieldGetters();
    private static final Map<String, FieldSetter<UserId>> FIELD_SETTERS = createFieldSetters();

    private String id;

    /**
     * Constructs a {@link UserId} with the default user ID of "UNKNOWN".
     */
    public UserId() {
        id(UNKNOWN_USER_ID);
    }

    public UserId id(final String id) {
        if (null == id) {
            this.id = UNKNOWN_USER_ID;
        } else {
            this.id = id;
        }
        return this;
    }

    public String getId() {
        return id;
    }


    public Object getField(final String reference) {
        return Util.getField(this, FIELD_GETTERS, reference);
    }

    public void setField(final String reference, final Object value) {
        Util.setField(this, FIELD_SETTERS, reference, value);
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

    public void setId(final String id) {
        this.id = id;
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
