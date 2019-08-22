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
import event.logging.Authorisation;
import event.logging.Classification;
import event.logging.Data;
import event.logging.Event;
import event.logging.ObjectOutcome;
import event.logging.Outcome;
import event.logging.Purpose;
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
import uk.gov.gchq.palisade.audit.service.request.RegisterRequestCompleteAuditRequest;
import uk.gov.gchq.palisade.audit.service.request.RegisterRequestExceptionAuditRequest;
import uk.gov.gchq.palisade.exception.NoConfigException;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.service.PalisadeService;
import uk.gov.gchq.palisade.service.ServiceState;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * A StroomAuditService is a simple implementation of an {@link AuditService} that simply constructs a message and logs
 * it using the Stroom EventLoggingService.
 */
public class StroomAuditService implements AuditService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StroomAuditService.class);
    private static final Map<Class, Consumer<Object>> DISPATCH = new HashMap<Class, Consumer<Object>>();

    private static final DefaultEventLoggingService EVENT_LOGGING_SERVICE = new DefaultEventLoggingService();
    private static final System SYSTEM = new System();
    private static final String EVENT_GENERATOR = "Palisade";

    protected static final String REGISTER_REQUEST_NO_RESOURCES_TYPE_ID = "REGISTER_REQUEST_NO_RESOURCES";
    protected static final String REGISTER_REQUEST_NO_RESOURCES_DESCRIPTION = "Audits the fact that the user requested access to some resources however they do not have permission to access any of those resources.";
    protected static final String REGISTER_REQUEST_NO_RESOURCES_OUTCOME_DESCRIPTION = "The user does not have permission to access any of those resources.";
    protected static final String REGISTER_REQUEST_COMPLETED_TYPE_ID = "REGISTER_REQUEST_COMPLETED";
    protected static final String REGISTER_REQUEST_COMPLETED_DESCRIPTION = "Audits the fact that this request for data has been approved and these are the resources they have been given course grain approval to query.";
    protected static final String REGISTER_REQUEST_EXCEPTION_USER_TYPE_ID = "REGISTER_REQUEST_EXCEPTION_USER";
    protected static final String REGISTER_REQUEST_EXCEPTION_USER_DESCRIPTION = "Audits the fact that the user could not be authenticated by the system and therefore the request has been denied.";
    protected static final String REGISTER_REQUEST_EXCEPTION_USER_OUTCOME_DESCRIPTION = "The user could not be authenticated by the system.";
    protected static final String REGISTER_REQUEST_EXCEPTION_RESOURCE_TYPE_ID = "REGISTER_REQUEST_EXCEPTION_RESOURCE";
    protected static final String REGISTER_REQUEST_EXCEPTION_RESOURCE_DESCRIPTION = "Audits the fact that the supplied resource id could not be resolved and therefore the request has been denied.";
    protected static final String REGISTER_REQUEST_EXCEPTION_RESOURCE_OUTCOME_DESCRIPTION = "The supplied resource id could not be resolved.";
    protected static final String REGISTER_REQUEST_EXCEPTION_OTHER_TYPE_ID = "REGISTER_REQUEST_EXCEPTION_OTHER";
    protected static final String REGISTER_REQUEST_EXCEPTION_OTHER_DESCRIPTION = "Audits the fact that for some reason the request has thrown an exception and therefore the request has been denied";
    protected static final String READ_REQUEST_COMPLETED_TYPE_ID = "READ_REQUEST_COMPLETED";
    protected static final String READ_REQUEST_COMPLETED_DESCRIPTION = "Audits the fact that a user has finished reading a specific data resource.";
    protected static final String READ_REQUEST_EXCEPTION_TOKEN_TYPE_ID = "READ_REQUEST_EXCEPTION_TOKEN";
    protected static final String READ_REQUEST_EXCEPTION_TOKEN_DESCRIPTION = "Audits the fact that the provided token is invalid, probably because it the request wasn't registered first and therefore the request has been denied.";
    protected static final String READ_REQUEST_EXCEPTION_TOKEN_OUTCOME_DESCRIPTION = "The provided token is invalid.";
    protected static final String READ_REQUEST_EXCEPTION_OTHER_TYPE_ID = "READ_REQUEST_EXCEPTION_OTHER";
    protected static final String READ_REQUEST_EXCEPTION_OTHER_DESCRIPTION = "Audits the fact that an exception was thrown when trying to provide the data to the user.";

    public StroomAuditService() {
    }

    /**
     * @param systemName the name of the system from which the audit service is receiving audit logs from
     * @return {@link StroomAuditService}
     */
    public StroomAuditService systemName(final String systemName) {
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
     * @return {@link StroomAuditService}
     */
    public StroomAuditService organisation(final String organisation) {
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
     * @return {@link StroomAuditService}
     */
    public StroomAuditService systemEnv(final String env) {
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
     * @return {@link StroomAuditService}
     */
    public StroomAuditService systemDescription(final String description) {
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
     * @return {@link StroomAuditService}
     */
    public StroomAuditService systemVersion(final String systemVersion) {
        requireNonNull(systemVersion, "The systemVersion cannot be null.");
        SYSTEM.setVersion(systemVersion);
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
     * @return {@link StroomAuditService}
     */
    public StroomAuditService systemClassification(final String systemClassification) {
        requireNonNull(systemClassification, "The systemClassification cannot be null.");
        Classification classification = new Classification();
        classification.setText(systemClassification);
        SYSTEM.setClassification(classification);
        return this;
    }

    public void setSystemClassification(final String systemClassification) {
        systemClassification(systemClassification);
    }

    public String getSystemClassification() {
        return SYSTEM.getClassification().getText();
    }

    private static Event generateNewGenericEvent(final AuditRequest request) {
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
        event.setEventChain(eventChain);
        // set the event source
        Event.EventSource eventSource = new Event.EventSource();
        eventSource.setSystem(SYSTEM);
        eventSource.setGenerator(EVENT_GENERATOR);
        eventSource.setDevice(DeviceUtil.createDevice(request.getServerHostname(), request.getServerIp()));
        event.setEventSource(eventSource);
        return event;
    }

    private static void addUserToEvent(final Event event, final uk.gov.gchq.palisade.UserId user) {
        Event.EventSource eventSource = event.getEventSource();
        User stroomUser = EventLoggingUtil.createUser(user.getId());
        eventSource.setUser(stroomUser);
    }

    private static void addPurposeToEvent(final Event event, final uk.gov.gchq.palisade.Context context) {
        Event.EventDetail eventDetail = event.getEventDetail();
        Purpose purpose = new Purpose();
        purpose.setJustification(context.getPurpose());
        eventDetail.setPurpose(purpose);
    }

    private static Outcome createOutcome(final boolean success) {
        Outcome outcome = new Outcome();
        outcome.setSuccess(success);
        return outcome;
    }

    //translate class object to handler
    static {
        //handler for RegisterRequestCompleteAuditRequest
        DISPATCH.put(RegisterRequestCompleteAuditRequest.class, (o) -> {
            requireNonNull(o, "RegisterRequestCompleteAuditRequest cannot be null");
            RegisterRequestCompleteAuditRequest registerRequestCompleteAuditRequest = (RegisterRequestCompleteAuditRequest) o;
            Event authorisationEvent = generateNewGenericEvent(registerRequestCompleteAuditRequest);
            Event.EventDetail authorisationEventDetail = new Event.EventDetail();
            authorisationEvent.setEventDetail(authorisationEventDetail);
            // log the user
            addUserToEvent(authorisationEvent, registerRequestCompleteAuditRequest.getUser().getUserId());
            // log the purpose that was supplied with the request
            addPurposeToEvent(authorisationEvent, registerRequestCompleteAuditRequest.getContext());
            // log the list of resources
            Event.EventDetail.Authorise authorise = new Event.EventDetail.Authorise();
            Outcome outcome;
            // if no files then authorisation request failure
            Set<LeafResource> resources = registerRequestCompleteAuditRequest.getLeafResources();
            if (resources.isEmpty()) {
                authorisationEventDetail.setTypeId(REGISTER_REQUEST_NO_RESOURCES_TYPE_ID);
                authorisationEventDetail.setDescription(REGISTER_REQUEST_NO_RESOURCES_DESCRIPTION);
                outcome = createOutcome(false);
                outcome.setDescription(REGISTER_REQUEST_NO_RESOURCES_OUTCOME_DESCRIPTION);
            } else {
                authorisationEventDetail.setTypeId(REGISTER_REQUEST_COMPLETED_TYPE_ID);
                authorisationEventDetail.setDescription(REGISTER_REQUEST_COMPLETED_DESCRIPTION);
                for (LeafResource resource : resources) {
                    event.logging.Object stroomResource = new event.logging.Object();
                    stroomResource.setId(resource.getId());
                    stroomResource.setType(resource.getType());
                    authorise.getObjects().add(stroomResource);
                }
                outcome = createOutcome(true);
            }
            authorise.setOutcome(outcome);
            authorise.setAction(Authorisation.REQUEST);
            authorisationEventDetail.setAuthorise(authorise);
            EVENT_LOGGING_SERVICE.log(authorisationEvent);
        });
        //handler for RegisterRequestExceptionAuditRequest
        DISPATCH.put(RegisterRequestExceptionAuditRequest.class, (o) -> {
            requireNonNull(o, "RegisterRequestExceptionAuditRequest cannot be null");
            RegisterRequestExceptionAuditRequest registerRequestExceptionAuditRequest = (RegisterRequestExceptionAuditRequest) o;
            // authorisation exception
            Event exceptionEvent = generateNewGenericEvent(registerRequestExceptionAuditRequest);
            Event.EventDetail exceptionEventDetail = new Event.EventDetail();
            exceptionEvent.setEventDetail(exceptionEventDetail);
            // log the user
            addUserToEvent(exceptionEvent, registerRequestExceptionAuditRequest.getUserId());
            // log the purpose that was supplied with the request
            addPurposeToEvent(exceptionEvent, registerRequestExceptionAuditRequest.getContext());
            Event.EventDetail.Authorise authorise = new Event.EventDetail.Authorise();
            // log the resource
            event.logging.Object stroomResource = new event.logging.Object();
            stroomResource.setId(registerRequestExceptionAuditRequest.getResourceId());
            authorise.getObjects().add(stroomResource);
            Outcome outcome = createOutcome(false);
            if (registerRequestExceptionAuditRequest.getServiceClass().getSimpleName().equals("UserService")) {
                exceptionEventDetail.setTypeId(REGISTER_REQUEST_EXCEPTION_USER_TYPE_ID);
                exceptionEventDetail.setDescription(REGISTER_REQUEST_EXCEPTION_USER_DESCRIPTION);
                outcome.setDescription(REGISTER_REQUEST_EXCEPTION_USER_OUTCOME_DESCRIPTION);
            } else if (registerRequestExceptionAuditRequest.getServiceClass().getSimpleName().equals("ResourceService")) {
                exceptionEventDetail.setTypeId(REGISTER_REQUEST_EXCEPTION_RESOURCE_TYPE_ID);
                exceptionEventDetail.setDescription(REGISTER_REQUEST_EXCEPTION_RESOURCE_DESCRIPTION);
                outcome.setDescription(REGISTER_REQUEST_EXCEPTION_RESOURCE_OUTCOME_DESCRIPTION);
            } else {
                exceptionEventDetail.setTypeId(REGISTER_REQUEST_EXCEPTION_OTHER_TYPE_ID);
                exceptionEventDetail.setDescription(REGISTER_REQUEST_EXCEPTION_OTHER_DESCRIPTION);
                outcome.setDescription(registerRequestExceptionAuditRequest.getException().getMessage());
            }
            authorise.setOutcome(outcome);
            authorise.setAction(Authorisation.REQUEST);
            exceptionEventDetail.setAuthorise(authorise);
            EVENT_LOGGING_SERVICE.log(exceptionEvent);
        });
        //handler for ReadRequestCompleteAuditRequest
        DISPATCH.put(ReadRequestCompleteAuditRequest.class, (o) -> {
            requireNonNull(o, "ReadRequestCompleteAuditRequest cannot be null");
            ReadRequestCompleteAuditRequest readRequestCompleteAuditRequest = (ReadRequestCompleteAuditRequest) o;
            // view request
            Event viewEvent = generateNewGenericEvent(readRequestCompleteAuditRequest);
            Event.EventDetail viewEventDetail = new Event.EventDetail();
            viewEvent.setEventDetail(viewEventDetail);
            viewEventDetail.setTypeId(READ_REQUEST_COMPLETED_TYPE_ID);
            viewEventDetail.setDescription(READ_REQUEST_COMPLETED_DESCRIPTION);
            // log the user
            addUserToEvent(viewEvent, readRequestCompleteAuditRequest.getUser().getUserId());
            // log the purpose that was supplied with the request
            addPurposeToEvent(viewEvent, readRequestCompleteAuditRequest.getContext());
            // log event outcome
            ObjectOutcome view = new ObjectOutcome();
            viewEventDetail.setView(view);
            view.setOutcome(createOutcome(true));
            // set the number of records returned
            Data resultsReturned = new Data();
            resultsReturned.setName("Number of records returned");
            resultsReturned.setValue(String.valueOf(readRequestCompleteAuditRequest.getNumberOfRecordsReturned()));
            view.getData().add(resultsReturned);
            Data resultsProcessed = new Data();
            resultsProcessed.setName("Number of records processed");
            resultsProcessed.setValue(String.valueOf(readRequestCompleteAuditRequest.getNumberOfRecordsProcessed()));
            view.getData().add(resultsProcessed);
            Data rulesApplied = new Data();
            rulesApplied.setName("Rules applied");
            rulesApplied.setValue(String.valueOf(readRequestCompleteAuditRequest.getRulesApplied().getMessage()));
            view.getData().add(rulesApplied);
            // set the resource that those records were read from
            event.logging.Object resource = new event.logging.Object();
            resource.setId(readRequestCompleteAuditRequest.getResource().getId());
            resource.setType(readRequestCompleteAuditRequest.getResource().getType());
            view.getObjects().add(resource);
            EVENT_LOGGING_SERVICE.log(viewEvent);
        });
        //handler for ReadRequestExceptionAuditRequest
        DISPATCH.put(ReadRequestExceptionAuditRequest.class, (o) -> {
            requireNonNull(o, "ReadRequestExceptionAuditRequest cannot be null");
            ReadRequestExceptionAuditRequest readRequestExceptionAuditRequest = (ReadRequestExceptionAuditRequest) o;
            // view request
            Event viewEvent = generateNewGenericEvent(readRequestExceptionAuditRequest);
            Event.EventDetail viewEventDetail = new Event.EventDetail();
            viewEvent.setEventDetail(viewEventDetail);
            ObjectOutcome view = new ObjectOutcome();
            viewEventDetail.setView(view);
            Outcome outcome = createOutcome(false);
            view.setOutcome(outcome);
            if (readRequestExceptionAuditRequest.getException().getMessage().startsWith(PalisadeService.TOKEN_NOT_FOUND_MESSAGE)) {
                viewEventDetail.setTypeId(READ_REQUEST_EXCEPTION_TOKEN_TYPE_ID);
                viewEventDetail.setDescription(READ_REQUEST_EXCEPTION_TOKEN_DESCRIPTION);
                outcome.setDescription(READ_REQUEST_EXCEPTION_TOKEN_OUTCOME_DESCRIPTION);
            } else {
                viewEventDetail.setTypeId(READ_REQUEST_EXCEPTION_OTHER_TYPE_ID);
                viewEventDetail.setDescription(READ_REQUEST_EXCEPTION_OTHER_DESCRIPTION);
                outcome.setDescription(readRequestExceptionAuditRequest.getException().getMessage());
            }
            // set the resource that those records were read from
            event.logging.Object resource = new event.logging.Object();
            resource.setId(readRequestExceptionAuditRequest.getResource().getId());
            resource.setType(readRequestExceptionAuditRequest.getResource().getType());
            view.getObjects().add(resource);
            // set the token used for this read request
            Data token = new Data();
            token.setName("token");
            token.setValue(readRequestExceptionAuditRequest.getToken());
            view.getData().add(token);
            EVENT_LOGGING_SERVICE.log(viewEvent);
        });
    }

    @Override
    public CompletableFuture<Boolean> audit(final AuditRequest request) {
        requireNonNull(request, "The audit request can not be null.");
        Consumer<java.lang.Object> handler = DISPATCH.get((request.getClass()));
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
