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

import uk.gov.gchq.palisade.policy.service.MultiPolicy;
import uk.gov.gchq.palisade.policy.service.Policy;
import uk.gov.gchq.palisade.policy.service.PolicyService;
import uk.gov.gchq.palisade.policy.service.request.CanAccessRequest;
import uk.gov.gchq.palisade.policy.service.request.GetPolicyRequest;
import uk.gov.gchq.palisade.policy.service.request.SetPolicyRequest;
import uk.gov.gchq.palisade.resource.Resource;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A HashMapPolicyService is a simple implementation of a {@link PolicyService}
 * that simply stores the policy rules in a {@link ConcurrentHashMap}.
 * By default the map is static so it will be shared across the same JVM.
 */
public class HashMapPolicyService implements PolicyService {
    private static final Logger LOGGER = LoggerFactory.getLogger(HashMapPolicyService.class);

    private static final MultiPolicy POLICY_STORE = new MultiPolicy(new ConcurrentHashMap<>());
    private final MultiPolicy policyStore;

    public HashMapPolicyService() {
        this(true);
    }

    public HashMapPolicyService(final boolean useStatic) {
        if (useStatic) {
            policyStore = POLICY_STORE;
        } else {
            policyStore = new MultiPolicy(new ConcurrentHashMap<>());
        }
    }

    @Override
    public CompletableFuture<Boolean> canAccess(final CanAccessRequest request) {
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<MultiPolicy> getPolicy(final GetPolicyRequest request) {
        MultiPolicy multiPolicy = new MultiPolicy();
        for (final Resource resource : request.getResources()) {
            final Policy policy = policyStore.getPolicy(resource);
            multiPolicy.setPolicy(resource, policy);
        }
        LOGGER.debug("Returning policy {}", multiPolicy);
        return CompletableFuture.completedFuture(multiPolicy);
    }

    @Override
    public CompletableFuture<Boolean> setPolicy(final SetPolicyRequest request) {
        LOGGER.debug("Setting policy {} for resource {}", request.getPolicy(), request.getResource());
        policyStore.getPolicies().remove(request.getResource());
        policyStore.setPolicy(request.getResource(), request.getPolicy());
        return CompletableFuture.completedFuture(true);
    }
}
