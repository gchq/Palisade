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
package uk.gov.gchq.palisade.example.data.serialiser;

import uk.gov.gchq.palisade.data.serialise.Serialiser;
import uk.gov.gchq.palisade.example.ExampleObj;
import uk.gov.gchq.palisade.io.SuppliedInputStream;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.stream.Stream;

public class ExampleObjSerialiser implements Serialiser<ExampleObj> {
    @Override
    public InputStream serialise(final Stream<ExampleObj> stream) {
        final Iterator<ExampleObj> itr = stream.iterator();
        return new SuppliedInputStream(() -> {
            if (itr.hasNext()) {
                final ExampleObj next = itr.next();
                return serialiseToBytes(next);
            }
            return null;
        });
    }

    @Override
    public Stream<ExampleObj> deserialise(final InputStream stream) {
        return new BufferedReader(new InputStreamReader(stream))
                .lines()
                .map(line -> {
                    final String[] parts = line.split(",");
                    return new ExampleObj(parts[0], parts[1], Long.parseLong(parts[2]));
                });
    }

    public byte[] serialiseToBytes(final ExampleObj obj) {
        return (obj.getProperty() + "," + obj.getVisibility() + "," + obj.getTimestamp() + "\n").getBytes();
    }
}
