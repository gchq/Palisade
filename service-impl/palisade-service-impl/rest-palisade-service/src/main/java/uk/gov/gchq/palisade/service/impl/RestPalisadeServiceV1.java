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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.config.service.ConfigUtils;
import uk.gov.gchq.palisade.service.PalisadeService;
import uk.gov.gchq.palisade.service.request.DataRequestConfig;
import uk.gov.gchq.palisade.service.request.DataRequestResponse;
import uk.gov.gchq.palisade.service.request.GetDataRequestConfig;
import uk.gov.gchq.palisade.service.request.RegisterDataRequest;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import java.util.concurrent.CompletableFuture;

import static java.util.Objects.requireNonNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@Api(value = "/")
public class RestPalisadeServiceV1 implements PalisadeService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestPalisadeServiceV1.class);

    private final PalisadeService delegate;

    private static PalisadeService palisadeService;

    @Inject
    public RestPalisadeServiceV1(final PalisadeService delegate) {
        requireNonNull(delegate, "delegate");
        this.delegate = delegate;
    }

    static synchronized PalisadeService createService(final String serviceConfigPath) {
        if (palisadeService == null) {
            palisadeService = ConfigUtils.createService(RestPalisadeServiceV1.class, serviceConfigPath, PalisadeService.class);
        }
        return palisadeService;
    }

    static synchronized void setDefaultDelegate(final PalisadeService palisadeService) {
        requireNonNull(palisadeService, "palisadeService");
        RestPalisadeServiceV1.palisadeService = palisadeService;
    }

    @POST
    @Path("/registerDataRequest")
    @ApiOperation(value = "Returns a DataRequestResponse response",
            response = DataRequestResponse.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Something went wrong in the server")
    })
    public DataRequestResponse registerDataRequestSync(
            @ApiParam(value = "The request") final RegisterDataRequest request) {
        LOGGER.debug("Invoking registerDataRequest: {}", request);
        return registerDataRequest(request).join();
    }

    @Override
    public CompletableFuture<DataRequestResponse> registerDataRequest(
            @ApiParam(value = "The request") final RegisterDataRequest request) {
        return delegate.registerDataRequest(request);
    }

    @POST
    @Path("/getDataRequestConfig")
    @ApiOperation(value = "Returns a DataRequestConfig response",
            response = DataRequestConfig.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Something went wrong in the server")
    })
    public DataRequestConfig getDataRequestConfigSync(
            @ApiParam(value = "The request") final GetDataRequestConfig request) {
        LOGGER.debug("Invoking getDataRequestConfig: {}", request);
        return getDataRequestConfig(request).join();
    }

    @Override
    public CompletableFuture<DataRequestConfig> getDataRequestConfig(
            @ApiParam(value = "The request") final GetDataRequestConfig request) {
        return delegate.getDataRequestConfig(request);
    }

    protected PalisadeService getDelegate() {
        return delegate;
    }
}
