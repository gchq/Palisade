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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.policy.service.MultiPolicy;
import uk.gov.gchq.palisade.policy.service.PolicyService;
import uk.gov.gchq.palisade.policy.service.request.CanAccessRequest;
import uk.gov.gchq.palisade.policy.service.request.GetPolicyRequest;
import uk.gov.gchq.palisade.policy.service.request.SetResourcePolicyRequest;
import uk.gov.gchq.palisade.policy.service.request.SetTypePolicyRequest;
import uk.gov.gchq.palisade.policy.service.response.CanAccessResponse;
import uk.gov.gchq.palisade.rest.RestUtil;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import java.util.concurrent.CompletableFuture;

import static java.util.Objects.requireNonNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@Api(value = "/")
public class RestPolicyServiceV1 implements PolicyService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestPolicyServiceV1.class);
    private final PolicyService delegate;

    private static PolicyService policyService;

    public RestPolicyServiceV1() {
        this(System.getProperty(RestUtil.CONFIG_SERVICE_PATH));
    }

    public RestPolicyServiceV1(final String serviceConfigPath) {
        this(createService(serviceConfigPath));
    }

    public RestPolicyServiceV1(final PolicyService delegate) {
        this.delegate = delegate;
    }

    private static synchronized PolicyService createService(final String serviceConfigPath) {
        if (policyService == null) {
            policyService = RestUtil.createService(RestPolicyServiceV1.class, serviceConfigPath, PolicyService.class);
        }
        return policyService;
    }

    static synchronized void setDefaultDelegate(final PolicyService policyService) {
        requireNonNull(policyService, "policyService");
        RestPolicyServiceV1.policyService = policyService;
    }

    @POST
    @Path("/canAccess")
    @ApiOperation(value = "Returns a Boolean response",
            response = Boolean.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Something went wrong in the server")
    })
    public CanAccessResponse canAccessSync(
            @ApiParam(value = "The request") final CanAccessRequest request) {
        LOGGER.debug("Invoking canAccess: {}", request);
        return canAccess(request).join();
    }

    @Override
    public CompletableFuture<CanAccessResponse> canAccess(final CanAccessRequest request) {
        return delegate.canAccess(request);
    }

    @POST
    @ApiOperation(value = "Returns a MultiPolicy response",
            response = Boolean.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Something went wrong in the server")
    })
    public MultiPolicy getPolicySync(
            @ApiParam(value = "The request") final GetPolicyRequest request) {
        LOGGER.debug("Invoking getPolicy: {}", request);
        return getPolicy(request).join();
    }

    @Override
    public CompletableFuture<MultiPolicy> getPolicy(final GetPolicyRequest request) {
        return delegate.getPolicy(request);
    }

    @PUT
    @ApiOperation(value = "Sets the policy",
            response = Boolean.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Something went wrong in the server")
    })
    public Boolean setPolicySync(
            @ApiParam(value = "The request") final SetResourcePolicyRequest request) {
        return setResourcePolicy(request).join();
    }

    @PUT
    @Path("/setResourcePolicyAsync")
    @ApiOperation(value = "Sets the resource policy asynchronously",
            response = Boolean.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Something went wrong in the server")
    })
    public void setResourcePolicyAsync(
            @ApiParam(value = "The request") final SetResourcePolicyRequest request) {
        setResourcePolicy(request);
    }

    @Override
    public CompletableFuture<Boolean> setResourcePolicy(
            @ApiParam(value = "The request") final SetResourcePolicyRequest request) {
        LOGGER.debug("Invoking setResourcePolicy: {}", request);
        return delegate.setResourcePolicy(request);
    }

    @PUT
    @Path("/setTypePolicyAsync")
    @ApiOperation(value = "Sets the resource type policy asynchronously",
            response = Boolean.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Something went wrong in the server")
    })
    public void setTypePolicyAsync(
            @ApiParam(value = "The request") final SetTypePolicyRequest request) {
        setTypePolicy(request);
    }

    @Override
    public CompletableFuture<Boolean> setTypePolicy(
            @ApiParam(value = "The request") final SetTypePolicyRequest request) {
        LOGGER.debug("Invoking setTypePolicy: {}", request);
        return delegate.setTypePolicy(request);
    }

    protected PolicyService getDelegate() {
        return delegate;
    }
}
