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
import uk.gov.gchq.palisade.policy.service.request.SetPolicyRequest;
import uk.gov.gchq.palisade.service.Service;
import uk.gov.gchq.palisade.service.request.Request;

import java.util.concurrent.CompletableFuture;

/**
 * The core API for the policy service.
 *
 * The responsibilities of the policy service is to provide the set of rules
 * (filters or transformations) that need to be applied to each resource that
 * has been requested, based the user and justification.
 *
 * Note that a resource could be a file, stream, directory or even the system
 * resource (policies added to the system resource would be applied globally).
 */
public interface PolicyService extends Service {
    /**
     * This method is used to find out if the given user is allowed to access
     * the resource given their justification. This is where any resource level
     * access controls are enforced.
     *
     * @param request a {@link CanAccessRequest} containing the user requesting
     *                the data, the justification of why they want the data and
     *                resource containing that data.
     * @return a {@link CompletableFuture} {@link Boolean} which is true if
     * they are allowed access to the resource.
     */
    // TODO: should this return bitmap  = READ, WRITE,EXECUTE ?
    // TODO could this work on a list of resources the same way as the getPolicy method does?
    CompletableFuture<Boolean> canAccess(final CanAccessRequest request);

    /**
     * This method gets the record level {@link Policy}'s that apply to the list
     * of resources that the user has requested access too.
     *
     * @param request a {@link GetPolicyRequest} containing the user requesting
     *                the data, a justification for why they want the data and a
     *                list of the resources the user wants access too.
     * @return a {@link MultiPolicy} containing the mapping of resource to {@link Policy}
     */
    CompletableFuture<MultiPolicy> getPolicy(final GetPolicyRequest request);

    /**
     * This method allows for the setting of a policy to a resource.
     *
     * @param request a {@link SetPolicyRequest} containing the
     *                         resource and the policy to set on that resource.
     * @return a {@link CompletableFuture} {@link Boolean} which is true if
     * the policy was sucessfully set.
     */
    CompletableFuture<Boolean> setPolicy(final SetPolicyRequest request);

    @Override
    default CompletableFuture<?> process(final Request request) {
        if (request instanceof CanAccessRequest) {
            return canAccess((CanAccessRequest) request);
        }
        if (request instanceof GetPolicyRequest) {
            return getPolicy((GetPolicyRequest) request);
        }
        if (request instanceof SetPolicyRequest) {
            setPolicy((SetPolicyRequest) request);
            return null;
        }
        return Service.super.process(request);
    }
}
