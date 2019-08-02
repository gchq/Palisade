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
import event.logging.Authorisation;
import event.logging.Classification;
import event.logging.Criteria;
import event.logging.Data;
import event.logging.Event;
import event.logging.ObjectOutcome;
import event.logging.Outcome;
import event.logging.Purpose;
import event.logging.Search;
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
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.service.ServiceState;

import java.math.BigInteger;
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

    protected static final String REGISTER_REQUEST_RECEIVED_DESCRIPTION = "Audits the fact that a user has requested approval to access a data resource with a given purpose. All the details have been provided by the user and not yet validated.";
    protected static final String REGISTER_REQUEST_COMPLETED_DESCRIPTION = "Audits the fact that this request for data has been approved and these are the resources they have been given course grain approval to query.";
    protected static final String REGISTER_REQUEST_AUTHENTICATION_SUCCESS_DESCRIPTION = "Audits the fact that for this request for data the user has been successfully authenticated.";
    protected static final String REGISTER_REQUEST_EXCEPTION_DESCRIPTION = "Audits the fact that the request failed and therefore this request will be denied, most likely because the resource they requested does not exist or there is a fault in the system configuration.";
    protected static final String REGISTER_REQUEST_USER_EXCEPTION_DESCRIPTION = "Audits the fact that the user authentication failed and therefore this request will be denied.";
    protected static final String READ_REQUEST_RECEIVED_DESCRIPTION = "Audits the fact that a user has requested access to a specific data resource.";
    protected static final String READ_REQUEST_COMPLETED_DESCRIPTION = "Audits the fact that a user has finished reading a specific data resource.";
    protected static final String READ_REQUEST_EXCEPTION_DESCRIPTION = "Audits the fact that an exception occurred while reading a specific data resource.";
    protected static final String READ_RESPONSE_DESCRIPTION = "Audits the fact that the stream of data from a specific data resource has started to be returned to the client.";

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
            eventSource.setClient(DeviceUtil.createDevice(registerRequestReceivedAuditRequest.getServerHostname(), registerRequestReceivedAuditRequest.getServerIp()));
            // create View request event detail
            Event.EventDetail eventDetail = new Event.EventDetail();
            eventDetail.setDescription(REGISTER_REQUEST_RECEIVED_DESCRIPTION);
            ObjectOutcome view = new ObjectOutcome();
            // log the resource id being requested
            event.logging.Object resource = new event.logging.Object();
            resource.setId(registerRequestReceivedAuditRequest.getResourceId());
            resource.setType("Request");
            view.getObjects().add(resource);
            eventDetail.setView(view);
            // log the context that was supplied with the request
            Purpose purpose = new Purpose();
            purpose.setJustification(registerRequestReceivedAuditRequest.getContext().getPurpose());
            eventDetail.setPurpose(purpose);
            event.setEventDetail(eventDetail);
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
            Event.EventDetail authenticationEventDetail = new Event.EventDetail();
            authenticationEventDetail.setDescription(REGISTER_REQUEST_AUTHENTICATION_SUCCESS_DESCRIPTION);
            Event.EventDetail.Authenticate authenticate = new Event.EventDetail.Authenticate();
            authenticate.setUser(user);
            authenticate.setAction(AuthenticateAction.CONNECT);
            AuthenticateOutcome authenticateOutcome = new AuthenticateOutcome();
            authenticateOutcome.setSuccess(true);
            authenticate.setOutcome(authenticateOutcome);
            authenticationEventDetail.setAuthenticate(authenticate);
            authenticationEvent.setEventDetail(authenticationEventDetail);
            // send the authenticate audit log
            EVENT_LOGGING_SERVICE.log(authenticationEvent);

            // log the resources that the user is approved to access (authorisation)
            Event authorisationEvent = generateNewGenericEvent(registerRequestCompleteAuditRequest);
            Event.EventDetail authorisationEventDetail = new Event.EventDetail();
            authorisationEventDetail.setDescription(REGISTER_REQUEST_COMPLETED_DESCRIPTION);
            // log the list of resources
            ObjectOutcome view = new ObjectOutcome();
            Outcome outcome;
            // if no files then view request failure
            Set<LeafResource> resources = registerRequestCompleteAuditRequest.getLeafResources();
            if (resources.isEmpty()) {
                outcome = createOutcome(false);
            } else {
                for (LeafResource resource : resources) {
                    event.logging.Object stroomResource = new event.logging.Object();
                    stroomResource.setId(resource.getId());
                    stroomResource.setType(resource.getType());
                    view.getObjects().add(stroomResource);
                }
                outcome = createOutcome(true);
            }
            authorisationEventDetail.setView(view);
            Event.EventDetail.Authorise authorise = new Event.EventDetail.Authorise();
            authorise.setOutcome(outcome);
            authorise.setAction(Authorisation.REQUEST);
            authorisationEventDetail.setAuthorise(authorise);
            authorisationEvent.setEventDetail(authorisationEventDetail);
            EVENT_LOGGING_SERVICE.log(authorisationEvent);
        });
        //handler for RegisterRequestExceptionAuditRequest
        DISPATCH.put(RegisterRequestExceptionAuditRequest.class, (o) -> {
            requireNonNull(o, "RegisterRequestExceptionAuditRequest cannot be null");
            RegisterRequestExceptionAuditRequest registerRequestExceptionAuditRequest = (RegisterRequestExceptionAuditRequest) o;
            // view exception or authentication exception (if came from user service)
            Event exceptionEvent = generateNewGenericEvent(registerRequestExceptionAuditRequest);
            Event.EventDetail exceptionEventDetail = new Event.EventDetail();
            if (registerRequestExceptionAuditRequest.getServiceClass().getSimpleName().equals("UserService")) {
                exceptionEventDetail.setDescription(REGISTER_REQUEST_USER_EXCEPTION_DESCRIPTION);
                Event.EventDetail.Authorise authorise = new Event.EventDetail.Authorise();
                authorise.setOutcome(createOutcome(false));
                exceptionEventDetail.setAuthorise(authorise);
            } else {
                exceptionEventDetail.setDescription(REGISTER_REQUEST_EXCEPTION_DESCRIPTION);
                ObjectOutcome viewOutcome = new ObjectOutcome();
                viewOutcome.setOutcome(createOutcome(false));
                exceptionEventDetail.setView(viewOutcome);
            }
            Event.EventDetail.Process exceptionProcess = new Event.EventDetail.Process();
            Outcome exceptionOutcome = new Outcome();
            exceptionOutcome.setDescription(registerRequestExceptionAuditRequest.getException().getMessage());
            exceptionProcess.setOutcome(exceptionOutcome);
            exceptionEventDetail.setProcess(exceptionProcess);
            exceptionEvent.setEventDetail(exceptionEventDetail);
            EVENT_LOGGING_SERVICE.log(exceptionEvent);
        });
        //handler for ReadRequestReceivedAuditRequest
        DISPATCH.put(ReadRequestReceivedAuditRequest.class, (o) -> {
            requireNonNull(o, "ReadRequestReceivedAuditRequest cannot be null");
            ReadRequestReceivedAuditRequest readRequestReceivedAuditRequest = (ReadRequestReceivedAuditRequest) o;
            // view request to acknowledge that a request to view data has been received
            Event viewEvent = generateNewGenericEvent(readRequestReceivedAuditRequest);
            Event.EventDetail viewEventDetail = new Event.EventDetail();
            viewEventDetail.setDescription(READ_REQUEST_RECEIVED_DESCRIPTION);
            ObjectOutcome view = new ObjectOutcome();
            LeafResource resource = readRequestReceivedAuditRequest.getResource();
            event.logging.Object stroomResource = new event.logging.Object();
            stroomResource.setId(resource.getId());
            stroomResource.setType(resource.getType());
            view.getObjects().add(stroomResource);
            viewEventDetail.setView(view);
            viewEvent.setEventDetail(viewEventDetail);
            EVENT_LOGGING_SERVICE.log(viewEvent);
        });
        //handler for ReadRequestCompleteAuditRequest
        DISPATCH.put(ReadRequestCompleteAuditRequest.class, (o) -> {
            requireNonNull(o, "ReadRequestCompleteAuditRequest cannot be null");
            ReadRequestCompleteAuditRequest readRequestCompleteAuditRequest = (ReadRequestCompleteAuditRequest) o;
            // view request
            Event viewEvent = generateNewGenericEvent(readRequestCompleteAuditRequest);
            Event.EventDetail viewEventDetail = new Event.EventDetail();
            viewEventDetail.setDescription(READ_REQUEST_COMPLETED_DESCRIPTION);
            Search search = new Search();
            search.setOutcome(createOutcome(true));
            // set the number of records returned
            search.setTotalResults(BigInteger.valueOf(readRequestCompleteAuditRequest.getNumberOfRecordsReturned()));
            // set the resource that those records were read from
            Criteria.DataSources dataSources = new Criteria.DataSources();
            dataSources.getDataSource().add(readRequestCompleteAuditRequest.getResource().getId());
            search.setDataSources(dataSources);
            viewEventDetail.setSearch(search);
            viewEvent.setEventDetail(viewEventDetail);
            EVENT_LOGGING_SERVICE.log(viewEvent);
        });
        //handler for ReadRequestExceptionAuditRequest
        DISPATCH.put(ReadRequestExceptionAuditRequest.class, (o) -> {
            requireNonNull(o, "ReadRequestExceptionAuditRequest cannot be null");
            ReadRequestExceptionAuditRequest readRequestExceptionAuditRequest = (ReadRequestExceptionAuditRequest) o;
            // view request
            Event viewEvent = generateNewGenericEvent(readRequestExceptionAuditRequest);
            Event.EventDetail viewEventDetail = new Event.EventDetail();
            viewEventDetail.setDescription(READ_REQUEST_EXCEPTION_DESCRIPTION);
            Search search = new Search();
            search.setOutcome(createOutcome(false));
            // set the exception details
            search.getOutcome().setDescription(readRequestExceptionAuditRequest.getException().getMessage());
            // set the resource that those records were read from
            Criteria.DataSources dataSources = new Criteria.DataSources();
            dataSources.getDataSource().add(readRequestExceptionAuditRequest.getResource().getId());
            search.setDataSources(dataSources);
            viewEventDetail.setSearch(search);
            viewEvent.setEventDetail(viewEventDetail);
            EVENT_LOGGING_SERVICE.log(viewEvent);
        });
        //handler for ReadResponseAuditRequest
        DISPATCH.put(ReadResponseAuditRequest.class, (o) -> {
            requireNonNull(o, "ReadResponseAuditRequest cannot be null");
            ReadResponseAuditRequest readResponseAuditRequest = (ReadResponseAuditRequest) o;
            // view request
            Event viewEvent = generateNewGenericEvent(readResponseAuditRequest);
            Event.EventDetail viewEventDetail = new Event.EventDetail();
            viewEventDetail.setDescription(READ_RESPONSE_DESCRIPTION);
            ObjectOutcome view = new ObjectOutcome();
            // set the resource that those records were read from
            event.logging.Object resource = new event.logging.Object();
            resource.setId(readResponseAuditRequest.getResource().getId());
            resource.setType(readResponseAuditRequest.getResource().getType());
            view.getObjects().add(resource);
            Data data = new Data();
            data.setName(readResponseAuditRequest.getRulesApplied().getMessage());
            view.getData().add(data);
            viewEventDetail.setView(view);
            viewEvent.setEventDetail(viewEventDetail);
            EVENT_LOGGING_SERVICE.log(viewEvent);
        });
    }

    private static Outcome createOutcome(final boolean success) {
        Outcome outcome = new Outcome();
        outcome.setSuccess(success);
        return outcome;
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
