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

import uk.gov.gchq.palisade.io.SuppliedInputStream;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Objects.isNull;

public abstract class LineSerialiser<T> implements Serialiser<T> {
    public static final String LINE_ENDING = String.format("%n");
    public static final Charset CHARSET = Charset.forName("UTF-8");

    public abstract String serialiseLine(final T obj);

    public abstract T deserialiseLine(final String line);

    @Override
    public InputStream serialise(final Stream<T> stream) {
        if (isNull(stream)) {
            return new EmptyInputStream();
        }
        return serialise(stream.iterator());
    }

    public InputStream serialise(final Iterator<T> itr) {
        if (isNull(itr)) {
            return new EmptyInputStream();
        }
        final Supplier<byte[]> supplier = () -> {
            if (itr.hasNext()) {
                final T next = itr.next();
                return (serialiseLine(next) + LINE_ENDING).getBytes(CHARSET);
            }
            return null;
        };
        return new SuppliedInputStream(supplier);
    }

    @Override
    public Stream<T> deserialise(final InputStream stream) {
        if (isNull(stream)) {
            return Stream.empty();
        }
        return new BufferedReader(new InputStreamReader(stream))
                .lines()
                .map(this::deserialiseLine);
    }

    private static class EmptyInputStream extends InputStream {
        public int read() {
            return -1;
        }

        public int available() {
            return 0;
        }
    }
}
