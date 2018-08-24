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

package uk.gov.gchq.palisade.data.service.reader;

import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.Resource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * The SimpleDataReader is a *very* simple implementation of {@link DataReader}
 * that just maintains a {@link Map} of {@link Resource} to byte[] of
 * raw data, allowing you to add and retrieve resources.
 * </p>
 * <p>
 * It should only be used for examples/demos.
 * </p>
 */
public class SimpleDataReader extends SerialisedDataReader {
    private Map<Resource, byte[]> data = new ConcurrentHashMap<>();

    public SimpleDataReader data(final Map<Resource, byte[]> data) {
        this.data = data;
        return this;
    }

    @Override
    protected InputStream readRaw(final LeafResource resource) {
        final byte[] requestedData = data.get(resource);
        if (null == requestedData) {
            throw new IllegalArgumentException("Invalid resource. The resource does not exist: " + resource);
        }
        return new ByteArrayInputStream(requestedData);
    }
}
