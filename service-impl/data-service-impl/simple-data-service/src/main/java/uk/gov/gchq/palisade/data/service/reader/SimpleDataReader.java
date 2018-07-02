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

import uk.gov.gchq.palisade.data.serialise.Serialiser;
import uk.gov.gchq.palisade.resource.Resource;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * <p>
 * The SimpleDataReader is a *very* simple implementation of {@link DataReader}
 * that just maintains a {@link Map} of {@link Resource} to {@link List} of
 * raw data, allowing you to add and retrieve resources.
 * </p>
 * <p>
 * It should only be used for examples/demos.
 * </p>
 */
public class SimpleDataReader extends SerialisedDataReader {
    private final Map<Resource, List<?>> data;

    public SimpleDataReader() {
        this(new ConcurrentHashMap<>());
    }


    public SimpleDataReader(final Map<String, Serialiser<?, ?>> serialisers) {
        this(new ConcurrentHashMap<>(), serialisers);
    }

    public SimpleDataReader(final Map<Resource, List<?>> data, final Map<String, Serialiser<?, ?>> serialisers) {
        super(serialisers);
        this.data = data;
    }

    public SimpleDataReader(final String type, final Serialiser<?, ?> serialiser) {
        this(new ConcurrentHashMap<>());
        addSerialiser(type, serialiser);
    }

    @Override
    protected Stream<?> readRaw(final Resource resource) {
        final List<?> requestedData = data.get(resource);
        if (null == requestedData) {
            throw new IllegalArgumentException("Invalid resource. The resource does not exist: " + resource);
        }
        return requestedData.stream();
    }
}
