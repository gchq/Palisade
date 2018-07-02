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

package uk.gov.gchq.palisade.example.data;

import uk.gov.gchq.palisade.data.serialise.Serialiser;
import uk.gov.gchq.palisade.data.service.reader.SimpleDataReader;
import uk.gov.gchq.palisade.example.ExampleObj;
import uk.gov.gchq.palisade.example.data.serialiser.ExampleObjSerialiser;
import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.resource.impl.FileResource;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExampleSimpleDataReader extends SimpleDataReader {
    public ExampleSimpleDataReader() {
        super(createExampleData(), createExampleSerialisers());
    }

    private static Map<String, Serialiser<?, ?>> createExampleSerialisers() {
        final Map<String, Serialiser<?, ?>> serialisers = new ConcurrentHashMap<>();
        serialisers.put("exampleObj", new ExampleObjSerialiser());
        return serialisers;
    }

    private static Map<Resource, List<?>> createExampleData() {
        final Map<Resource, List<?>> data = new ConcurrentHashMap<>();
        data.put(new FileResource("file1", "exampleObj"),
                Stream.of(new ExampleObj("item1a", "public", 1),
                        new ExampleObj("item1b", "public", 10),
                        new ExampleObj("item1c", "public", 20),
                        new ExampleObj("item1d", "private", 20),
                        new ExampleObj("item2a", "public", 1),
                        new ExampleObj("item2b", "public", 10),
                        new ExampleObj("item2c", "public", 20),
                        new ExampleObj("item2d", "private", 20))
                        .map(new ExampleObjSerialiser()::serialise)
                        .collect(Collectors.toList())
        );
        return data;
    }
}
