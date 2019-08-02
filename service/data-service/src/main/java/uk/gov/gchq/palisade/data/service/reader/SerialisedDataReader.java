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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.Util;
import uk.gov.gchq.palisade.audit.service.AuditService;
import uk.gov.gchq.palisade.audit.service.request.ReadRequestCompleteAuditRequest;
import uk.gov.gchq.palisade.data.serialise.Serialiser;
import uk.gov.gchq.palisade.data.serialise.SimpleStringSerialiser;
import uk.gov.gchq.palisade.data.service.reader.request.DataReaderRequest;
import uk.gov.gchq.palisade.data.service.reader.request.DataReaderResponse;
import uk.gov.gchq.palisade.io.CloseActionInputStream;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.rule.Rules;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
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
 * <p>
 * A serialiser is chosen based on a {@link DataFlavour} which is a combination of
 * data type and serialised format.
 */
public abstract class SerialisedDataReader implements DataReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(SerialisedDataReader.class);
    protected long numberOfRecordsProcessed = 0;
    protected long numberOfRecordsReturned = 0;

    @JsonProperty("default")
    private Serialiser<?> defaultSerialiser = new SimpleStringSerialiser();

    /**
     * Map of the types and formats to the serialising object. The first element of the key is the data type
     * and the second element is the serialised format.
     */
    @JsonProperty("serialisers")
    @JsonSerialize(keyUsing = DataFlavour.FlavourSerializer.class)
    @JsonDeserialize(keyUsing = DataFlavour.FlavourDeserializer.class)
    private Map<DataFlavour, Serialiser<?>> serialisers = new ConcurrentHashMap<>();

    private AuditService auditService;

    /**
     * @param serialisers a mapping of data type to serialiser
     * @return the {@link SerialisedDataReader}
     */
    public SerialisedDataReader serialisers(final Map<DataFlavour, Serialiser<?>> serialisers) {
        requireNonNull(serialisers, "The serialisers cannot be set to null.");
        this.serialisers = serialisers;
        return this;
    }

    public SerialisedDataReader defaultSerialiser(final Serialiser<?> serialiser) {
        requireNonNull(serialiser, "The default serialiser cannot be set to null.");
        this.defaultSerialiser = serialiser;
        return this;
    }

    public SerialisedDataReader auditService(final AuditService auditService) {
        requireNonNull(auditService, "The audit service cannot be set to null");
        this.auditService = auditService;
        return this;
    }

    /**
     * This read method uses the serialiser that matches the data type of the
     * resource to serialise the raw data and apply the rules to the data and
     * then deserialise it back to the raw format expected by the client.
     *
     * @param request {@link DataReaderRequest} containing the resource to be
     *                read, rules to be applied, the user requesting the data
     *                and the purpose for accessing the data.
     * @return a {@link DataReaderResponse} containing the stream of data
     * read to be streamed back to the client
     */
    @Override
    public DataReaderResponse read(final DataReaderRequest request) {
        requireNonNull(request, "The request cannot be null.");

        final Serialiser<Object> serialiser = getSerialiser(request.getResource());
        final InputStream rawStream = readRaw(request.getResource());

        final InputStream data;
        final Rules<Object> rules = request.getRules();
        if (isNull(rules) || isNull(rules.getRules()) || rules.getRules().isEmpty()) {
            LOGGER.debug("No rules to apply");
            data = rawStream;
        } else {
            LOGGER.debug("Applying rules: {}", rules);
            // TODO identify way to count the number of records processed and returned
            final Stream<Object> deserialisedData = Util.applyRulesToStream(
                    serialiser.deserialise(rawStream),
                    request.getUser(),
                    request.getContext(),
                    rules
            ).onClose(() -> {
                // Audit log the number of results returned
                ReadRequestCompleteAuditRequest auditRequest = new ReadRequestCompleteAuditRequest()
                        .resource(request.getResource())
                        .numberOfRecordsProcessed(numberOfRecordsProcessed)
                        .numberOfRecordsReturned(numberOfRecordsReturned);
                auditRequest.originalRequestId(request.getOriginalRequestId());
                getAuditService().audit(auditRequest);
                //ensure the original stream is closed as well
                try {
                    rawStream.close();
                } catch (IOException ignored) {
                }
            });
            //make sure we close the streams
            data = new CloseActionInputStream(serialiser.serialise(deserialisedData), deserialisedData::close);
        }

        return new DataReaderResponse().data(data);
    }

    public static class ClosingInputStream extends FilterInputStream {
        private final Runnable closeAction;

        public ClosingInputStream(final InputStream underlyingStream, final Runnable closeAction) {
            super(underlyingStream);
            requireNonNull(closeAction, "closeAction");
            this.closeAction = closeAction;
        }

        @Override
        public void close() throws IOException {
            try {
                super.close();
            } finally {
                closeAction.run();
            }
        }
    }

    /**
     * This is the method that connects to the data and streams the raw data
     * into the {@link SerialisedDataReader}.
     *
     * @param resource the resource to be accessed
     * @return a stream of data in the format that the client expects the data to be in.
     */
    protected abstract InputStream readRaw(final LeafResource resource);

    public <RULES_DATA_TYPE> Serialiser<RULES_DATA_TYPE> getSerialiser(final DataFlavour flavour) {
        requireNonNull(flavour, "The flavour cannot be null.");
        Serialiser<?> serialiser = serialisers.get(flavour);

        if (null == serialiser) {
            serialiser = defaultSerialiser;
        }
        return (Serialiser<RULES_DATA_TYPE>) serialiser;
    }

    public <I> Serialiser<I> getSerialiser(final LeafResource resource) {
        requireNonNull(resource, "The resource cannot be null.");
        return getSerialiser(DataFlavour.of(resource.getType(), resource.getSerialisedFormat()));
    }

    public void addSerialiser(final DataFlavour flavour, final Serialiser<?> serialiser) {
        requireNonNull(flavour, "The flavour cannot be null.");
        requireNonNull(serialiser, "The serialiser cannot be null.");
        serialisers.put(flavour, serialiser);
    }

    /**
     * Adds all the serialiser mappings to the current map of serialisers.Any existing mappings for a given {@link DataFlavour}
     * are replaced.
     *
     * @param mergingSerialisers the new serialisers to merge
     */
    public void addAllSerialisers(final Map<DataFlavour, Serialiser<?>> mergingSerialisers) {
        requireNonNull(mergingSerialisers, "mergingSerialisers");
        serialisers.putAll(mergingSerialisers);
    }

    public void setSerialisers(final Map<DataFlavour, Serialiser<?>> serialisers) {
        serialisers(serialisers);
    }

    public void setDefaultSerialiser(final Serialiser<?> defaultSerialiser) {
        defaultSerialiser(defaultSerialiser);
    }

    public AuditService getAuditService() {
        requireNonNull(auditService, "The audit service has not been set.");
        return auditService;
    }

    public void setAuditService(final AuditService auditService) {
        auditService(auditService);
    }
}
