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
package uk.gov.gchq.palisade.data.service.impl.serialiser;

import com.google.common.collect.Lists;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileStream;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificData;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.junit.Test;

import uk.gov.gchq.palisade.data.serialise.Serialiser;
import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.palisade.util.JsonAssert;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class AvroSerialiserTest {

    public static final int INPUT_SIZE = 100;
    public static final Integer[] INPUT = IntStream.range(0, INPUT_SIZE).mapToObj(Integer::valueOf).toArray((a) -> new Integer[INPUT_SIZE]);

    @Test
    public void shouldConsistentlyPass() throws IOException {
        for (int i = 0; i < 10000; i++) {
            testPrimitiveSerialiseAndDeserialise();
        }
    }

    @Test
    public void testPrimitiveSerialise() throws IOException {
        // Given
        final AvroSerialiser<Integer> serialiser = new AvroSerialiser<>(Integer.class);

        // When
        final InputStream serialised = serialiser.serialise(Stream.of(INPUT));

        // Then
        final DatumReader<Integer> datumReader = new SpecificDatumReader<>(Integer.class);
        final DataFileStream<Integer> in = new DataFileStream<>(serialised, datumReader);
        final List<Integer> deserialised = Lists.newArrayList((Iterator<Integer>) in);
        assertEquals(Arrays.asList(INPUT), deserialised);
    }

    @Test
    public void testPrimitiveDeserialise() throws IOException {
        // Given
        final AvroSerialiser<Integer> serialiser = new AvroSerialiser<>(Integer.class);

        final Schema schema = SpecificData.get().getSchema(Integer.class);
        final DatumWriter<Integer> datumWriter = new SpecificDatumWriter<>(schema);
        final DataFileWriter<Integer> dataFileWriter = new DataFileWriter<>(datumWriter);

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        dataFileWriter.create(schema, outputStream);
        Stream.of(INPUT).forEach(item -> {
            try {
                dataFileWriter.append(item);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        });
        dataFileWriter.flush();
        final InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        // When
        final Stream<Integer> deserialised = serialiser.deserialise(inputStream);

        // Then
        assertEquals(Arrays.asList(INPUT), deserialised.collect(Collectors.toList()));
    }

    @Test
    public void testPrimitiveSerialiseAndDeserialise() throws IOException {
        // Given
        final AvroSerialiser<Integer> serialiser = new AvroSerialiser<>(Integer.class);

        // When
        final InputStream serialised = serialiser.serialise(Stream.of(INPUT));
        final Stream<Integer> deserialised = serialiser.deserialise(serialised);

        // Then
        assertEquals(Arrays.asList(INPUT), deserialised.collect(Collectors.toList()));
    }

    @Test
    public void shouldSerialiseAndDeserialiseWithClass() throws IOException {
        // Given
        final AvroSerialiser<TestObj> serialiser = new AvroSerialiser<>(TestObj.class);

        final List<TestObj> input = Lists.newArrayList(
                new TestObj("str1A", 1, null),
                new TestObj("str1B", 2, "str2B"),
                TestObj.newBuilder()
                        .setFieldStr1("str1C")
                        .setFieldInt(null)
                        .setFieldStr2("str2C")
                        .build()
        );

        // When
        final InputStream serialised = serialiser.serialise(input.stream());
        final Stream<TestObj> deserialised = serialiser.deserialise(serialised);

        // Then
        assertEquals(input, deserialised.collect(Collectors.toList()));
    }

    @Test
    public void shouldJsonSerialiseAndDeserialise() throws IOException {
        // Given
        final AvroSerialiser<Integer> serialiser = new AvroSerialiser<>(Integer.class);

        // When
        final byte[] json = JSONSerialiser.serialise(serialiser, true);
        final Serialiser deserialised = JSONSerialiser.deserialise(json, Serialiser.class);

        // Then
        JsonAssert.assertEquals(String.format("{%n" +
                "  \"domainClass\" : \"java.lang.Integer\",%n" +
                "  \"class\" : \"uk.gov.gchq.palisade.data.service.impl.serialiser.AvroSerialiser\"%n" +
                "}").getBytes(), json);
        assertEquals(AvroSerialiser.class, deserialised.getClass());
        assertEquals(serialiser.getDomainClass(), ((AvroSerialiser) deserialised).getDomainClass());
    }
}
