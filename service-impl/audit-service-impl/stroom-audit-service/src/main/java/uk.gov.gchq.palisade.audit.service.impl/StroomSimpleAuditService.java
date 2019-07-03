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

import event.logging.Activity;
import event.logging.AuthenticateAction;
import event.logging.AuthenticateOutcome;
import event.logging.Classification;
import event.logging.Event;
import event.logging.ObjectOutcome;
import event.logging.System;
import event.logging.User;
import event.logging.impl.DefaultEventLoggingService;
import event.logging.util.DeviceUtil;
import event.logging.util.EventLoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.gchq.palisade.audit.service.AuditService;
import uk.gov.gchq.palisade.audit.service.request.AuditRequest;
import uk.gov.gchq.palisade.audit.service.request.ReadRequestCompleteAuditRequest;
import uk.gov.gchq.palisade.audit.service.request.ReadRequestExceptionAuditRequest;
import uk.gov.gchq.palisade.audit.service.request.ReadRequestReceivedAuditRequest;
import uk.gov.gchq.palisade.audit.service.request.ReadResponseAuditRequest;
import uk.gov.gchq.palisade.audit.service.request.RegisterRequestCompleteAuditRequest;
import uk.gov.gchq.palisade.audit.service.request.RegisterRequestExceptionAuditRequest;
import uk.gov.gchq.palisade.audit.service.request.RegisterRequestReceivedAuditRequest;
import uk.gov.gchq.palisade.exception.NoConfigException;
import uk.gov.gchq.palisade.service.ServiceState;

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

    private static final DefaultEventLoggingService EVENT_LOGGING_SERVICE = new DefaultEventLoggingService();
    private static final System SYSTEM = new System();
    private static final String EVENT_GENERATOR = "Palisade";

    public StroomSimpleAuditService() {
    }

    /**
     * @param systemName the name of the system from which the audit service is receiving audit logs from
     * @return {@link StroomSimpleAuditService}
     */
    public StroomSimpleAuditService systemName(final String systemName) {
        requireNonNull(systemName, "The system name cannot be null.");
        SYSTEM.setName(systemName);
        return this;
    }

    public void setSystemName(final String systemName) {
        systemName(systemName);
    }

    public String getSystemName() {
        return SYSTEM.getName();
    }

    /**
     * @param organisation the organisation that the system belongs too
     * @return {@link StroomSimpleAuditService}
     */
    public StroomSimpleAuditService organisation(final String organisation) {
        requireNonNull(organisation, "The organisation cannot be null.");
        SYSTEM.setOrganisation(organisation);
        return this;
    }

    public void setOrganisation(final String organisation) {
        organisation(organisation);
    }

    public String getOrganisation() {
        return SYSTEM.getOrganisation();
    }

    /**
     * @param env the system environment of this deployment, e.g prod, ref, test
     * @return {@link StroomSimpleAuditService}
     */
    public StroomSimpleAuditService systemEnv(final String env) {
        requireNonNull(env, "The env cannot be null.");
        SYSTEM.setEnvironment(env);
        return this;
    }

    public void setSystemEnv(final String systemEnv) {
        systemEnv(systemEnv);
    }

    public String getSystemEnv() {
        return SYSTEM.getEnvironment();
    }

    /**
     * @param description the description of the system from which the audit service is receiving audit logs from
     * @return {@link StroomSimpleAuditService}
     */
    public StroomSimpleAuditService systemDescription(final String description) {
        requireNonNull(description, "The description cannot be null.");
        SYSTEM.setDescription(description);
        return this;
    }

    public void setSystemDescription(final String description) {
        systemDescription(description);
    }

    public String getSystemDescription() {
        return SYSTEM.getDescription();
    }

    /**
     * @param systemVersion the system version of this deployment, v1, v1.0.2, v2, etc
     * @return {@link StroomSimpleAuditService}
     */
    public StroomSimpleAuditService systemVersion(final String systemVersion) {
        requireNonNull(systemVersion, "The systemVersion cannot be null.");
        SYSTEM.setEnvironment(systemVersion);
        return this;
    }

    public void setSystemVersion(final String systemVersion) {
        systemVersion(systemVersion);
    }

    public String getSystemVersion() {
        return SYSTEM.getVersion();
    }

    /**
     * @param systemClassification the classification of the system from which the audit service is receiving audit logs from
     * @return {@link StroomSimpleAuditService}
     */
    public StroomSimpleAuditService systemClassification(final String systemClassification) {
        requireNonNull(systemClassification, "The systemClassification cannot be null.");
        Classification classification = new Classification();
        classification.setText(systemClassification);
        SYSTEM.setClassification(classification);
        return this;
    }

    public void setSystemlassification(final String systemClassification) {
        systemClassification(systemClassification);
    }

    public String getSystemlassification() {
        return SYSTEM.getClassification().getText();
    }

    private static Event generateNewGenericEvent(final AuditRequest request){
        Event event = EVENT_LOGGING_SERVICE.createEvent();
        // set the event time
        Event.EventTime eventTime = EventLoggingUtil.createEventTime(request.getTimestamp());
        event.setEventTime(eventTime);
        // set the event chain
        Event.EventChain eventChain = new Event.EventChain();
        Activity parent = new Activity();
        parent.setId(request.getOriginalRequestId().getId());
        Activity activity = new Activity();
        activity.setParent(parent);
        activity.setId(request.getId().getId());
        eventChain.setActivity(activity);
        // set the event source
        Event.EventSource eventSource = new Event.EventSource();
        eventSource.setSystem(SYSTEM);
        eventSource.setGenerator(EVENT_GENERATOR);
        eventSource.setServer(DeviceUtil.createDevice(request.getServerHostname(), request.getServerIp()));
        event.setEventSource(eventSource);
        return event;
    }

    //translate class object to handler
    static {
        //handler for RegisterRequestReceivedAuditRequest
        DISPATCH.put(RegisterRequestReceivedAuditRequest.class, (o) -> {
            requireNonNull(o, "RegisterRequestReceivedAuditRequest cannot be null");
            RegisterRequestReceivedAuditRequest registerRequestReceivedAuditRequest = (RegisterRequestReceivedAuditRequest) o;

            Event event = generateNewGenericEvent(registerRequestReceivedAuditRequest);
            // log who the user is claiming to be
            Event.EventSource eventSource = event.getEventSource();
            eventSource.setUser(EventLoggingUtil.createUser(registerRequestReceivedAuditRequest.getUserId().getId()));
            // log where the client (palisade service) that the audit request came from
            eventSource.setClient(DeviceUtil.createDevice(registerRequestReceivedAuditRequest.getClientHostname(), registerRequestReceivedAuditRequest.getClientIp()));
            // TODO log the resource id being requested

            // TODO log the context that was supplied with the request

            // TODO create View request event detail???
            EventLoggingUtil.createEventDetail("???", "???");
            Event.EventDetail eventDetail = new Event.EventDetail();
            eventDetail.setView(new ObjectOutcome());
//            event.setEventDetail(eventDetail);
            EVENT_LOGGING_SERVICE.log(event);
        });
        //handler for RegisterRequestCompleteAuditRequest
        DISPATCH.put(RegisterRequestCompleteAuditRequest.class, (o) -> {
            requireNonNull(o, "RegisterRequestCompleteAuditRequest cannot be null");
            RegisterRequestCompleteAuditRequest registerRequestCompleteAuditRequest = (RegisterRequestCompleteAuditRequest) o;

            // create authentication audit log
            Event authenticationEvent = generateNewGenericEvent(registerRequestCompleteAuditRequest);
            Event.EventSource eventSource = authenticationEvent.getEventSource();
            // log the user
            User user = EventLoggingUtil.createUser(registerRequestCompleteAuditRequest.getUser().getUserId().getId());
            eventSource.setUser(user);
            // log authentication event
            Event.EventDetail eventDetail = new Event.EventDetail();
            Event.EventDetail.Authenticate authenticate = new Event.EventDetail.Authenticate();
            authenticate.setUser(user);
            authenticate.setAction(AuthenticateAction.CONNECT);
            AuthenticateOutcome authenticateOutcome = new AuthenticateOutcome();
            authenticateOutcome.setSuccess(true);
            authenticate.setOutcome(authenticateOutcome);
            eventDetail.setAuthenticate(authenticate);
            authenticationEvent.setEventDetail(eventDetail);
            // send the authenticate audit log
            EVENT_LOGGING_SERVICE.log(authenticationEvent);

            // TODO log the resources that the user is approved to access (authorisation)
            Event event = generateNewGenericEvent(registerRequestCompleteAuditRequest);
            // log the list of resources

            // log the trusted user information

        });
        //handler for RegisterRequestExceptionAuditRequest
        DISPATCH.put(RegisterRequestExceptionAuditRequest.class, (o) -> {
            requireNonNull(o, "RegisterRequestExceptionAuditRequest cannot be null");
            RegisterRequestExceptionAuditRequest registerRequestExceptionAuditRequest = (RegisterRequestExceptionAuditRequest) o;


        });
        //handler for ReadRequestReceivedAuditRequest
        DISPATCH.put(ReadRequestReceivedAuditRequest.class, (o) -> {
            requireNonNull(o, "ReadRequestReceivedAuditRequest cannot be null");
            ReadRequestReceivedAuditRequest readRequestReceivedAuditRequest = (ReadRequestReceivedAuditRequest) o;

        });
        //handler for ReadRequestCompleteAuditRequest
        DISPATCH.put(ReadRequestCompleteAuditRequest.class, (o) -> {
            requireNonNull(o, "ReadRequestCompleteAuditRequest cannot be null");
            ReadRequestCompleteAuditRequest readRequestCompleteAuditRequest = (ReadRequestCompleteAuditRequest) o;

        });
        //handler for ReadRequestExceptionAuditRequest
        DISPATCH.put(ReadRequestExceptionAuditRequest.class, (o) -> {
            requireNonNull(o, "ReadRequestExceptionAuditRequest cannot be null");
            ReadRequestExceptionAuditRequest readRequestExceptionAuditRequest = (ReadRequestExceptionAuditRequest) o;

        });
        //handler for ReadResponseAuditRequest
        DISPATCH.put(ReadResponseAuditRequest.class, (o) -> {
            requireNonNull(o, "ReadResponseAuditRequest cannot be null");
            ReadResponseAuditRequest readResponseAuditRequest = (ReadResponseAuditRequest) o;

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
