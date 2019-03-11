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

import uk.gov.gchq.palisade.rest.RestUtil;
import uk.gov.gchq.palisade.service.PalisadeMetricProvider;
import uk.gov.gchq.palisade.service.request.GetMetricRequest;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static java.util.Objects.requireNonNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@Api(value = "/")
public class RestPalisadeMetricProviderV1 implements PalisadeMetricProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestPalisadeMetricProviderV1.class);

    private final PalisadeMetricProvider delegate;

    private static PalisadeMetricProvider palisadeMetricProvider;

    @Inject
    public RestPalisadeMetricProviderV1(final PalisadeMetricProvider delegate) {
        requireNonNull(delegate, "delegate");
        this.delegate = delegate;
    }

    static synchronized PalisadeMetricProvider createService(final String serviceConfigPath) {
        if (palisadeMetricProvider == null) {
            palisadeMetricProvider = RestUtil.createService(RestPalisadeMetricProviderV1.class, serviceConfigPath, PalisadeMetricProvider.class);
        }
        return palisadeMetricProvider;
    }

    static synchronized void setDefaultDelegate(final PalisadeMetricProvider palisadeMetricProvider) {
        requireNonNull(palisadeMetricProvider, "palisadeMetricProvider");
        RestPalisadeMetricProviderV1.palisadeMetricProvider = palisadeMetricProvider;
    }

    @POST
    @Path("/getMetrics")
    @ApiOperation(value = "Gets some metrics about Palisade",
            response = Map.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Something went wrong in the server")
    })
    public Map<String, String> getMetricsSync(
            @ApiParam(value = "The request") final GetMetricRequest request) {
        LOGGER.debug("Invoking getMetrics: {}", request);
        return getMetrics(request).join();
    }

    @Override
    public CompletableFuture<Map<String, String>> getMetrics(
            @ApiParam(value = "The request") final GetMetricRequest request) {
        return delegate.getMetrics(request);
    }

    protected PalisadeMetricProvider getDelegate() {
        return delegate;
    }
}
