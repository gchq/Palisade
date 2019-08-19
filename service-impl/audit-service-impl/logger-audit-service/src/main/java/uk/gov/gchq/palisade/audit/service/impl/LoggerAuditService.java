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

package uk.gov.gchq.palisade.audit.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.audit.service.AuditService;
import uk.gov.gchq.palisade.audit.service.request.AuditRequest;
import uk.gov.gchq.palisade.audit.service.request.ReadRequestCompleteAuditRequest;
import uk.gov.gchq.palisade.audit.service.request.ReadRequestExceptionAuditRequest;
import uk.gov.gchq.palisade.audit.service.request.RegisterRequestCompleteAuditRequest;
import uk.gov.gchq.palisade.audit.service.request.RegisterRequestExceptionAuditRequest;
import uk.gov.gchq.palisade.exception.NoConfigException;
import uk.gov.gchq.palisade.service.ServiceState;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * A LoggerAuditService is a simple implementation of an {@link AuditService} that simply constructs a message and logs
 * it using log4j {@link Logger}. <ul> <li>Messages are logged at INFO logging level.</li> <li>Error messages are logged
 * at ERROR logging level.</li> </ul> <p> An example message is: </p>
 * <pre>
 * 'Alice' accessed 'file1' for 'Payroll' and it was processed using 'Age off and visibility filtering'
 * </pre>
 */
public class LoggerAuditService implements AuditService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerAuditService.class);
    private static final Map<Class, Consumer<Object>> DISPATCH = new HashMap<Class, Consumer<Object>>();

    //translate class object to handler
    static {
        //handler for RegisterRequestCompleteAuditRequest
        DISPATCH.put(RegisterRequestCompleteAuditRequest.class, (o) -> {
            requireNonNull(o, "RegisterRequestCompleteAuditRequest cannot be null");
            RegisterRequestCompleteAuditRequest registerRequestCompleteAuditRequest = (RegisterRequestCompleteAuditRequest) o;
            final String msg = "'RegisterRequestCompleteAuditRequest': " + registerRequestCompleteAuditRequest;
            LOGGER.info(msg);
        });
        //handler for RegisterRequestExceptionAuditRequest
        DISPATCH.put(RegisterRequestExceptionAuditRequest.class, (o) -> {
            requireNonNull(o, "RegisterRequestExceptionAuditRequest cannot be null");
            RegisterRequestExceptionAuditRequest registerRequestExceptionAuditRequest = (RegisterRequestExceptionAuditRequest) o;
            final String msg = "'RegisterRequestExceptionAuditRequest': " + registerRequestExceptionAuditRequest;
            LOGGER.error(msg);
        });
        //handler for ReadRequestCompleteAuditRequest
        DISPATCH.put(ReadRequestCompleteAuditRequest.class, (o) -> {
            requireNonNull(o, "ReadRequestCompleteAuditRequest cannot be null");
            ReadRequestCompleteAuditRequest readRequestCompleteAuditRequest = (ReadRequestCompleteAuditRequest) o;
            final String msg = "'readRequestException': " + readRequestCompleteAuditRequest;
            LOGGER.info(msg);
        });
        //handler for ReadRequestExceptionAuditRequest
        DISPATCH.put(ReadRequestExceptionAuditRequest.class, (o) -> {
            requireNonNull(o, "ReadRequestExceptionAuditRequest cannot be null");
            ReadRequestExceptionAuditRequest readRequestExceptionAuditRequest = (ReadRequestExceptionAuditRequest) o;
            final String msg = "'readRequestExceptionAuditRequest': " + readRequestExceptionAuditRequest;
            LOGGER.error(msg);
        });
    }

    @Override
    public CompletableFuture<Boolean> audit(final AuditRequest request) {
        requireNonNull(request, "The audit request can not be null.");
        Consumer<Object> handler = DISPATCH.get((request.getClass()));
        if (handler != null) {
            handler.accept(request);
        } else {
            //received an AuditRequest derived class that is not defined as a Handler above.
            //need to add handler for this class.
            LOGGER.error("handler == null for " + request.getClass().getName());

        }
        return CompletableFuture.completedFuture(Boolean.TRUE);
    }

    @Override
    public void recordCurrentConfigTo(final ServiceState config) {
        requireNonNull(config, "config");
        config.put(AuditService.class.getTypeName(), getClass().getTypeName());
        LOGGER.debug("Wrote configuration data: no-op");
    }

    @Override
    public void applyConfigFrom(final ServiceState config) throws NoConfigException {
        requireNonNull(config, "config");
        LOGGER.debug("Read configuration data: no-op");
    }
}
