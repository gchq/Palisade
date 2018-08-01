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

package uk.gov.gchq.palisade.resource;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import uk.gov.gchq.palisade.ToStringBuilder;

public abstract class AbstractResource implements Resource {
    private String id;
    private String type;
    private String serialisedFormat;

    public AbstractResource() {
    }

    public AbstractResource id(final String id) {
        this.id = id;
        return this;
    }

    public AbstractResource type(final String type) {
        this.type = type;
        return this;
    }

    public AbstractResource serialisedFormat(final String serialisedFormat) {
        this.serialisedFormat = serialisedFormat;
        return this;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getSerialisedFormat() {
        return serialisedFormat;
    }

    @Override
    public void setId(final String id) {
        this.id = id;
    }

    @Override
    public void setType(final String type) {
        this.type = type;
    }

    @Override
    public void setSerialisedFormat(final String serialisedFormat) {
        this.serialisedFormat = serialisedFormat;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final AbstractResource that = (AbstractResource) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .append(type, that.type)
                .append(serialisedFormat, that.serialisedFormat)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(29, 31)
                .append(id)
                .append(type)
                .append(serialisedFormat)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("type", type)
                .append("serialisedFormat", serialisedFormat)
                .toString();
    }
}
