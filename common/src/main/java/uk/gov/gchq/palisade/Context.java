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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.HashMap;
import java.util.Map;

@JsonPropertyOrder(value = {"class", "map"}, alphabetic = true)
public class Context {
    private Map<String, Object> map;

    public Context() {
        this(new HashMap<>());
    }

    @JsonCreator
    public Context(@JsonProperty("map") final HashMap<String, Object> map) {
        this.map = map;
    }

    public Context map(final Map<String, Object> map) {
        this.map = map;
        return this;
    }

    public Map<String, Object> getMap() {
        return map;
    }

    public Context justification(final String justification) {
        map.put(ContextKeys.JUSTIFICATION, justification);
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Context that = (Context) o;

        return new EqualsBuilder()
                .append(map, that.map)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 23)
                .append(map)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("map", map)
                .toString();
    }

    public Object get(final Object key) {
        return map.get(key);
    }
}
