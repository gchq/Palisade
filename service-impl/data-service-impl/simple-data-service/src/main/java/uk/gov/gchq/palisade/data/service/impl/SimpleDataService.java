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

package uk.gov.gchq.palisade.data.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.data.service.DataService;
import uk.gov.gchq.palisade.data.service.reader.DataReader;
import uk.gov.gchq.palisade.data.service.reader.NullDataReader;
import uk.gov.gchq.palisade.data.service.reader.request.DataReaderRequest;
import uk.gov.gchq.palisade.data.service.reader.request.DataReaderResponse;
import uk.gov.gchq.palisade.data.service.request.ReadRequest;
import uk.gov.gchq.palisade.data.service.request.ReadResponse;
import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.service.NullPalisadeService;
import uk.gov.gchq.palisade.service.PalisadeService;
import uk.gov.gchq.palisade.service.request.ConnectionDetail;
import uk.gov.gchq.palisade.service.request.DataRequestConfig;
import uk.gov.gchq.palisade.service.request.DataRequestResponse;

import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * <p>
 * A SimpleDataService is a simple implementation of {@link DataService} that
 * applies the required policy rules before returning data.
 * </p>
 * <p>
 * It should only be used for examples/demos.
 * </p>
 * <p>
 * It does not currently apply any validation of the {@link ReadRequest}, so users are able to
 * request any data they want, which could be different to the original data they requested from Palisade.
 * </p>
 */
public class SimpleDataService implements DataService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleDataService.class);

    private PalisadeService palisadeService;
    private DataReader reader;

    public SimpleDataService() {
        this(new NullPalisadeService(), new NullDataReader());
    }

    public SimpleDataService(final PalisadeService palisadeService, final DataReader reader) {
        Objects.requireNonNull(palisadeService);
        Objects.requireNonNull(reader);
        this.palisadeService = palisadeService;
        this.reader = reader;
    }

    @Override
    public <T> CompletableFuture<ReadResponse<T>> read(final ReadRequest request) {
        Objects.requireNonNull(request);
        Objects.requireNonNull(request.getDataRequestResponse());
        Objects.requireNonNull(request.getDataRequestResponse().getRequestId());
        LOGGER.debug("Creating async read: {}", request);
        return CompletableFuture.supplyAsync(() -> {
            LOGGER.debug("Starting to read: {}", request);
            final DataRequestResponse dataRequestResponse = request.getDataRequestResponse();
            LOGGER.debug("Calling palisade service with: {}", dataRequestResponse);
            final DataRequestConfig config = palisadeService.getDataRequestConfig(dataRequestResponse).join();
            LOGGER.debug("Palisade service returned: {}", config);
            Stream<T> allData = Stream.empty();

            for (final Entry<Resource, ConnectionDetail> entry : dataRequestResponse.getResources().entrySet()) {
                validate(entry.getValue());

                final DataReaderRequest<T> readerRequest = new DataReaderRequest<>(
                        entry.getKey(),
                        config.getUser(),
                        config.getJustification(),
                        config.getResourceRules(entry.getKey())
                );
                LOGGER.debug("Calling reader with: {}", readerRequest);
                final DataReaderResponse<T> readerResult = reader.read(readerRequest);
                LOGGER.debug("Reader returned: {}", readerResult);
                if (null != readerResult.getData()) {
                    allData = Stream.concat(
                            allData,
                            readerResult.getData()
                    );
                }
            }
            final ReadResponse<T> response = new ReadResponse<>(allData);
            LOGGER.debug("Returning from read: {}", response);
            return response;
        });
    }

    private void validate(final ConnectionDetail connectionDetail) {
        // no validation required for this simple implementation.
    }

    public PalisadeService getPalisadeService() {
        return palisadeService;
    }

    public void setPalisadeService(final PalisadeService palisadeService) {
        this.palisadeService = palisadeService;
    }

    public DataReader getReader() {
        return reader;
    }

    public void setReader(final DataReader reader) {
        this.reader = reader;
    }
}
