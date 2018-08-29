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

import java.util.Objects;

public abstract class AbstractLeafResource extends AbstractResource implements LeafResource {

    private String type;
    private String serialisedFormat;
    private ParentResource parent;

    public AbstractLeafResource() {
    }

    public AbstractLeafResource type(final String type) {
        Objects.requireNonNull(type, "The type of a resource cannot be set to null.");
        this.type = type;
        return this;
    }

    public AbstractLeafResource serialisedFormat(final String serialisedFormat) {
        Objects.requireNonNull(serialisedFormat, "The serialised format of a resource cannot be set to null.");
        this.serialisedFormat = serialisedFormat;
        return this;
    }

    @Override
    public String getType() {
        Objects.requireNonNull(type, "The type has not been set for this resource.");
        return type;
    }

    @Override
    public String getSerialisedFormat() {
        Objects.requireNonNull(serialisedFormat, "The serialised format has not been set for this resource.");
        return serialisedFormat;
    }

    @Override
    public void setType(final String type) {
        type(type);
    }

    @Override
    public void setSerialisedFormat(final String serialisedFormat) {
        serialisedFormat(serialisedFormat);
    }

    public AbstractLeafResource parent(final ParentResource parent) {
        Objects.requireNonNull(parent, "The parent cannot be set to null.");
        this.parent = parent;
        return this;
    }

    @Override
    public ParentResource getParent() {
        Objects.requireNonNull(parent, "The parent has not been set for this resource.");
        return parent;
    }

    @Override
    public void setParent(final ParentResource parent) {
        parent(parent);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final AbstractLeafResource that = (AbstractLeafResource) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(type, that.type)
                .append(serialisedFormat, that.serialisedFormat)
                .append(parent, that.parent)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(29, 31)
                .appendSuper(super.hashCode())
                .append(type)
                .append(serialisedFormat)
                .append(parent)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("type", type)
                .append("serialisedFormat", serialisedFormat)
                .append("parent", parent)
                .toString();
    }
}
