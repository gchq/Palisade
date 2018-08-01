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

import uk.gov.gchq.palisade.resource.AbstractResource;
import uk.gov.gchq.palisade.resource.ChildResource;
import uk.gov.gchq.palisade.resource.ParentResource;

public class FileResource extends AbstractResource implements ChildResource {

    private ParentResource parent;

    public FileResource() {
    }

    public FileResource id(final String id) {
        super.id(id);
        return this;
    }

    public FileResource type(final String type) {
        super.type(type);
        return this;
    }

    public FileResource serialisedFormat(final String serialisedFormat) {
        super.serialisedFormat(serialisedFormat);
        return this;
    }

    @Override
    public ParentResource getParent() {
        return parent;
    }

    @Override
    public void setParent(final ParentResource parentId) {
        this.parent = parentId;
    }

    @Override
    public String _getClass() {
        return null;
    }
}
