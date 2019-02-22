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

package uk.gov.gchq.palisade.example;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import uk.gov.gchq.palisade.ToStringBuilder;

public class ExampleObj {
    private String property;
    private String visibility;
    private long timestamp;

    public ExampleObj() {
    }

    public ExampleObj(final String property, final String visibility, final long timestamp) {
        this.property = property;
        this.visibility = visibility;
        this.timestamp = timestamp;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(final String property) {
        this.property = property;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(final String visibility) {
        this.visibility = visibility;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ExampleObj that = (ExampleObj) o;

        return new EqualsBuilder()
                .append(timestamp, that.timestamp)
                .append(property, that.property)
                .append(visibility, that.visibility)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(43, 47)
                .append(property)
                .append(visibility)
                .append(timestamp)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("property", property)
                .append("visibility", visibility)
                .append("timestamp", timestamp)
                .toString();
    }
}
