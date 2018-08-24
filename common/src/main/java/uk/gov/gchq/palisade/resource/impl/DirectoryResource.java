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

package uk.gov.gchq.palisade.resource.impl;

import uk.gov.gchq.palisade.ToStringBuilder;
import uk.gov.gchq.palisade.resource.AbstractResource;
import uk.gov.gchq.palisade.resource.ChildResource;
import uk.gov.gchq.palisade.resource.ParentResource;

import java.util.Objects;

public class DirectoryResource extends AbstractResource implements ChildResource, ParentResource {

    private ParentResource parent;

    public DirectoryResource() {
    }

    @Override
    public DirectoryResource id(final String id) {
        return (DirectoryResource) super.id(id);
    }

    public DirectoryResource parent(final ParentResource parent) {
        Objects.requireNonNull(parent, "The parent cannot be set to null");
        this.parent = parent;
        return this;
    }

    @Override
    public ParentResource getParent() {
        Objects.requireNonNull(parent, "The parent resource has not been set for this resource.");
        return parent;
    }

    @Override
    public void setParent(final ParentResource parent) {
        parent(parent);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("parent", parent)
                .toString();
    }
}
