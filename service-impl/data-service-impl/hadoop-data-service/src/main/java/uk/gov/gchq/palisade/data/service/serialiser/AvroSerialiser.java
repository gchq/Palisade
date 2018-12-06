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

package uk.gov.gchq.palisade.data.service.serialiser;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileStream;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificData;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.data.serialise.Serialiser;
import uk.gov.gchq.palisade.io.Bytes;
import uk.gov.gchq.palisade.io.BytesOutputStream;
import uk.gov.gchq.palisade.io.BytesSuppliedInputStream;

import java.io.Closeable;
import java.io.InputStream;
import java.util.Iterator;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Objects.requireNonNull;

/**
 * An {@code AvroInputStreamSerialiser} is used to serialise and deserialise Avro files.
 * Converts an avro {@link InputStream} to/from a {@link Stream} of domain objects ({@link O}s).
 *
 * @param <O> the domain object type
 */
public class AvroSerialiser<O> implements Serialiser<O> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AvroSerialiser.class);

    private final Class<O> domainClass;
    private final Schema schema;

    @JsonCreator
    public AvroSerialiser(@JsonProperty("domainClass") final Class<O> domainClass) {
        requireNonNull(domainClass, "domainClass is required");
        this.domainClass = domainClass;
        this.schema = SpecificData.get().getSchema(domainClass);
    }

    @Override
    public InputStream serialise(final Stream<O> stream) {
        return new BytesSuppliedInputStream(new AvroSupplier<>(stream, new SpecificDatumWriter<>(schema), schema));
    }

    @Override
    public Stream<O> deserialise(final InputStream input) {
        DataFileStream<O> in = null;
        try {
            in = new DataFileStream<>(input, new SpecificDatumReader<>(domainClass));
            return StreamSupport.stream(in.spliterator(), false);
        } catch (final Exception e) {
            LOGGER.debug("Closing streams");
            IOUtils.closeQuietly(in);
            throw new RuntimeException("Unable to deserialise object, failed to read input bytes", e);
        }
    }

    public Class<O> getDomainClass() {
        return domainClass;
    }

    private static class AvroSupplier<O> implements Supplier<Bytes> {
        private final Schema schema;
        private final DataFileWriter<O> dataFileWriter;
        private final BytesOutputStream outputStream;
        private final Iterator<O> items;

        private boolean isCreated;

        AvroSupplier(final Stream<O> stream, final DatumWriter<O> datumWriter, final Schema schema) {
            outputStream = new BytesOutputStream();
            dataFileWriter = new DataFileWriter<>(datumWriter);
            items = stream.iterator();
            this.schema = schema;
        }

        @Override
        public Bytes get() {
            outputStream.reset();
            if (!isCreated) {
                LOGGER.debug("Creating data file writer");
                try {
                    dataFileWriter.create(schema, outputStream);
                } catch (final Exception e) {
                    return onError(dataFileWriter, "Unable to create data file writer", e);
                }
                isCreated = true;
            }
            while (items.hasNext() && outputStream.getCount() == 0) {
                final O next = items.next();
                LOGGER.debug("Appending: {}", next);
                try {
                    dataFileWriter.append(next);
                } catch (final Exception e) {
                    return onError(dataFileWriter, "Unable to serialise item", e);
                }
            }
            if (!items.hasNext()) {
                LOGGER.debug("Flushing data file writer");
                try {
                    dataFileWriter.flush();
                } catch (final Exception e) {
                    return onError(dataFileWriter, "Unable to serialise flush after reading input stream", e);
                }
            }
            return outputStream;
        }

        private <T> T onError(final Closeable closeable, final String errorMsg, final Exception e) {
            LOGGER.debug("Closing streams");
            IOUtils.closeQuietly(closeable);
            throw new RuntimeException(errorMsg, e);
        }
    }
}
