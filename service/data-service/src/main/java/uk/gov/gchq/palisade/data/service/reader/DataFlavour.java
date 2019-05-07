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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.IOException;

import static java.util.Objects.requireNonNull;

/**
 * In Palisade, to process a resource, e.g. to de-serialise it, we must know its data type (what type of object the resource
 * contains, e.g. employee records) and its serialised format (how it is stored, e.g. Avro). A {@code DataFlavour} ius the combination
 * of these two features.
 */
public class DataFlavour {
    /**
     * The delimiter between data type and serialised format.
     */
    public static final String DELIMITER = "##";

    /**
     * The internal store of the flavour. The left entry is the data type and the right entry is the serialised format.
     */
    private final ImmutablePair<String, String> flavour;

    /**
     * Class to ensure {@link DataFlavour}s can be serialised into JSON.
     */
    public final static class FlavourSerializer extends StdSerializer<DataFlavour> {

        public FlavourSerializer() {
            super(DataFlavour.class);
        }

        @Override
        public void serialize(final DataFlavour dataFlavour, final JsonGenerator jsonGenerator, final SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeFieldName(dataFlavour.getDataType() + DELIMITER + dataFlavour.getSerialisedFormat());
        }
    }

    /**
     * Class to ensure {@link DataFlavour}s can be deserialised from JSON.
     */
    public final static class FlavourDeserializer extends KeyDeserializer {

        @Override
        public Object deserializeKey(final String text, final DeserializationContext deserializationContext) throws IOException {
            String[] parts = text.split(DELIMITER);
            if (parts.length != 2) {
                throw new IllegalStateException("error deserialising " + text + " as a DataFlavour, should be in format \"<data_type>" + DELIMITER + "<seralised_format>\"");
            }
            return DataFlavour.of(parts[0], parts[1]);
        }
    }

    /**
     * Create a flavour.
     *
     * @param dataType         the type of record or object being described
     * @param serialisedFormat the encoding format for storing the data
     * @throws IllegalArgumentException if either parameter is empty or blank
     */
    @JsonCreator
    public DataFlavour(@JsonProperty("dataType") final String dataType, @JsonProperty("serialisedFormat") final String serialisedFormat) {
        requireNonNull(dataType, "dataType");
        requireNonNull(serialisedFormat, "serialisedFormat");
        if (dataType.trim().isEmpty()) {
            throw new IllegalArgumentException("dataType cannot be empty");
        }
        if (serialisedFormat.trim().isEmpty()) {
            throw new IllegalArgumentException("serialisedFormat cannot be empty");
        }
        flavour = ImmutablePair.of(dataType, serialisedFormat);
    }

    /**
     * Convenience method to create a {@link DataFlavour}.
     *
     * @param dataType         the data type
     * @param serialisedFormat the serialised format
     * @return a new {@link DataFlavour}
     * @throws IllegalArgumentException if either parameter is empty or blank
     */
    public static DataFlavour of(final String dataType, final String serialisedFormat) {
        return new DataFlavour(dataType, serialisedFormat);
    }

    /**
     * The data type. This is the type of entity that is being described by this flavour, e.g. employee record or bank account record
     *
     * @return data type
     */
    public String getDataType() {
        return flavour.left;
    }

    /**
     * The serialised format. This is the method of storing the data type in a machine processable format, e.g. Avro or JSON.
     *
     * @return serialised format
     */
    public String getSerialisedFormat() {
        return flavour.right;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null) {
            return false;
        }

        DataFlavour other = (DataFlavour) o;

        return flavour.equals(other.flavour);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(flavour.left)
                .append(flavour.right)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("dataType", flavour.left)
                .append("serialisedFormat", flavour.right)
                .toString();
    }
}
