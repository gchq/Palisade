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

package uk.gov.gchq.palisade.service;

import uk.gov.gchq.palisade.policy.service.MultiPolicy;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.rule.Rules;
import uk.gov.gchq.palisade.service.exception.NoPolicyException;
import uk.gov.gchq.palisade.service.request.DataRequestConfig;
import uk.gov.gchq.palisade.service.request.DataRequestResponse;
import uk.gov.gchq.palisade.service.request.GetDataRequestConfig;
import uk.gov.gchq.palisade.service.request.RegisterDataRequest;
import uk.gov.gchq.palisade.service.request.Request;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * The core API for the palisade service. The responsibility of the palisade service is to send off the required
 * auditing records and collate all the relevant information about a request for data (using the other services) and to
 * provide the Data service with the information it requires to enforce the policy and apply any user filters. In order
 * to have multiple Palisade services, the data that the data server will require, needs to be stored in a shared cache
 * (Cache service).
 */
public interface PalisadeService extends Service {

    String TOKEN_NOT_FOUND_MESSAGE = "User's request was not in the cache: ";

    /**
     * This method is used by the client code to register that they want to read a resource or data set. This method
     * will check that the user can have access to the resource and pass back details of all the resources linked to the
     * initial request (if they asked for a data set) and how to connect to the relevant data service to get that data.
     * Then behind the scenes this will have triggered more services to be called to collate the data required by the
     * getDataRequestConfig() method so the data service can apply the relevant policies.
     *
     * @param request a {@link RegisterDataRequest} that contains the details the client needs to provide the palisade
     *                service for it to register the data request
     * @return details of all the resources linked to the initial request (if they asked for a data set) and how to
     * connect to the relevant data service to get that data.
     */
    CompletableFuture<DataRequestResponse> registerDataRequest(final RegisterDataRequest request);

    /**
     * This method is used by the data service's to request the trusted details that it requires to apply the necessary
     * data access controls.
     *
     * @param request This is the {@link GetDataRequestConfig} that the client passed to the data service.
     * @return a {@link DataRequestConfig} containing the information that the data service requires to apply the
     * necessary filtering/transformations to the data.
     */
    CompletableFuture<DataRequestConfig> getDataRequestConfig(final GetDataRequestConfig request);

    // TODO handling adding resources?

    @Override
    default CompletableFuture<?> process(final Request request) {
        if (request instanceof RegisterDataRequest) {
            return registerDataRequest((RegisterDataRequest) request);
        }
        return Service.super.process(request);
    }

    /**
     * Checks that each {@link LeafResource} in a request has record level policy associated with it in the {@link
     * MultiPolicy}.
     *
     * @param policy    policy being applied to a request
     * @param resources the list of resources being processed
     * @return the {@code} policy object
     * @throws NoPolicyException if no record level rules are available for any {@link LeafResource} in {@code resources}
     */
    static MultiPolicy ensureRecordRulesAvailableFor(final MultiPolicy policy, final Collection<LeafResource> resources) {
        Objects.requireNonNull(policy, "policy");
        Objects.requireNonNull(resources, "resources");
        final Map<LeafResource, Rules> ruleMap = policy.getRuleMap();
        resources.forEach(resource -> {
            if (!ruleMap.containsKey(resource)) {
                throw new NoPolicyException("No policy record rules available for " + resource);
            }
            if (!ruleMap.get(resource).containsRules()) {
                //policy available but is empty
                //TODO: audit this because it is unusual behaviour
            }
        });
        return policy;
    }
}
