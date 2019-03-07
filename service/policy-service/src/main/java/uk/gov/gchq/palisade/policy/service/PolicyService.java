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

package uk.gov.gchq.palisade.policy.service;

import uk.gov.gchq.palisade.policy.service.request.CanAccessRequest;
import uk.gov.gchq.palisade.policy.service.request.GetPolicyRequest;
import uk.gov.gchq.palisade.policy.service.request.SetResourcePolicyRequest;
import uk.gov.gchq.palisade.policy.service.request.SetTypePolicyRequest;
import uk.gov.gchq.palisade.policy.service.response.CanAccessResponse;
import uk.gov.gchq.palisade.service.Service;
import uk.gov.gchq.palisade.service.request.Request;

import java.util.concurrent.CompletableFuture;

/**
 * The core API for the policy service.
 * The responsibilities of the policy service is to provide the set of rules
 * (filters or transformations) that need to be applied to each resource that
 * has been requested, based the user and purpose.
 * Note that a resource could be a file, stream, directory or even the system
 * resource (policies added to the system resource would be applied globally).
 */
public interface PolicyService extends Service {
    /**
     * This method is used to find out if the given user is allowed to access
     * the resource given their purpose. This is where any resource level
     * access controls are enforced.
     *
     * @param request a {@link CanAccessRequest} containing the user requesting
     *                the data, the query time context containing environmental
     *                variables such as why they want the data and
     *                collection of resource's containing that data.
     * @return a {@link CanAccessResponse} which contains a collection of the
     * resources that the user is allowed access too.
     */
    // TODO: should this return bitmap  = READ, WRITE,EXECUTE ?
    CompletableFuture<CanAccessResponse> canAccess(final CanAccessRequest request);

    /**
     * This method gets the record level {@link Policy}'s that apply to the list
     * of resources that the user has requested access too.
     *
     * @param request a {@link GetPolicyRequest} containing the user requesting
          *                the data, the query time context containing environmental
          *                variables such as why they want the data and
     *                list of the resources the user wants access too.
     * @return a {@link MultiPolicy} containing the mapping of resource to {@link Policy}
     */
    CompletableFuture<MultiPolicy> getPolicy(final GetPolicyRequest request);

    /**
     * This method allows for the setting of a policy to a resource.
     *
     * @param request a {@link SetResourcePolicyRequest} containing the
     *                resource and the policy to set on that resource.
     * @return a {@link CompletableFuture} {@link Boolean} which is true if
     * the policy was successfully set.
     */
    CompletableFuture<Boolean> setResourcePolicy(final SetResourcePolicyRequest request);

    /**
     * This method allows for the setting of a policy to a resource type.
     *
     * @param request a {@link SetTypePolicyRequest} containing the
     *                resource type and the policy to set on that resource.
     * @return a {@link CompletableFuture} {@link Boolean} which is true if
     * the policy was successfully set.
     */
    CompletableFuture<Boolean> setTypePolicy(final SetTypePolicyRequest request);

    @Override
    default CompletableFuture<?> process(final Request request) {
        if (request instanceof CanAccessRequest) {
            return canAccess((CanAccessRequest) request);
        }
        if (request instanceof GetPolicyRequest) {
            return getPolicy((GetPolicyRequest) request);
        }
        if (request instanceof SetResourcePolicyRequest) {
            return setResourcePolicy((SetResourcePolicyRequest) request);
        }
        if (request instanceof SetTypePolicyRequest) {
            return setTypePolicy((SetTypePolicyRequest) request);
        }
        return Service.super.process(request);
    }
}
