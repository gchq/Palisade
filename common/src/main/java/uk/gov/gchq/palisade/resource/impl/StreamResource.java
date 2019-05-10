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

public class StreamResource extends AbstractLeafResource {
    long start;
    long end;

    public StreamResource() {
    }

    @Override
    public StreamResource id(final String id) {
        return (StreamResource) super.id(id);
    }

    @Override
    public StreamResource type(final String type) {
        return (StreamResource) super.type(type);
    }

    @Override
    public StreamResource serialisedFormat(final String serialisedFormat) {
        return (StreamResource) super.serialisedFormat(serialisedFormat);
    }

    @Override
    public StreamResource attributes(Map<String, Object> attributes) {
        return (StreamResource) super.attributes(attributes);
    }

    @Override
    public StreamResource attribute(String attributeKey, Object attributeValue) {
        return (StreamResource) super.attribute(attributeKey, attributeValue);
    }

    @Override
    public StreamResource parent(final ParentResource parent) {
        return (StreamResource) super.parent(parent);
    }
}
