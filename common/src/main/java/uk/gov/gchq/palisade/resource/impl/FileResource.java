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

import uk.gov.gchq.palisade.resource.AbstractLeafResource;
import uk.gov.gchq.palisade.resource.ParentResource;

import java.util.Map;

public class FileResource extends AbstractLeafResource {

    public FileResource() {
    }

    @Override
    public FileResource id(final String id) {
        return (FileResource) super.id(id);
    }

    @Override
    public FileResource type(final String type) {
        return (FileResource) super.type(type);
    }

    @Override
    public FileResource serialisedFormat(final String serialisedFormat) {
        return (FileResource) super.serialisedFormat(serialisedFormat);
    }

    @Override
    public FileResource attributes(final Map<String, Object> attributes) {
        return (FileResource) super.attributes(attributes);
    }

    @Override
    public FileResource attribute(final String attributeKey, final Object attributeValue) {
        return (FileResource) super.attribute(attributeKey, attributeValue);
    }

    @Override
    public FileResource parent(final ParentResource parent) {
        return (FileResource) super.parent(parent);
    }
}
