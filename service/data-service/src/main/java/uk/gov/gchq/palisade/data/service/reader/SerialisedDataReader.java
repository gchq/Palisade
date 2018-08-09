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
import uk.gov.gchq.palisade.rule.Rules;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * This class is an abstract implementation of the {@link DataReader} which uses
 * serialisers to serialise the data into the for that the rules need to be able
 * to apply those rules and then de-serialise to how the client is expecting the
 * data to be returned.
 * This class means that the only places where the structure of the data needs
 * to be known is in the serialisers, rules and client code. Therefore you only
 * need to implement a {@link uk.gov.gchq.palisade.data.service.DataService} for
 * each data storage technology and data format combination, rather than also
 * having to add the data structure into the mix.
 */
public abstract class SerialisedDataReader implements DataReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(SerialisedDataReader.class);

    private Serialiser<?> defaultSerialiser = new NullSerialiser<>();
    private Map<String, Serialiser<?>> serialisers = new ConcurrentHashMap<>();

    /**
     * @param serialisers a mapping of data type to serialiser
     * @return the {@link SerialisedDataReader}
     */
    public SerialisedDataReader serialisers(final Map<String, Serialiser<?>> serialisers) {
        requireNonNull(serialisers);
        this.serialisers = serialisers;
        return this;
    }

    /**
     * This read method uses the serialiser that matches the data type of the
     * resource to serialise the raw data and apply the rules to the data and
     * then deserialise it back to the raw format expected by the client.
     *
     * @param request {@link DataReaderRequest} containing the resource to be
     *                read, rules to be applied, the user requesting the data
     *                and the justification for accessing the data.
     * @return a {@link DataReaderResponse} containing the stream of data
     * read to be streamed back to the client
     */
    @Override
    public DataReaderResponse read(final DataReaderRequest request) {
        requireNonNull(request, "Request is required");
        requireNonNull(request.getResource(), "Request resource is required");

        final Serialiser<Object> serialiser = getSerialiser(request.getResource());
        final InputStream rawStream = readRaw(request.getResource());

        final InputStream data;
        final Rules<Object> rules = request.getRules();
        if (rules.getRules().isEmpty()) {
            LOGGER.debug("No rules to apply");
            data = rawStream;
        } else {
            LOGGER.debug("Applying rules: {}", rules);
            final Stream<Object> deserialisedData = Util.applyRulesToStream(
                    serialiser.deserialise(rawStream),
                    request.getUser(),
                    request.getJustification(),
                    rules
            );
            data = serialiser.serialise(deserialisedData);
        }

        return new DataReaderResponse().data(data);
    }

    /**
     * This is the method that connects to the data and streams the raw data
     * into the {@link SerialisedDataReader}.
     *
     * @param resource the resource to be accessed
     * @return a stream of data in the format that the client expects the data to be in.
     */
    protected abstract InputStream readRaw(final Resource resource);

    public <RULES_DATA_TYPE> Serialiser<RULES_DATA_TYPE> getSerialiser(final String type) {
        Serialiser<?> serialiser = serialisers.get(type);
        if (null == serialiser) {
            serialiser = defaultSerialiser;
        }
        return (Serialiser<RULES_DATA_TYPE>) serialiser;
    }

    public <I> Serialiser<I> getSerialiser(final Resource resource) {
        return getSerialiser(resource.getType());
    }

    public void addSerialiser(final String type, final Serialiser<?> serialiser) {
        requireNonNull(type);
        requireNonNull(serialiser);
        serialisers.put(type, serialiser);
    }

    public void setSerialisers(final Map<String, Serialiser<?>> serialisers) {
        requireNonNull(serialisers);
        this.serialisers = serialisers;
    }

    public void setDefaultSerialiser(final Serialiser<?> defaultSerialiser) {
        this.defaultSerialiser = defaultSerialiser;
    }
}
