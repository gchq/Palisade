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
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.audit.service.AuditService;
import uk.gov.gchq.palisade.audit.service.request.AuditRequest;
import uk.gov.gchq.palisade.cache.service.CacheService;
import uk.gov.gchq.palisade.cache.service.request.AddCacheDataRequest;
import uk.gov.gchq.palisade.cache.service.request.GetCacheDataRequest;
import uk.gov.gchq.palisade.policy.service.MultiPolicy;
import uk.gov.gchq.palisade.policy.service.Policy;
import uk.gov.gchq.palisade.policy.service.PolicyService;
import uk.gov.gchq.palisade.policy.service.request.GetPolicyRequest;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.service.ResourceService;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByIdRequest;
import uk.gov.gchq.palisade.service.PalisadeService;
import uk.gov.gchq.palisade.service.request.ConnectionDetail;
import uk.gov.gchq.palisade.service.request.DataRequestConfig;
import uk.gov.gchq.palisade.service.request.DataRequestResponse;
import uk.gov.gchq.palisade.service.request.GetDataRequestConfig;
import uk.gov.gchq.palisade.service.request.RegisterDataRequest;
import uk.gov.gchq.palisade.user.service.UserService;
import uk.gov.gchq.palisade.user.service.request.GetUserRequest;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static java.util.Objects.requireNonNull;

/**
 * <p> A simple implementation of a Palisade Service that just connects up the Audit, Cache, User, Policy and Resource
 * services. </p> <p> It currently doesn't validate that the user is actually requesting the correct resources. It
 * should check the resources requested in getDataRequestConfig are the same or a subset of the resources passed in in
 * registerDataRequest. </p>
 */
public class SimplePalisadeService implements PalisadeService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimplePalisadeService.class);

    private AuditService auditService;
    private PolicyService policyService;
    private UserService userService;
    private ResourceService resourceService;
    private CacheService cacheService;

    public SimplePalisadeService() {
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
    public CompletableFuture<DataRequestResponse> registerDataRequest(final RegisterDataRequest request) {
        LOGGER.debug("Registering data request: {}", request);

        final GetUserRequest userRequest = new GetUserRequest().userId(request.getUserId());
        LOGGER.debug("Getting user from userService: {}", userRequest);
        final CompletableFuture<User> futureUser = userService.getUser(userRequest)
                .thenApply(user -> {
                    LOGGER.debug("Got user: {}", user);
                    return user;
                });

        final GetResourcesByIdRequest resourceRequest = new GetResourcesByIdRequest().resourceId(request.getResourceId());
        LOGGER.debug("Getting resources from resourceService: {}", resourceRequest);
        final CompletableFuture<Map<LeafResource, ConnectionDetail>> futureResources = resourceService.getResourcesById(resourceRequest)
                .thenApply(resources -> {
                    LOGGER.debug("Got resources: {}", resources);
                    return resources;
                });

        final RequestId requestId = new RequestId().id(request.getUserId().getId() + "-" + UUID.randomUUID().toString());

        final DataRequestConfig config = new DataRequestConfig();
        config.setContext(request.getContext());

        return CompletableFuture.allOf(futureUser, futureResources)
                .thenApply(t -> getPolicy(request, futureUser, futureResources))
                .thenApply(multiPolicy -> ensureRecordRulesAvailableFor(multiPolicy, futureResources.join().keySet()))
                .thenAccept(multiPolicy -> {
                    audit(request, futureUser.join(), multiPolicy);
                    cache(request, futureUser.join(), requestId, multiPolicy);
                }).thenApply(t -> {
                    final DataRequestResponse response = new DataRequestResponse().requestId(requestId).resources(futureResources.join());
                    LOGGER.debug("Responding with: {}", response);
                    return response;
                });
    }

    private MultiPolicy getPolicy(final RegisterDataRequest request, final CompletableFuture<User> futureUser, final CompletableFuture<Map<LeafResource, ConnectionDetail>> futureResources) {
        final GetPolicyRequest policyRequest = new GetPolicyRequest().user(futureUser.join()).context(request.getContext()).resources(new HashSet<>(futureResources.join().keySet()));
        LOGGER.debug("Getting policy from policyService: {}", policyRequest);
        return policyService.getPolicy(policyRequest)
                .thenApply(policy -> {
                    LOGGER.debug("Got policy: {}", policy);
                    return policy;
                }).join();
    }

    private void audit(final RegisterDataRequest request, final User user, final MultiPolicy multiPolicy) {
        for (final Entry<LeafResource, Policy> entry : multiPolicy.getPolicies().entrySet()) {
            final AuditRequest auditRequest =
                    new AuditRequest()
                            .resource(entry.getKey())
                            .user(user)
                            .context(request.getContext())
                            .howItWasProcessed(entry.getValue().getMessage());
            LOGGER.debug("Auditing: {}", auditRequest);
            auditService.audit(auditRequest);
        }
    }

    private void cache(final RegisterDataRequest request, final User user, final RequestId requestId, final MultiPolicy multiPolicy) {
        final AddCacheDataRequest cacheRequest = new AddCacheDataRequest()
                .requestId(requestId)
                .dataRequestConfig(new DataRequestConfig()
                                .user(user)
                                .context(request.getContext())
                                .rules(multiPolicy.getRuleMap())
                );
        LOGGER.debug("Caching: {}", cacheRequest);
        final Boolean success = cacheService.add(cacheRequest).join();
        if (null == success || !success) {
            throw new CompletionException(new RuntimeException("Failed to cache request: " + request));
        }
    }

    @Override
    public CompletableFuture<DataRequestConfig> getDataRequestConfig(final GetDataRequestConfig request) {
        requireNonNull(request);
        requireNonNull(request.getRequestId());
        // TODO: need to validate that the user is actually requesting the correct info.
        // extract resources from request and check they are a subset of the original RegisterDataRequest resources
        final GetCacheDataRequest cacheRequest = new GetCacheDataRequest().requestId(request.getRequestId());
        LOGGER.debug("Getting cached data: {}", cacheRequest);
        return cacheService.get(cacheRequest)
                .thenApply(cache -> {
                    if (null == cache
                            || null == cache.getUser()) {
                        throw new RuntimeException("User's request was not in the cache: " + request.getRequestId().getId());
                    }
                    LOGGER.debug("Got cache: {}", cache);
                    return cache;
                });
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
