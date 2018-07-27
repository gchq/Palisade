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

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * By having the policies stored in several key value stores we can attach policies
 * at either the resource, data type.
 *
 * Each rule needs to be flagged as a resource level filter, or a record level filter/transform.
 *
 * To get the rules for a file/stream resource, you need to get the rules for the given resource
 * followed by the rules of all its parents. Then you get the rules of the given resources data type.
 * If there are any negation rules then all rules inherited from up the
 * chain should be checked to see if any rules need removing due to the negation rule.
 */
public class HierarchicalPolicyService implements PolicyService {
    private static final Logger LOGGER = LoggerFactory.getLogger(HierarchicalPolicyService.class);

    private HashMap<String, Policy> dataTypePoliciesMap;
    private HashMap<Resource, Policy> resourcePoliciesMap;

    @Override
    public CompletableFuture<Boolean> canAccess(final CanAccessRequest request) {
        return null;
    }

    @Override
    public CompletableFuture<MultiPolicy> getPolicy(final GetPolicyRequest request) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> setPolicy(final SetPolicyRequest request) {
        Objects.requireNonNull(request);
        Resource resource = request.getResource();
        if (resource.getId() == null) {
            if (resource.getType() != null) {
                dataTypePoliciesMap.put(resource.getType(), request.getPolicy());
                LOGGER.debug("Set %s to data type %s", request.getPolicy(), resource.getType());
            } else {
                LOGGER.debug("The resource provided does not have the id or type field populated. Therefore the policy can not be added: %s", resource);
//                return new IOException("The resource provided does not have the id or type field populated. Therefore the policy can not be added.");
                return CompletableFuture.completedFuture(Boolean.FALSE);
            }
        } else {
            resourcePoliciesMap.put(resource, request.getPolicy());
            LOGGER.debug("Set %s to %s", request.getPolicy(), resource);
        }
        return CompletableFuture.completedFuture(Boolean.TRUE);
    }
}
