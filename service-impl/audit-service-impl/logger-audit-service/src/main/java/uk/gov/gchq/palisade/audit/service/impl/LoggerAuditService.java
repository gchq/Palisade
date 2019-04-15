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
import uk.gov.gchq.palisade.audit.service.request.AuditRequestWithContext;
import uk.gov.gchq.palisade.audit.service.request.ExceptionAuditRequest;
import uk.gov.gchq.palisade.audit.service.request.ProcessingCompleteAuditRequest;
import uk.gov.gchq.palisade.audit.service.request.ProcessingStartedAuditRequest;
import uk.gov.gchq.palisade.audit.service.request.ReadRequestExceptionAuditRequest;
import uk.gov.gchq.palisade.audit.service.request.ReadRequestReceivedAuditRequest;
import uk.gov.gchq.palisade.audit.service.request.ReadResponseAuditRequest;
import uk.gov.gchq.palisade.audit.service.request.RequestReceivedAuditRequest;
import uk.gov.gchq.palisade.exception.NoConfigException;
import uk.gov.gchq.palisade.service.ServiceState;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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
    private interface Handler {
        void handle(Object o);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerAuditService.class);
    private static final Map<Class, Handler> DISPATCH = new HashMap<Class, Handler>();

    //translate class object to handler
    static {
        //handler for ExceptionAuditRequest
        DISPATCH.put(ExceptionAuditRequest.class, (o) -> {
            requireNonNull(o, "exceptionAuditRequest");
            AuditRequestWithContext auditRequestWithContext = (AuditRequestWithContext) o;
            ExceptionAuditRequest exceptionAuditRequest = (ExceptionAuditRequest) o;
            final String msg = auditLogContext(auditRequestWithContext) + " 'exception'" + exceptionAuditRequest.getException().getMessage();
            LOGGER.error(msg);
        });
        //handler for ProcessingCompleteAuditRequest
        DISPATCH.put(ProcessingCompleteAuditRequest.class, (o) -> {
            requireNonNull(o, "processingCompleteAuditRequest");
            AuditRequestWithContext auditRequestWithContext = (AuditRequestWithContext) o;
            final String msg = auditLogContext(auditRequestWithContext) + " 'processingComplete' ";
            LOGGER.info(msg);
        });
        //handler for ProcessingStartedAuditRequest
        DISPATCH.put(ProcessingStartedAuditRequest.class, (o) -> {
            requireNonNull(o, "processingStartedAuditRequest");
            AuditRequestWithContext auditRequestWithContext = (AuditRequestWithContext) o;
            ProcessingStartedAuditRequest processingStartedAuditRequest = (ProcessingStartedAuditRequest) o;
            final String msg = auditLogContext(auditRequestWithContext) + " 'processingStarted' "
            + processingStartedAuditRequest.getUser().toString()
            + "' accessed '" + processingStartedAuditRequest.getLeafResource().getId()
            + "' and it was processed using '" + processingStartedAuditRequest.getHowItWasProcessed();
            LOGGER.info(msg);
        });
        //handler for RequestReceivedAuditRequest
        DISPATCH.put(RequestReceivedAuditRequest.class, (o) -> {
            requireNonNull(o, "RequestReceivedAuditRequest");
            AuditRequestWithContext auditRequestWithContext = (AuditRequestWithContext) o;
            final String msg = auditLogContext(auditRequestWithContext) + " auditRequest ";
            LOGGER.info(msg);
        });
        //handler for ReadRequestExceptionAuditRequest
        DISPATCH.put(ReadRequestExceptionAuditRequest.class, (o) -> {
            requireNonNull(o, "ReadRequestExceptionAuditRequest");
            ReadRequestExceptionAuditRequest readRequestExceptionAuditRequest = (ReadRequestExceptionAuditRequest) o;
            final String msg = " 'readRequestException' "
            + readRequestExceptionAuditRequest.getId() + " "
            + readRequestExceptionAuditRequest.getRequestId() + " "
            + readRequestExceptionAuditRequest.getOriginalRequestId() + " "
            + readRequestExceptionAuditRequest.getResource().toString() + " "
            + readRequestExceptionAuditRequest.getException().toString();
            LOGGER.error(msg);
        });
        //handler for ReadRequestReceivedAuditRequest
        DISPATCH.put(ReadRequestReceivedAuditRequest.class, (o) -> {
            requireNonNull(o, "ReadRequestReceivedAuditRequest");
            ReadRequestReceivedAuditRequest readRequestReceivedAuditRequest = (ReadRequestReceivedAuditRequest) o;
            final String msg = " 'readRequestReceived' "
            + readRequestReceivedAuditRequest.getId() + " "
            + readRequestReceivedAuditRequest.getRequestId() + " "
            + readRequestReceivedAuditRequest.getOriginalRequestId() + " "
            + readRequestReceivedAuditRequest.getResource().toString() + " ";
            LOGGER.info(msg);
        });
        //handler for ReadResponseAuditRequest
        DISPATCH.put(ReadResponseAuditRequest.class, (o) -> {
            requireNonNull(o, "ReadResponseAuditRequest");
            ReadResponseAuditRequest readResponseAuditRequest = (ReadResponseAuditRequest) o;
            final String msg = " 'readResponseAuditRequest' "
            + readResponseAuditRequest.getId() + " "
            + readResponseAuditRequest.getRequestId() + " "
            + readResponseAuditRequest.getOriginalRequestId() + " "
            + readResponseAuditRequest.getResource().toString() + " ";
            LOGGER.info(msg);
        });

    }

    static String auditLogContext(final AuditRequestWithContext auditRequestWithContext) {

        final String msg = " 'userId' " + auditRequestWithContext.getUserId().getId()
        + " 'purpose' " + auditRequestWithContext.getContext().getPurpose()
        + "' resourceId '" + auditRequestWithContext.getResourceId()
        + "' id '" + auditRequestWithContext.getId()
        + "' originalRequestId '" + auditRequestWithContext.getOriginalRequestId();
        return msg;
    }


    @Override
    public CompletableFuture<Boolean> audit(final AuditRequest request) {
        requireNonNull(request, "The audit request can not be null.");
        Handler handler = DISPATCH.get((request.getClass()));
        if (handler != null) {
            handler.handle(request);
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
