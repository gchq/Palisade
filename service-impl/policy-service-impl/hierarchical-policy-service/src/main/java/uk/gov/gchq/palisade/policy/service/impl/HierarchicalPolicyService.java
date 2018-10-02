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

package uk.gov.gchq.palisade.policy.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.Util;
import uk.gov.gchq.palisade.policy.service.MultiPolicy;
import uk.gov.gchq.palisade.policy.service.Policy;
import uk.gov.gchq.palisade.policy.service.PolicyService;
import uk.gov.gchq.palisade.policy.service.request.CanAccessRequest;
import uk.gov.gchq.palisade.policy.service.request.GetPolicyRequest;
import uk.gov.gchq.palisade.policy.service.request.SetResourcePolicyRequest;
import uk.gov.gchq.palisade.policy.service.request.SetTypePolicyRequest;
import uk.gov.gchq.palisade.policy.service.response.CanAccessResponse;
import uk.gov.gchq.palisade.resource.ChildResource;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.rule.Rules;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * By having the policies stored in several key value stores we can attach policies
 * at either the resource or data type level.
 * Each rule needs to be flagged as a resource level filter, or a record level filter/transform.
 * To get the rules for a file/stream resource, you need to get the rules for the given resource
 * followed by the rules of all its parents. Then you get the rules of the given resources data type.
 * If there are any negation rules then all rules inherited from up the
 * chain should be checked to see if any rules need removing due to the negation rule.
 */
public class HierarchicalPolicyService implements PolicyService {
    private static final Logger LOGGER = LoggerFactory.getLogger(HierarchicalPolicyService.class);

    private static final Map<String, Policy> DATA_TYPE_POLICIES_MAP = new ConcurrentHashMap<>();
    private static final Map<Resource, Policy> RESOURCE_POLICIES_MAP = new ConcurrentHashMap<>();

    private Map<String, Policy> dataTypePoliciesMap;
    private Map<Resource, Policy> resourcePoliciesMap;

    public HierarchicalPolicyService() {
        this(true);
    }

    public HierarchicalPolicyService(final boolean useStatic) {
        if (useStatic) {
            this.dataTypePoliciesMap = DATA_TYPE_POLICIES_MAP;
            this.resourcePoliciesMap = RESOURCE_POLICIES_MAP;
        } else {
            this.dataTypePoliciesMap = new ConcurrentHashMap<>();
            this.resourcePoliciesMap = new ConcurrentHashMap<>();
        }
    }

    public HierarchicalPolicyService(final Map<String, Policy> dataTypePoliciesMap, final Map<Resource, Policy> resourcePoliciesMap) {
        requireNonNull(dataTypePoliciesMap);
        requireNonNull(resourcePoliciesMap);
        this.dataTypePoliciesMap = dataTypePoliciesMap;
        this.resourcePoliciesMap = resourcePoliciesMap;
    }

    @Override
    public CompletableFuture<CanAccessResponse> canAccess(final CanAccessRequest request) {
        Context context = request.getContext();
        User user = request.getUser();
        Collection<LeafResource> resources = request.getResources();
        CanAccessResponse response = new CanAccessResponse().canAccessResources(canAccess(context, user, resources));
        return CompletableFuture.completedFuture(response);
    }

    private Collection<LeafResource> canAccess(final Context context, final User user, final Collection<LeafResource> resources) {
        return resources.stream()
                .map(resource -> {
                    Rules<LeafResource> rules = getApplicableRules(resource, true, resource.getType());
                    return Util.applyRulesToRecord(resource, user, context, rules);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * This method is used to recursively go up the resource hierarchy ending with the original
     * data type to extract and merge the policies at each stage of the hierarchy.
     *
     * @param resource         A {@link Resource} to get the applicable rules for.
     * @param canAccessRequest A boolean that is true if you want the resource level.
     *                         rules and therefore this is called from the canAccess method
     * @param originalDataType This is the data type that you want to be at the top of the
     *                         Resource hierarchy tree, which will be the data type of the
     *                         first resource in the recursive calls to this method.
     * @param <T>              The type of the returned {@link Rules}.
     * @return A {@link Rules} object of type T, which contains the list of rules
     * that need to be applied to the resource.
     */
    protected <T> Rules<T> getApplicableRules(final Resource resource, final boolean canAccessRequest, final String originalDataType) {

        Rules<T> inheritedRules;
        if (resource instanceof ChildResource) {
            inheritedRules = getApplicableRules(((ChildResource) resource).getParent(), canAccessRequest, originalDataType);
        } else {
            Policy inheritedPolicy = dataTypePoliciesMap.getOrDefault(originalDataType, new Policy());
            if (canAccessRequest) {
                inheritedRules = inheritedPolicy.getResourceRules();
            } else {
                inheritedRules = inheritedPolicy.getRecordRules();
            }
        }
        Policy newPolicy = resourcePoliciesMap.getOrDefault(resource, null);
        if (newPolicy == null) {
            return inheritedRules;
        } else {
            Rules<T> newRules;
            if (canAccessRequest) {
                newRules = newPolicy.getResourceRules();
            } else {
                newRules = newPolicy.getRecordRules();
            }
            return mergeRules(inheritedRules, newRules);
        }
    }

    private <T> Rules<T> mergeRules(final Rules<T> inheritedRules, final Rules<T> newRules) {
        String inheritedMessage = inheritedRules.getMessage();
        String newMessage = newRules.getMessage();
        if (!inheritedMessage.equals(Rules.NO_RULES_SET) && !newMessage.equals(Rules.NO_RULES_SET)) {
            inheritedRules.message(inheritedMessage + ", " + newMessage);
        } else if (!newMessage.equals(Rules.NO_RULES_SET)) {
            inheritedRules.message(newMessage);
        }
        return inheritedRules.addRules(newRules.getRules());
    }

    @Override
    public CompletableFuture<MultiPolicy> getPolicy(final GetPolicyRequest request) {
        Context context = request.getContext();
        User user = request.getUser();
        Collection<LeafResource> resources = request.getResources();
        Collection<LeafResource> canAccessResources = canAccess(context, user, resources);
        HashMap<LeafResource, Policy> map = new HashMap<>();
        canAccessResources.forEach(resource -> {
            Rules rules = getApplicableRules(resource, false, resource.getType());
            map.put(resource, new Policy<>().recordRules(rules));
        });
        return CompletableFuture.completedFuture(new MultiPolicy().policies(map));
    }

    @Override
    public CompletableFuture<Boolean> setResourcePolicy(final SetResourcePolicyRequest request) {
        requireNonNull(request);
        requireNonNull(request.getPolicy());
        requireNonNull(request.getResource());
        Resource resource = request.getResource();
        if (resource.getId() == null) {
            if (resource instanceof LeafResource) {
                dataTypePoliciesMap.put(((LeafResource) resource).getType(), request.getPolicy());
                LOGGER.debug("Set %s to data type %s", request.getPolicy(), ((LeafResource) resource).getType());
            } else {
                LOGGER.debug("The resource provided does not have the id or type field populated. Therefore the policy can not be added: %s", resource);
                final CompletableFuture<Boolean> future = new CompletableFuture<>();
                future.completeExceptionally(new IOException("The resource provided does not have the id or type field populated. Therefore the policy can not be added."));
                return future;
            }
        } else {
            resourcePoliciesMap.put(resource, request.getPolicy());
            LOGGER.debug("Set %s to %s", request.getPolicy(), resource);
        }
        return CompletableFuture.completedFuture(Boolean.TRUE);
    }

    @Override
    public CompletableFuture<Boolean> setTypePolicy(final SetTypePolicyRequest request) {
        requireNonNull(request);
        requireNonNull(request.getPolicy());
        final String type = request.getType();
        dataTypePoliciesMap.put(type, request.getPolicy());
        LOGGER.debug("Set %s to data type %s", request.getPolicy(), type);
        return CompletableFuture.completedFuture(Boolean.TRUE);
    }
}
