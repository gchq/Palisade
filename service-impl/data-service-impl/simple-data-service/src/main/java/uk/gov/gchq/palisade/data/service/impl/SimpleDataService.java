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

import uk.gov.gchq.palisade.cache.service.CacheService;
import uk.gov.gchq.palisade.cache.service.heart.Heartbeat;
import uk.gov.gchq.palisade.data.service.DataService;
import uk.gov.gchq.palisade.data.service.reader.DataReader;
import uk.gov.gchq.palisade.data.service.reader.request.DataReaderRequest;
import uk.gov.gchq.palisade.data.service.reader.request.DataReaderResponse;
import uk.gov.gchq.palisade.data.service.request.ReadRequest;
import uk.gov.gchq.palisade.data.service.request.ReadResponse;
import uk.gov.gchq.palisade.exception.NoConfigException;
import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.palisade.service.PalisadeService;
import uk.gov.gchq.palisade.service.ServiceState;
import uk.gov.gchq.palisade.service.request.DataRequestConfig;
import uk.gov.gchq.palisade.service.request.GetDataRequestConfig;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

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

    private static final String PALISADE_IMPL_KEY = "sds.svc.palisade.svc";
    private static final String READER_IMPL_KEY = "sds.svc.reader.svc";
    private static final String CACHE_IMPL_KEY = "sds.svc.cache.svc";

    private PalisadeService palisadeService;
    private DataReader reader;
    private CacheService cache;
    private final Heartbeat heartbeat;

    public SimpleDataService() {
        heartbeat = new Heartbeat().serviceClass(SimpleDataService.class);
    }

    public SimpleDataService palisadeService(final PalisadeService palisadeService) {
        requireNonNull(palisadeService, "The palisade service cannot be set to null.");
        this.palisadeService = palisadeService;
        return this;
    }

    public SimpleDataService reader(final DataReader reader) {
        requireNonNull(reader, "The data reader cannot be set to null.");
        this.reader = reader;
        return this;
    }

    public SimpleDataService cacheService(final CacheService cacheService) {
        requireNonNull(cacheService, "The cache service cannot be set to null.");
        this.cache = cacheService;
        //changing cache service...
        heartbeat.stop();
        heartbeat.cacheService(this.cache);
        heartbeat.start();
        return this;
    }

    @Override
    public CompletableFuture<ReadResponse> read(final ReadRequest request) {
        requireNonNull(request, "The request cannot be null.");
        LOGGER.info("EMR debug: SimpleDataService - at start of read - ready to check heartbeat ");
        //check that we have an active heartbeat before serving request
        if (!heartbeat.isBeating()) {
            throw new IllegalStateException("data service is not sending heartbeats! Can't send data. Has the cache service been configured?");
        }
        LOGGER.info("EMR debug: SimpleDataService - after check heartbeat ");
        LOGGER.debug("Creating async read: {}", request);
        LOGGER.info("EMR debug: SimpleDataService - Creating async read: {}", request);
        return CompletableFuture.supplyAsync(() -> {
            LOGGER.debug("Starting to read: {}", request);
            LOGGER.info("EMR debug: SimpleDataService - Starting to read: {}", request);
            final GetDataRequestConfig getConfig = new GetDataRequestConfig()
                    .requestId(request.getRequestId())
                    .resource(request.getResource());
            LOGGER.debug("Calling palisade service with: {}", getConfig);
            LOGGER.info("EMR debug: Calling palisade service with: {}", getConfig);
            final DataRequestConfig config = getPalisadeService().getDataRequestConfig(getConfig).join();
            LOGGER.debug("Palisade service returned: {}", config);
            LOGGER.info("EMR debug: Palisade service returned: {}", config);

            final DataReaderRequest readerRequest = new DataReaderRequest()
                    .resource(request.getResource())
                    .user(config.getUser())
                    .context(config.getContext())
                    .rules(config.getRules().get(request.getResource()));

            LOGGER.debug("Calling reader with: {}", readerRequest);
            final DataReaderResponse readerResult = getReader().read(readerRequest);
            LOGGER.debug("Reader returned: {}", readerResult);

            final ReadResponse response = new ReadResponse();
            if (null != readerResult.getData()) {
                response.data(readerResult.getData());
            }
            LOGGER.debug("Returning from read: {}", response);
            return response;
        });
    }

    public PalisadeService getPalisadeService() {
        requireNonNull(palisadeService, "The palisade service has not been set.");
        return palisadeService;
    }

    public void setPalisadeService(final PalisadeService palisadeService) {
        palisadeService(palisadeService);
    }

    @Override
    public void applyConfigFrom(final ServiceState config) throws NoConfigException {
        requireNonNull(config, "config");
        String serialisedPalisade = config.getOrDefault(PALISADE_IMPL_KEY, null);
        if (nonNull(serialisedPalisade)) {
            setPalisadeService(JSONSerialiser.deserialise(serialisedPalisade.getBytes(StandardCharsets.UTF_8), PalisadeService.class));
        } else {
            throw new NoConfigException("no service specified in configuration");
        }
        String serialisedReader = config.getOrDefault(READER_IMPL_KEY, null);
        if (nonNull(serialisedReader)) {
            setReader(JSONSerialiser.deserialise(serialisedReader.getBytes(StandardCharsets.UTF_8), DataReader.class));
        } else {
            throw new NoConfigException("no reader specified in configuration");
        }
        String serialisedCache = config.getOrDefault(CACHE_IMPL_KEY, null);
        if (nonNull(serialisedCache)) {
            setCacheService(JSONSerialiser.deserialise(serialisedCache.getBytes(StandardCharsets.UTF_8), CacheService.class));
        } else {
            throw new NoConfigException("no cache specified in configuration");
        }
    }

    @Override
    public void recordCurrentConfigTo(final ServiceState config) {
        requireNonNull(config, "config");
        config.put(DataService.class.getTypeName(), getClass().getTypeName());
        String serialised = new String(JSONSerialiser.serialise(getPalisadeService()), StandardCharsets.UTF_8);
        config.put(PALISADE_IMPL_KEY, serialised);
        String serialisedReader = new String(JSONSerialiser.serialise(reader), StandardCharsets.UTF_8);
        config.put(READER_IMPL_KEY, serialisedReader);
        String serialisedCache = new String(JSONSerialiser.serialise(cache), StandardCharsets.UTF_8);
        config.put(CACHE_IMPL_KEY, serialisedCache);
    }

    public DataReader getReader() {
        requireNonNull(reader, "The data reader has not been set.");
        return reader;
    }

    public void setReader(final DataReader reader) {
        reader(reader);
    }

    public CacheService getCacheService() {
        requireNonNull(cache, "The cache service has not been set.");
        return cache;
    }

    public void setCacheService(final CacheService cacheService) {
        cacheService(cacheService);
    }
}
