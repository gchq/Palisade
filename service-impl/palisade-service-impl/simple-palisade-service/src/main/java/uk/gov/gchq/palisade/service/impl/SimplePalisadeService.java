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
import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.audit.service.AuditService;
import uk.gov.gchq.palisade.audit.service.NullAuditService;
import uk.gov.gchq.palisade.audit.service.request.AuditRequest;
import uk.gov.gchq.palisade.cache.service.CacheService;
import uk.gov.gchq.palisade.cache.service.NullCacheService;
import uk.gov.gchq.palisade.cache.service.request.AddCacheRequest;
import uk.gov.gchq.palisade.cache.service.request.GetCacheRequest;
import uk.gov.gchq.palisade.policy.service.MultiPolicy;
import uk.gov.gchq.palisade.policy.service.NullPolicyService;
import uk.gov.gchq.palisade.policy.service.Policy;
import uk.gov.gchq.palisade.policy.service.PolicyService;
import uk.gov.gchq.palisade.policy.service.request.GetPolicyRequest;
import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.resource.service.NullResourceService;
import uk.gov.gchq.palisade.resource.service.ResourceService;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByIdRequest;
import uk.gov.gchq.palisade.service.PalisadeService;
import uk.gov.gchq.palisade.service.request.ConnectionDetail;
import uk.gov.gchq.palisade.service.request.DataRequestConfig;
import uk.gov.gchq.palisade.service.request.DataRequestResponse;
import uk.gov.gchq.palisade.service.request.RegisterDataRequest;
import uk.gov.gchq.palisade.user.service.NullUserService;
import uk.gov.gchq.palisade.user.service.UserService;
import uk.gov.gchq.palisade.user.service.request.GetUserRequest;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * <p>
 * A simple implementation of a Palisade Service that just connects up the
 * Audit, Cache, User, Policy and Resource services.
 * </p>
 * <p>
 * It currently doesn't validate that the user is actually requesting the correct
 * resources. It should check the resources requested in getDataRequestConfig
 * are the same or a subset of the resources passed in in registerDataRequest.
 * </p>
 */
public class SimplePalisadeService implements PalisadeService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimplePalisadeService.class);

    private AuditService auditService;
    private PolicyService policyService;
    private UserService userService;
    private ResourceService resourceService;
    private CacheService cacheService;

    public SimplePalisadeService() {
        this(new NullResourceService(),
                new NullAuditService(),
                new NullPolicyService(),
                new NullUserService(),
                new NullCacheService());
    }

    public SimplePalisadeService(final ResourceService resourceService,
                                 final AuditService auditService,
                                 final PolicyService policyService,
                                 final UserService userService,
                                 final CacheService cacheService) {
        this.resourceService = resourceService;
        this.auditService = auditService;
        this.policyService = policyService;
        this.userService = userService;
        this.cacheService = cacheService;
    }

    @Override
    public CompletableFuture<DataRequestResponse> registerDataRequest(final RegisterDataRequest request) {
        LOGGER.debug("Registering data request: {}", request);

        final GetUserRequest userRequest = new GetUserRequest(request.getUserId());
        LOGGER.debug("Getting user from userService: {}", userRequest);
        final CompletableFuture<User> futureUser = userService.getUser(userRequest)
                .thenApply(user -> {
                    LOGGER.debug("Got user: {}", user);
                    return user;
                });

        final GetResourcesByIdRequest resourceRequest = new GetResourcesByIdRequest(request.getResource());
        LOGGER.debug("Getting resources from resourceService: {}", resourceRequest);
        final CompletableFuture<Map<Resource, ConnectionDetail>> futureResources = resourceService.getResourcesById(resourceRequest)
                .thenApply(resources -> {
                    LOGGER.debug("Got resources: {}", resources);
                    return resources;
                });

        final RequestId requestId = new RequestId(request.getUserId().getId() + "-" + UUID.randomUUID().toString());

        final DataRequestConfig config = new DataRequestConfig();
        config.setJustification(request.getJustification());

        return CompletableFuture.allOf(futureUser, futureResources)
                .thenApply(t -> getPolicy(request, futureUser, futureResources))
                .thenAccept(multiPolicy -> {
                    audit(request, futureUser.join(), multiPolicy);
                    cache(request, futureUser.join(), requestId, multiPolicy);
                }).thenApply(t -> {
                    final DataRequestResponse response = new DataRequestResponse(requestId, futureResources.join());
                    LOGGER.debug("Responding with: {}", response);
                    return response;
                });
    }

    private MultiPolicy getPolicy(final RegisterDataRequest request, final CompletableFuture<User> futureUser, final CompletableFuture<Map<Resource, ConnectionDetail>> futureResources) {
        final GetPolicyRequest policyRequest = new GetPolicyRequest(futureUser.join(), request.getJustification(), new HashSet<>(futureResources.join().keySet()));
        LOGGER.debug("Getting policy from policyService: {}", policyRequest);
        return policyService.getPolicy(policyRequest)
                .thenApply(policy -> {
                    LOGGER.debug("Got policy: {}", policy);
                    return policy;
                }).join();
    }

    private void audit(final RegisterDataRequest request, final User user, final MultiPolicy multiPolicy) {
        for (final Entry<Resource, Policy> entry : multiPolicy.getPolicies().entrySet()) {
            final AuditRequest auditRequest = new AuditRequest(
                    entry.getKey(),
                    user,
                    request.getJustification(),
                    entry.getValue().getMessage()
            );
            LOGGER.debug("Auditing: {}", auditRequest);
            auditService.audit(auditRequest);
        }
    }

    private void cache(final RegisterDataRequest request, final User user, final RequestId requestId, final MultiPolicy multiPolicy) {
        final AddCacheRequest cacheRequest = new AddCacheRequest(
                requestId,
                new DataRequestConfig(
                        user,
                        request.getJustification(),
                        multiPolicy.getRuleMap()
                )
        );
        LOGGER.debug("Caching: {}", cacheRequest);
        final Boolean success = cacheService.add(cacheRequest).join();
        if (null == success || !success) {
            throw new CompletionException(new RuntimeException("Failed to cache request: " + request));
        }
    }

    @Override
    public CompletableFuture<DataRequestConfig> getDataRequestConfig(final DataRequestResponse request) {
        Objects.requireNonNull(request);
        Objects.requireNonNull(request.getRequestId());
        // TODO: need to validate that the user is actually requesting the correct info.
        // extract resources from request and check they are a subset of the original RegisterDataRequest resources
        final GetCacheRequest cacheRequest = new GetCacheRequest(request.getRequestId());
        LOGGER.debug("Getting cached data: {}", cacheRequest);
        return cacheService.get(cacheRequest)
                .thenApply(cache -> {
                    if (null == cache
                            || null == cache.getUser()
                            || null == cache.getUser().getUserId()
                            || UserId.UNKNOWN_USER_ID.equals(cache.getUser().getUserId().getId())) {
                        throw new RuntimeException("User's request was not in the cache: " + request.getRequestId().getId());
                    }
                    LOGGER.debug("Got cache: {}", cache);
                    return cache;
                });
    }

    public AuditService getAuditService() {
        return auditService;
    }

    public void setAuditService(final AuditService auditService) {
        Objects.requireNonNull(auditService);
        this.auditService = auditService;
    }

    public PolicyService getPolicyService() {
        return policyService;
    }

    public void setPolicyService(final PolicyService policyService) {
        Objects.requireNonNull(policyService);
        this.policyService = policyService;
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(final UserService userService) {
        Objects.requireNonNull(userService);
        this.userService = userService;
    }

    public ResourceService getResourceService() {
        return resourceService;
    }

    public void setResourceService(final ResourceService resourceService) {
        Objects.requireNonNull(resourceService);
        this.resourceService = resourceService;
    }

    public CacheService getCacheService() {
        return cacheService;
    }

    public void setCacheService(final CacheService cacheService) {
        Objects.requireNonNull(cacheService);
        this.cacheService = cacheService;
    }
}
