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

public class Justification {
    public static final String NAMESPACE = "Justification";
    public static final String JUSTIFICATION = "justification";

    private static final Map<String, FieldGetter<Justification>> FIELD_GETTERS = createFieldGetters();
    private static final Map<String, FieldSetter<Justification>> FIELD_SETTERS = createFieldSetters();

    private String justification;

    public Justification() {
    }

    public Justification(final String justification) {
        this.justification = justification;
    }

    public Object getField(final String reference) {
        return Util.getField(this, FIELD_GETTERS, reference);
    }

    public void setField(final String reference, final Object value) {
        Util.setField(this, FIELD_SETTERS, reference, value);
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(final String justification) {
        this.justification = justification;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Justification that = (Justification) o;

        return new EqualsBuilder()
                .append(justification, that.justification)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 23)
                .append(justification)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("justification", justification)
                .toString();
    }

    private static Map<String, FieldGetter<Justification>> createFieldGetters() {
        Map<String, FieldGetter<Justification>> map = new HashMap<>();
        map.put(JUSTIFICATION, (justification, subfield) -> justification.getJustification());
        return Collections.unmodifiableMap(map);
    }

    private static Map<String, FieldSetter<Justification>> createFieldSetters() {
        Map<String, FieldSetter<Justification>> map = new HashMap<>();
        map.put(JUSTIFICATION, ((justification, subfield, value) -> justification.setJustification(((String) value))));
        return Collections.unmodifiableMap(map);
    }
}
