/*
 * Copyright 2019 Crown Copyright
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

import event.logging.AuthenticateAction;
import event.logging.Device;
import event.logging.Event;
import event.logging.Event.EventDetail.Process;
import event.logging.EventLoggingService;
import event.logging.ProcessAction;
import event.logging.System;
import event.logging.User;
import event.logging.impl.DefaultEventLoggingService;
import event.logging.util.DeviceUtil;
import event.logging.util.EventLoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.UserId;
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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * A StroomAuditService is a simple implementation of an {@link AuditService} that simply constructs a message and logs
 * it using the Stroom EventLoggingService.
 */
public class StroomSimpleAuditService implements AuditService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StroomSimpleAuditService.class);
    private static final Map<Class, Consumer<Object>> DISPATCH = new HashMap<Class, Consumer<Object>>();
    //Create the logging service
    private static final EventLoggingService EVENT_LOGGING_SERVICE = new DefaultEventLoggingService();
    private static final System EVENT_LOGGING_SYSTEM = new System();


    public static void logToStroom(final Class<? extends AuditRequest> cls, final String msg, final RequestId requestId, final UserId userId) {
//Create the user involved in the authenticate action (possibly different from
//the eventSource user)
        String userMsg = "";
        if (userId != null) {
            userMsg += userId.toString() + " ";
        }
        userMsg += requestId.toString();

        final User user = EventLoggingUtil.createUser(userMsg);
        String hostName;
        String hostAddress;

        //Describe the device the event occurred on
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            hostName = inetAddress.getHostName();
            hostAddress = inetAddress.getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        final Device device = DeviceUtil.createDevice(hostName, hostAddress);


//Provide details of where the event came from
        final Event.EventSource eventSource = new Event.EventSource();
        eventSource.setSystem(EVENT_LOGGING_SYSTEM);
        eventSource.setGenerator("testCode");
        eventSource.setDevice(device);
        eventSource.setUser(user);

//Create the authenticate object to describe the authentication specific details
        final Event.EventDetail.Process process = new Process();
        process.setAction(ProcessAction.EXECUTE);
        //mmm not sure what action is the most appropriate here !!!

        final Event.EventDetail.Authenticate authenticate = new Event.EventDetail.Authenticate();
        authenticate.setAction(AuthenticateAction.LOGON);
        authenticate.setUser(user);

//Create the detail of what happened
//TypeId is typically a EVENT_LOGGING_SYSTEM specific code that is unique to a use case in that EVENT_LOGGING_SYSTEM
        final Event.EventDetail eventDetail = new Event.EventDetail();
        eventDetail.setTypeId(cls.getSimpleName());
        eventDetail.setDescription(msg);
        eventDetail.setAuthenticate(authenticate);

//Define the time the event happened
        final Event.EventTime eventTime = EventLoggingUtil.createEventTime(new Date());

//Combine the sub-objects together
        final Event event = EVENT_LOGGING_SERVICE.createEvent();
        event.setEventTime(eventTime);
        event.setEventSource(eventSource);
        event.setEventDetail(eventDetail);

//Send the event
        EVENT_LOGGING_SERVICE.log(event);

    }


    //translate class object to handler
    static {
        //handler for ExceptionAuditRequest
        DISPATCH.put(ExceptionAuditRequest.class, (o) -> {
            requireNonNull(o, "exceptionAuditRequest");
            ExceptionAuditRequest exceptionAuditRequest = (ExceptionAuditRequest) o;
            final String msg = " 'exception' " + auditLogContext(exceptionAuditRequest) + exceptionAuditRequest.getException().getMessage();
            logToStroom(ExceptionAuditRequest.class, msg, exceptionAuditRequest.getOriginalRequestId(), exceptionAuditRequest.getUserId());
        });
        //handler for ProcessingCompleteAuditRequest
        DISPATCH.put(ProcessingCompleteAuditRequest.class, (o) -> {
            requireNonNull(o, "processingCompleteAuditRequest");
            AuditRequestWithContext auditRequestWithContext = (AuditRequestWithContext) o;
            final String msg = " 'processingComplete' " + auditLogContext(auditRequestWithContext);
            logToStroom(AuditRequestWithContext.class, msg, auditRequestWithContext.getOriginalRequestId(), auditRequestWithContext.getUserId());
        });
        //handler for ProcessingStartedAuditRequest
        DISPATCH.put(ProcessingStartedAuditRequest.class, (o) -> {
            requireNonNull(o, "processingStartedAuditRequest");
            ProcessingStartedAuditRequest processingStartedAuditRequest = (ProcessingStartedAuditRequest) o;
            final String msg = " 'processingStarted' " + auditLogContext(processingStartedAuditRequest)
                    + processingStartedAuditRequest.getUser().toString()
                    + "' accessed '" + processingStartedAuditRequest.getLeafResource().getId()
                    + "' and it was processed using '" + processingStartedAuditRequest.getHowItWasProcessed();
            logToStroom(ProcessingStartedAuditRequest.class, msg, processingStartedAuditRequest.getOriginalRequestId(), processingStartedAuditRequest.getUserId());
        });
        //handler for RequestReceivedAuditRequest
        DISPATCH.put(RequestReceivedAuditRequest.class, (o) -> {
            requireNonNull(o, "RequestReceivedAuditRequest");
            AuditRequestWithContext auditRequestWithContext = (AuditRequestWithContext) o;
            final String msg = " auditRequest " + auditLogContext(auditRequestWithContext);
            logToStroom(AuditRequestWithContext.class, msg, auditRequestWithContext.getOriginalRequestId(), auditRequestWithContext.getUserId());
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
            logToStroom(ReadRequestExceptionAuditRequest.class, msg, readRequestExceptionAuditRequest.getRequestId(), null);
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
            logToStroom(ReadRequestReceivedAuditRequest.class, msg, readRequestReceivedAuditRequest.getRequestId(), null);
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
            logToStroom(ReadResponseAuditRequest.class, msg, readResponseAuditRequest.getRequestId(), null);
        });
    }

    public StroomSimpleAuditService() {
        //Create the EVENT_LOGGING_SYSTEM that is logging the authenticat event
        EVENT_LOGGING_SYSTEM.setName("Palisade Audit Service");
        EVENT_LOGGING_SYSTEM.setEnvironment("Test");
        String hostName = "";
        String hostAddress = "";


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
