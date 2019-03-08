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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * A structure to store contextual information from the client at query time that can be used when interacting with resources.
 * Interaction with a resource include both reading and writing and will often require
 * additional information that can be stored and recovered in this structure and passed along with the request/operation.
 * i.e. A users justification for requesting the contents of a file.
 */
@JsonPropertyOrder(value = {"class", "contents"}, alphabetic = true)
public class Context {

    private static final String JUSTIFICATION = "justification";
    public static final String NAMESPACE = "Context";
    private Map<String, Object> contents;

    public Context() {
        this(new HashMap<>());
    }

    @JsonCreator
    public Context(@JsonProperty("contents") final Map<String, Object> contents) {
        requireNonNull(contents, "The contents cannot be set to null.");
        this.contents = contents;
    }

    public Context contents(final Map<String, Object> contents) {
        requireNonNull(contents, "The contents cannot be set to null.");
        this.contents = contents;
        return this;
    }

    public void setContents(final Map<String, Object> contents) {
        contents(contents);
    }

    public Map<String, Object> getContents() {
        // contents will never be null
        return contents;
    }

    @JsonIgnore
    public Map<String, Object> getContentsCopy() {
        return ImmutableMap.copyOf(contents);
    }

    @JsonIgnore
    public Context justification(final String justification) {
        requireNonNull(justification, "The justification cannot be set to null");
        contents.put(JUSTIFICATION, justification);
        return this;
    }

    @JsonIgnore
    public String getJustification() {
        try {
            return (String) contents.get(JUSTIFICATION);
        } catch (final ClassCastException e) {
            throw new RuntimeException("The justification value should be a string");
        }
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
                .append(contents, that.contents)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 23)
                .append(contents)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("contents", contents)
                .toString();
    }

    public Object get(final String key) {
        return contents.get(key);
    }

    public Context put(final String key, final Object value) {
        requireNonNull(key, "The key cannot be null.");
        requireNonNull(value, "The value cannot be null.");
        contents.put(key, value);
        return this;
    }

    public Context putIfAbsent(final String key, final Object value) {
        requireNonNull(key, "The key cannot be null.");
        requireNonNull(value, "The value cannot be null.");
        contents.putIfAbsent(key, value);
        return this;
    }
}
