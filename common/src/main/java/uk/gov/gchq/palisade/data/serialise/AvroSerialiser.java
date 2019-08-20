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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileStream;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.reflect.ReflectData;
import org.apache.avro.reflect.ReflectDatumReader;
import org.apache.avro.reflect.ReflectDatumWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

/**
 * An {@code AvroInputStreamSerialiser} is used to serialise and deserialise Avro files.
 * Converts an avro {@link InputStream} to/from a {@link Stream} of domain objects ({@link O}s).
 *
 * @param <O> the domain object type
 */
public class AvroSerialiser<O> implements Serialiser<O> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AvroSerialiser.class);
    private final ReflectDatumWriter<O> datumWriter;

    private final Class<O> domainClass;
    private final Schema schema;

    @JsonCreator
    public AvroSerialiser(@JsonProperty("domainClass") final Class<O> domainClass) {
        requireNonNull(domainClass, "domainClass is required");
        this.domainClass = domainClass;
        this.schema = ReflectData.AllowNull.get().getSchema(domainClass);
        this.datumWriter = new ReflectDatumWriter<>(schema);
    }

    @Override
    public Stream<O> deserialise(final InputStream input) throws IOException {
        DataFileStream<O> in;
        in = new DataFileStream<>(input, new ReflectDatumReader<>(schema));
        //Don't use try-with-resources here! This input stream needs to stay open until it is closed manually by the
        //stream it is feeding below
        return StreamSupport.stream(in.spliterator(), false);
    }

    @Override
    public void serialise(final Stream<O> objects, final OutputStream output) throws IOException {
        requireNonNull(output, "output");
        if (nonNull(objects)) {
            //create a data file writer around the output stream
            //since we didn't create the output stream, we shouldn't close it either, someone else might want it afterwards!
            final DataFileWriter<O> dataFileWriter = new DataFileWriter<>(datumWriter);
            LOGGER.debug("Creating data file writer");
            try {
                dataFileWriter.create(schema, output);
                //iterate and append items -- we can't use forEach on the stream as the lambda can't throw an IOException
                Iterator<O> objectIt = objects.iterator();

                while (objectIt.hasNext()) {
                    O next = objectIt.next();
                    dataFileWriter.append(next);
                }

            } finally {
                try {
                    dataFileWriter.flush();
                } catch (IOException e) {
                    LOGGER.warn("Unable to flush Avro DataFileWriter", e);
                }
            }
        }
    }

    public Class<O> getDomainClass() {
        return domainClass;
    }
}
