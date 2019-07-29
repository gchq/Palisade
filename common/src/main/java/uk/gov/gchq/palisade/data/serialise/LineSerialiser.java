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
package uk.gov.gchq.palisade.data.serialise;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

public abstract class LineSerialiser<T> implements Serialiser<T> {
    public static final Charset CHARSET = StandardCharsets.UTF_8;

    public abstract String serialiseLine(final T obj);

    public abstract T deserialiseLine(final String line);

    @Override
    public Serialiser<T> serialise(final Stream<T> objects, final OutputStream output) {
        return serialise(objects.iterator(), output);
    }

    public Serialiser<T> serialise(final Iterator<T> itr, final OutputStream output) {
        requireNonNull(output, "output");
        if (nonNull(itr)) {
            PrintWriter printOut = new PrintWriter(new OutputStreamWriter(output, CHARSET));
            try {
                itr.forEachRemaining(item -> {
                    printOut.println(serialiseLine(item));
                });
            } finally {
                printOut.flush();
            }
        }
        return this;
    }

    @Override
    public Stream<T> deserialise(final InputStream stream) {
        if (isNull(stream)) {
            return Stream.empty();
        }
        return new BufferedReader(new InputStreamReader(stream, CHARSET))
                .lines()
                .map(this::deserialiseLine);
    }
}
