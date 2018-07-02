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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.Util;
import uk.gov.gchq.palisade.data.serialise.NullSerialiser;
import uk.gov.gchq.palisade.data.serialise.Serialiser;
import uk.gov.gchq.palisade.data.service.reader.request.DataReaderRequest;
import uk.gov.gchq.palisade.data.service.reader.request.DataReaderResponse;
import uk.gov.gchq.palisade.resource.Resource;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * This class is an abstract implementation of the {@link DataReader} which uses
 * serialisers to serialise the data into the for that the rules need to be able
 * to apply those rules and then de-serialise to how the client is expecting the
 * data to be returned.
 *
 * This class means that the only places where the structure of the data needs
 * to be known is in the serialisers, rules and client code. Therefore you only
 * need to implement a {@link uk.gov.gchq.palisade.data.service.DataService} for
 * each data storage technology and data format combination, rather than also
 * having to add the data structure into the mix.
 */
public abstract class SerialisedDataReader implements DataReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(SerialisedDataReader.class);

    private final Serialiser<?, ?> defaultSerialiser = new NullSerialiser<>();
    private Map<String, Serialiser<?, ?>> serialisers;

    // no-args constructor required
    public SerialisedDataReader() {
        serialisers = new HashMap<>();
    }

    /**
     * Default constructor
     *
     * @param serialisers a mapping of data type to serialiser
     */
    public SerialisedDataReader(final Map<String, Serialiser<?, ?>> serialisers) {
        Objects.requireNonNull(serialisers);
        this.serialisers = serialisers;
    }

    /**
     * This read method uses the serialiser that matches the data type of the
     * resource to serialise the raw data and apply the rules to the data and
     * then deserialise it back to the raw format expected by the client.
     *
     * @param request {@link DataReaderRequest} containing the resource to be
     *                read, rules to be applied, the user requesting the data
     *                and the justification for accessing the data.
     * @param <RAW_DATA_TYPE> The format that the data is expected by the client.
     * @param <RULES_DATA_TYPE> The format that the rules expect the data to be in.
     * @return a {@link DataReaderResponse} containing the stream of data
     * read to be streamed back to the client
     */
    @Override
    public <RAW_DATA_TYPE, RULES_DATA_TYPE> DataReaderResponse<RAW_DATA_TYPE> read(final DataReaderRequest<RULES_DATA_TYPE> request) {
        final Serialiser<RAW_DATA_TYPE, RULES_DATA_TYPE> serialiser = getSerialiser(request.getResource());
        final Stream<RAW_DATA_TYPE> rawStream = (Stream<RAW_DATA_TYPE>) readRaw(request.getResource());

        final Stream<RAW_DATA_TYPE> data;
        if (request.getRules().getRules().isEmpty()) {
            LOGGER.debug("No rules to apply");
            data = rawStream;
        } else {
            LOGGER.debug("Applying rules: {}", request.getRules());
            data = Util.applyRules(
                    rawStream.map(serialiser::deserialise),
                    request.getUser(),
                    request.getJustification(),
                    request.getRules()
            ).map(serialiser::serialise);
        }

        return new DataReaderResponse<>(data);
    }

    /**
     * This is the method that connects to the data and streams the raw data
     * into the {@link SerialisedDataReader}.
     *
     * @param resource the resource to be accessed
     * @return a stream of data in the format that the client expects the data to be in.
     */
    protected abstract Stream<?> readRaw(final Resource resource);

    public <RAW_DATA_TYPE, RULES_DATA_TYPE> Serialiser<RAW_DATA_TYPE, RULES_DATA_TYPE> getSerialiser(final String type) {
        Serialiser<?, ?> serialiser = serialisers.get(type);
        if (null == serialiser) {
            serialiser = defaultSerialiser;
        }
        return (Serialiser<RAW_DATA_TYPE, RULES_DATA_TYPE>) serialiser;
    }

    public <I, O> Serialiser<I, O> getSerialiser(final Resource resource) {
        return getSerialiser(resource.getType());
    }

    public void addSerialiser(final String type, final Serialiser<?, ?> serialiser) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(serialiser);
        serialisers.put(type, serialiser);
    }

    public void setSerialisers(final Map<String, Serialiser<?, ?>> serialisers) {
        Objects.requireNonNull(serialisers);
        this.serialisers = serialisers;
    }
}
