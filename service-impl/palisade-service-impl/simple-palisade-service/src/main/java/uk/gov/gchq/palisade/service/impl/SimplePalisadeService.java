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

package uk.gov.gchq.palisade.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.ToStringBuilder;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.audit.service.AuditService;
import uk.gov.gchq.palisade.audit.service.request.ExceptionAuditRequest;
import uk.gov.gchq.palisade.audit.service.request.ProcessingStartedAuditRequest;
import uk.gov.gchq.palisade.audit.service.request.RequestReceivedAuditRequest;
import uk.gov.gchq.palisade.cache.service.CacheService;
import uk.gov.gchq.palisade.cache.service.request.AddCacheRequest;
import uk.gov.gchq.palisade.cache.service.request.GetCacheRequest;
import uk.gov.gchq.palisade.exception.NoConfigException;
import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.palisade.policy.service.MultiPolicy;
import uk.gov.gchq.palisade.policy.service.Policy;
import uk.gov.gchq.palisade.policy.service.PolicyService;
import uk.gov.gchq.palisade.policy.service.request.GetPolicyRequest;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.service.ResourceService;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByIdRequest;
import uk.gov.gchq.palisade.service.ConnectionDetail;
import uk.gov.gchq.palisade.service.PalisadeMetricProvider;
import uk.gov.gchq.palisade.service.PalisadeService;
import uk.gov.gchq.palisade.service.Service;
import uk.gov.gchq.palisade.service.ServiceState;
import uk.gov.gchq.palisade.service.request.DataRequestConfig;
import uk.gov.gchq.palisade.service.request.DataRequestResponse;
import uk.gov.gchq.palisade.service.request.GetDataRequestConfig;
import uk.gov.gchq.palisade.service.request.GetMetricRequest;
import uk.gov.gchq.palisade.service.request.RegisterDataRequest;
import uk.gov.gchq.palisade.service.request.Request;
import uk.gov.gchq.palisade.user.service.UserService;
import uk.gov.gchq.palisade.user.service.request.GetUserRequest;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

/**
 * <p> A simple implementation of a Palisade Service that just connects up the Audit, Cache, User, Policy and Resource
 * services. </p> <p> It currently doesn't validate that the user is actually requesting the correct resources. It
 * should check the resources requested in getDataRequestConfig are the same or a subset of the resources passed in in
 * registerDataRequest. </p>
 */
public class SimplePalisadeService implements PalisadeService, PalisadeMetricProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimplePalisadeService.class);
    //Cache keys
    public static final String RES_COUNT_KEY = "res_count_";
    //Configuration keys
    public static final String AUDIT_IMPL_KEY = "palisade.svc.audit.svc";
    public static final String POLICY_IMPL_KEY = "palisade.svc.policy.svc";
    public static final String USER_IMPL_KEY = "palisade.svc.user.svc";
    public static final String RESOURCE_IMPL_KEY = "palisade.svc.resource.svc";
    public static final String CACHE_IMPL_KEY = "palisade.svc.cache.svc";

    /**
     * Duration for how long the count of resources requested should live in the cache.
     */
    public static final Duration COUNT_PERSIST_DURATION = Duration.ofMinutes(10);

    private AuditService auditService;
    private PolicyService policyService;
    private UserService userService;
    private ResourceService resourceService;
    private CacheService cacheService;

    public SimplePalisadeService() {
    }

    @Override
    public void applyConfigFrom(final ServiceState config) throws NoConfigException {
        requireNonNull(config, "config");
        setAuditService(getFromConfig(config, AUDIT_IMPL_KEY, AuditService.class));
        setPolicyService(getFromConfig(config, POLICY_IMPL_KEY, PolicyService.class));
        setUserService(getFromConfig(config, USER_IMPL_KEY, UserService.class));
        setResourceService(getFromConfig(config, RESOURCE_IMPL_KEY, ResourceService.class));
        setCacheService(getFromConfig(config, CACHE_IMPL_KEY, CacheService.class));
    }

    /**
     * Given an {@link ServiceState} object, this will create an instance of the given service. The {@code key} gives the
     * name of the serialised form inside the configuration object.
     *
     * @param config       the configuration for this Palisade service
     * @param key          the config key name
     * @param serviceClass the class of the service to create
     * @param <S>          the service type
     * @return a created service
     * @throws NoConfigException if the required configuration item could not be found
     */
    private static <S extends Service> S getFromConfig(final ServiceState config, final String key, final Class<S> serviceClass) throws NoConfigException {
        requireNonNull(config, "config");
        requireNonNull(key, "key");
        requireNonNull(serviceClass, "serviceClass");
        String serialised = config.getOrDefault(key, null);
        if (nonNull(serialised)) {
            return JSONSerialiser.deserialise(serialised.getBytes(StandardCharsets.UTF_8), serviceClass);
        } else {
            throw new NoConfigException("no service specified in configuration for class " + serviceClass.getTypeName());
        }
    }

    @Override
    public void recordCurrentConfigTo(final ServiceState config) {
        requireNonNull(config, "config");
        config.put(PalisadeService.class.getTypeName(), getClass().getTypeName());
        storeInConfig(config, auditService, AUDIT_IMPL_KEY);
        storeInConfig(config, policyService, POLICY_IMPL_KEY);
        storeInConfig(config, userService, USER_IMPL_KEY);
        storeInConfig(config, resourceService, RESOURCE_IMPL_KEY);
        storeInConfig(config, cacheService, CACHE_IMPL_KEY);
    }

    /**
     * Store the given service into the configuration object with the given key.
     *
     * @param config  the configuration object to store the detail in
     * @param service the service to store
     * @param key     the key name
     */
    private static void storeInConfig(final ServiceState config, final Service service, final String key) {
        requireNonNull(config, "config");
        requireNonNull(service, "service");
        requireNonNull(key, "key");
        String serialised = new String(JSONSerialiser.serialise(service), StandardCharsets.UTF_8);
        config.put(key, serialised);
    }

    public SimplePalisadeService resourceService(final ResourceService resourceService) {
        requireNonNull(resourceService, "The resource service cannot be set to null.");
        this.resourceService = resourceService;
        return this;
    }

    public SimplePalisadeService auditService(final AuditService auditService) {
        requireNonNull(auditService, "The audit service cannot be set to null.");
        this.auditService = auditService;
        return this;
    }

    public SimplePalisadeService policyService(final PolicyService policyService) {
        requireNonNull(policyService, "The policy service cannot be set to null.");
        this.policyService = policyService;
        return this;
    }

    public SimplePalisadeService userService(final UserService userService) {
        requireNonNull(userService, "The user service cannot be set to null.");
        this.userService = userService;
        return this;
    }

    public SimplePalisadeService cacheService(final CacheService cacheService) {
        requireNonNull(cacheService, "The cache service cannot be set to null.");
        this.cacheService = cacheService;
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("auditService", auditService)
                .append("policyService", policyService)
                .append("userService", userService)
                .append("resourceService", resourceService)
                .append("cacheService", cacheService)
                .toString();
    }

    @Override
    public CompletableFuture<DataRequestResponse> registerDataRequest(final RegisterDataRequest request) {
        final RequestId originalRequestId = new RequestId().id(UUID.randomUUID().toString());
        LOGGER.debug("Registering data request: {}", request, originalRequestId);
        auditRequestReceived(request, originalRequestId);
        final GetUserRequest userRequest = new GetUserRequest().userId(request.getUserId());
        userRequest.setOriginalRequestId(originalRequestId);
        LOGGER.debug("Getting user from userService: {}", userRequest);
        final CompletableFuture<User> futureUser = userService.getUser(userRequest)
                .thenApply(user -> {
                    LOGGER.debug("Got user: {}", user);
                    return user;
                })
                .exceptionally(ex -> {
                    LOGGER.error("Failed to get user: {}", ex.getMessage());
                    if (nonNull(ex)) {
                        auditRequestReceivedException(request, originalRequestId, ex);
                    }
                    throw new RuntimeException(ex); //rethrow the exception
                });
        final GetResourcesByIdRequest resourceRequest = new GetResourcesByIdRequest().resourceId(request.getResourceId());
        resourceRequest.setOriginalRequestId(originalRequestId);
        LOGGER.debug("Getting resources from resourceService: {}", resourceRequest);
        final CompletableFuture<Map<LeafResource, ConnectionDetail>> futureResources = resourceService.getResourcesById(resourceRequest)
                .thenApply(resources -> {
                    LOGGER.debug("Got resources: {}", resources);
                    return resources;
                })
                .exceptionally(ex -> {
                    LOGGER.error("Failed to get resources: {}", ex.getMessage());
                    if (nonNull(ex)) {
                        auditRequestReceivedException(request, originalRequestId, ex);
                    }
                    throw new RuntimeException(ex); //rethrow the exception
                });
        final RequestId requestId = new RequestId().id(request.getUserId().getId() + "-" + UUID.randomUUID().toString());
        final DataRequestConfig config = new DataRequestConfig();
        config.setContext(request.getContext());
        config.setOriginalRequestId(originalRequestId);
        return CompletableFuture.allOf(futureUser, futureResources)
                .thenApply(t -> getPolicy(request, futureUser, futureResources, originalRequestId))
                .thenApply(multiPolicy -> ensureRecordRulesAvailableFor(multiPolicy, futureResources.join().keySet()))
                .thenAccept(multiPolicy -> {
                    auditProcessingStarted(request, futureUser.join(), multiPolicy, originalRequestId);
                    cache(request, futureUser.join(), requestId, multiPolicy, futureResources.join().size(), originalRequestId);
                })
                .thenApply(multiPolicy -> {
                    final DataRequestResponse response = new DataRequestResponse().requestId(requestId).originalRequestId(originalRequestId).resources(futureResources.join());
                    response.setOriginalRequestId(originalRequestId);
                    LOGGER.debug("Responding with: {}", response);
                    return response;
                })
                .exceptionally(ex -> {
                    LOGGER.error("Error handling: {}", ex.getMessage());
                    if (nonNull(ex)) {
                        auditRequestReceivedException(request, originalRequestId, ex);

                    }
                    throw new RuntimeException(ex); //rethrow the exception
                });
    }

    private MultiPolicy getPolicy(final RegisterDataRequest request, final CompletableFuture<User> futureUser, final CompletableFuture<Map<LeafResource, ConnectionDetail>> futureResources, final RequestId originalRequestId) {
        final GetPolicyRequest policyRequest = new GetPolicyRequest().user(futureUser.join()).context(request.getContext()).resources(new HashSet<>(futureResources.join().keySet()));
        policyRequest.setOriginalRequestId(originalRequestId);
        LOGGER.debug("Getting policy from policyService: {}", policyRequest);
        return policyService.getPolicy(policyRequest)
                .thenApply(policy -> {
                    LOGGER.debug("Got policy: {}", policy);
                    return policy;
                }).join();
    }

    private void auditProcessingStarted(final RegisterDataRequest request, final User user, final MultiPolicy multiPolicy, final RequestId originalRequestId) {
        for (final Entry<LeafResource, Policy> entry : multiPolicy.getPolicies().entrySet()) {
            final ProcessingStartedAuditRequest auditRequestProcessingStarted =
                    new ProcessingStartedAuditRequest();
            auditRequestProcessingStarted.resource(entry.getKey()).user(user)
                    .howItWasProcessed(entry.getValue().getMessage())
                    .context(request.getContext(), ProcessingStartedAuditRequest.class)
                    .userId(request.getUserId(), ProcessingStartedAuditRequest.class)
                    .resourceId(request.getResourceId(), ProcessingStartedAuditRequest.class)
                    .id(request.getId())
                    .originalRequestId(originalRequestId);
            LOGGER.debug("Auditing: {}", auditRequestProcessingStarted);
            auditService.audit(auditRequestProcessingStarted).join();
        }
    }

    private void auditRequestReceived(final RegisterDataRequest request, final RequestId originalRequestId) {
        final RequestReceivedAuditRequest requestReceivedAuditRequest = new RequestReceivedAuditRequest();
        requestReceivedAuditRequest.context(request.getContext(), RequestReceivedAuditRequest.class)
                .userId(request.getUserId(), RequestReceivedAuditRequest.class)
                .resourceId(request.getResourceId(), RequestReceivedAuditRequest.class)
                .id(request.getId())
                .originalRequestId(originalRequestId);
        auditService.audit(requestReceivedAuditRequest).join();
    }

    private void auditRequestReceivedException(final RegisterDataRequest request, final RequestId originalRequestId, final Throwable ex) {
        final ExceptionAuditRequest auditRequestWithException = new ExceptionAuditRequest();
        auditRequestWithException
                .exception(ex)
                .context(request.getContext(), ExceptionAuditRequest.class)
                .userId(request.getUserId(), ExceptionAuditRequest.class)
                .resourceId(request.getResourceId(), ExceptionAuditRequest.class)
                .id(request.getId())
                .originalRequestId(originalRequestId);
        LOGGER.debug("Error handling: " + ex.getMessage());
        auditService.audit(auditRequestWithException).join();
    }

    private void cache(final RegisterDataRequest request, final User user,
                       final RequestId requestId, final MultiPolicy multiPolicy,
                       final int resCount,
                       final RequestId originalRequestId) {
        DataRequestConfig dataRequestConfig = new DataRequestConfig()
                .user(user)
                .context(request.getContext())
                .rules(multiPolicy.getRuleMap());
        dataRequestConfig.setOriginalRequestId(originalRequestId);
        final AddCacheRequest<DataRequestConfig> cacheRequest = new AddCacheRequest<>()
                .key(requestId.getId())
                .value(dataRequestConfig)
                .service(this.getClass());
        LOGGER.debug("Caching: {}", cacheRequest);
        final Boolean success = cacheService.add(cacheRequest).join();
        if (null == success || !success) {
            throw new CompletionException(new RuntimeException("Failed to cache request: " + request));
        }
        //set a quick count of how many resources are needed for this request
        final AddCacheRequest<byte[]> resourceCountRequest = new AddCacheRequest<>()
                .key(RES_COUNT_KEY + requestId.getId() + "_" + resCount)
                .value(new byte[1])
                .timeToLive(Optional.of(COUNT_PERSIST_DURATION))
                .service(this.getClass());
        cacheService.add(resourceCountRequest).join();
    }

    @Override
    public CompletableFuture<DataRequestConfig> getDataRequestConfig(
            final GetDataRequestConfig request) {
        requireNonNull(request);
        requireNonNull(request.getRequestId());
        // TODO: need to validate that the user is actually requesting the correct info.
        // extract resources from request and check they are a subset of the original RegisterDataRequest resources
        final GetCacheRequest<DataRequestConfig> cacheRequest = new GetCacheRequest<>().key(request.getRequestId().getId()).service(this.getClass());
        LOGGER.debug("Getting cached data: {}", cacheRequest);
        return cacheService.get(cacheRequest)
                .thenApply(cache -> {
                    DataRequestConfig value = cache.orElseThrow(() -> createCacheException(request.getRequestId().getId()));
                    if (null == value.getUser()) {
                        throw createCacheException(request.getRequestId().getId());
                    }
                    LOGGER.debug("Got cache: {}", value);
                    return value;
                });
    }

    @Override
    public CompletableFuture<Map<String, String>> getMetrics(
            final GetMetricRequest request) {
        requireNonNull(request, "request");
        return CompletableFuture.completedFuture(new SimpleMetricProvider(getCacheService())
                .computeMetrics(request.getFilters()));
    }

    @Override
    public CompletableFuture<?> process(final Request request) {
        //first try one parent interface
        try {
            return PalisadeService.super.process(request);
        } catch (IllegalArgumentException e) {
            //that failed try the other
            return PalisadeMetricProvider.super.process(request);
        }
    }

    private RuntimeException createCacheException(final String id) {
        return new RuntimeException("User's request was not in the cache: " + id);
    }

    public AuditService getAuditService() {
        requireNonNull(auditService, "The audit service has not been set.");
        return auditService;
    }

    public void setAuditService(final AuditService auditService) {
        auditService(auditService);
    }

    public PolicyService getPolicyService() {
        requireNonNull(policyService, "The policy service has not been set.");
        return policyService;
    }

    public void setPolicyService(final PolicyService policyService) {
        policyService(policyService);
    }

    public UserService getUserService() {
        requireNonNull(userService, "The user service has not been set.");
        return userService;
    }

    public void setUserService(final UserService userService) {
        userService(userService);
    }

    public ResourceService getResourceService() {
        requireNonNull(resourceService, "The resource service has not been set.");
        return resourceService;
    }

    public void setResourceService(final ResourceService resourceService) {
        resourceService(resourceService);
    }

    public CacheService getCacheService() {
        requireNonNull(cacheService, "The cache service has not been set.");
        return cacheService;
    }

    public void setCacheService(final CacheService cacheService) {
        cacheService(cacheService);
    }
}
