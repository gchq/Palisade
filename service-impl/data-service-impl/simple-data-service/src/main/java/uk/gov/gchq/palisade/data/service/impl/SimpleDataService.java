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

import uk.gov.gchq.palisade.audit.service.AuditService;
import uk.gov.gchq.palisade.audit.service.request.ReadRequestExceptionAuditRequest;
import uk.gov.gchq.palisade.audit.service.request.ReadRequestReceivedAuditRequest;
import uk.gov.gchq.palisade.audit.service.request.ReadResponseAuditRequest;
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
    private static final String AUDIT_IMPL_KEY = "sds.svc.audit.svc";

    private PalisadeService palisadeService;
    private DataReader reader;
    private CacheService cache;
    private final Heartbeat heartbeat;
    private AuditService auditService;

    public SimpleDataService() {
        heartbeat = new Heartbeat().serviceClass(SimpleDataService.class);
    }

    public SimpleDataService auditService(final AuditService auditService) {
        requireNonNull(auditService, "The audit service cannot be set to null");
        this.auditService = auditService;
        return this;
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

    private void auditReadRequestReceived(final ReadRequest request) {
        final ReadRequestReceivedAuditRequest requestReceivedAuditRequest = new ReadRequestReceivedAuditRequest();
        requestReceivedAuditRequest
        .requestId(request.getRequestId())
        .resource(request.getResource())
        .id(request.getId())
        .originalRequestId(request.getOriginalRequestId());
        auditService.audit(requestReceivedAuditRequest);
    }

    private void auditRequestReceivedException(final ReadRequest request, final Throwable ex) {
        final ReadRequestExceptionAuditRequest readRequestExceptionAuditRequest = new ReadRequestExceptionAuditRequest();
        readRequestExceptionAuditRequest.exception(ex)
        .resource(request.getResource())
        .requestId(request.getRequestId())
        .id(request.getId())
        .originalRequestId(request.getOriginalRequestId());
        LOGGER.debug("Error handling: " + ex.getMessage());
        auditService.audit(readRequestExceptionAuditRequest);
    }

    private void auditReadResponse(final ReadRequest request) {
        final ReadResponseAuditRequest readResponseAuditRequest = new ReadResponseAuditRequest();
        readResponseAuditRequest
        .resource(request.getResource())
        .requestId(request.getRequestId())
        .id(request.getId())
        .originalRequestId(request.getOriginalRequestId());
        auditService.audit(readResponseAuditRequest);
    }

    @Override
    public CompletableFuture<ReadResponse> read(final ReadRequest request) {
        requireNonNull(request, "The request cannot be null.");
        //check that we have an active heartbeat before serving request

        auditReadRequestReceived(request);
        if (!heartbeat.isBeating()) {
            throw new IllegalStateException("data service is not sending heartbeats! Can't send data. Has the cache service been configured?");
        }
        LOGGER.debug("Creating async read: {}", request);
        return CompletableFuture.supplyAsync(() -> {
            LOGGER.debug("Starting to read: {}", request);
            final GetDataRequestConfig getConfig = new GetDataRequestConfig()
            .requestId(request.getRequestId())
            .resource(request.getResource());
            getConfig.setOriginalRequestId(request.getOriginalRequestId());
            LOGGER.debug("Calling palisade service with: {}", getConfig);
            final DataRequestConfig config = getPalisadeService().getDataRequestConfig(getConfig).join();
            LOGGER.debug("Palisade service returned: {}", config);

            final DataReaderRequest readerRequest = new DataReaderRequest()
            .resource(request.getResource())
            .user(config.getUser())
            .context(config.getContext())
            .rules(config.getRules().get(request.getResource()));
            readerRequest.setOriginalRequestId(request.getOriginalRequestId());

            LOGGER.debug("Calling reader with: {}", readerRequest);
            final DataReaderResponse readerResult = getReader().read(readerRequest);
            LOGGER.debug("Reader returned: {}", readerResult);

            final ReadResponse response = new ReadResponse();
            if (null != readerResult.getData()) {
                response.data(readerResult.getData());
            }
            LOGGER.debug("Returning from read: {}", response);
            auditReadResponse(request);
            return response;
        })
        .exceptionally(ex -> {
            LOGGER.debug("Error handling: " + ex.getMessage());
            auditRequestReceivedException(request, ex);
            throw new RuntimeException(ex); //rethrow the exception
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
    public void applyConfigFrom(final ServiceState config) throws
    NoConfigException {
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
        String serialisedAudit = config.getOrDefault(AUDIT_IMPL_KEY, null);
        if (nonNull(serialisedAudit)) {
            setAuditService(JSONSerialiser.deserialise(serialisedAudit.getBytes(StandardCharsets.UTF_8), AuditService.class));
        } else {
            throw new NoConfigException("no audit specified in configuration");
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
        String serialisedAudit = new String(JSONSerialiser.serialise(auditService), StandardCharsets.UTF_8);
        config.put(AUDIT_IMPL_KEY, serialisedAudit);
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

    public AuditService getAuditService() {
        requireNonNull(auditService, "The audit service has not been set.");
        return auditService;
    }

    public void setAuditService(final AuditService auditService) {
        auditService(auditService);
    }
}
