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

@JsonPropertyOrder(value = {"class", "context"}, alphabetic = true)
public class Context {
    private Map<String, Object> context;

    public Context() {
        this(new HashMap<>());
    }

    @JsonCreator
    public Context(@JsonProperty("context") final HashMap<String, Object> context) {
        this.context = context;
    }

    public Context context(final Map<String, Object> context) {
        this.context = context;
        return this;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public Context justification(final String justification) {
        context.put(ContextKeys.JUSTIFICATION, justification);
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
                .append(context, that.context)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 23)
                .append(context)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("justification", context)
                .toString();
    }

}
