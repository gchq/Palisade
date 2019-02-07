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

package uk.gov.gchq.palisade.config.service.impl;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.config.service.ConfigurationService;
import uk.gov.gchq.palisade.config.service.request.AddConfigRequest;
import uk.gov.gchq.palisade.config.service.request.GetConfigRequest;
import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.palisade.service.request.ServiceConfiguration;
import uk.gov.gchq.palisade.util.StreamUtil;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

import static java.util.Objects.requireNonNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@Api(value = "/")
public class RestConfigServiceV1 implements ConfigurationService {
    public static final String BOOTSTRAP_CONFIG = "palisade.rest.bootstrap.path";
    private static final Logger LOGGER = LoggerFactory.getLogger(RestConfigServiceV1.class);

    private final ConfigurationService delegate;

    private static ConfigurationService configService;

    @Inject
    public RestConfigServiceV1(final ConfigurationService delegate) {
        requireNonNull(delegate, "delegate");
        this.delegate = delegate;
    }

    static synchronized ConfigurationService createService(final String serviceConfigPath) {
        if (configService == null) {
            //create the configuration service from the initial bootstrap information
            final InputStream stream = StreamUtil.openStream(RestConfigServiceV1.class, serviceConfigPath);
            configService = JSONSerialiser.deserialise(stream, ConfigurationService.class);
            configService.configureSelfFromConfig();
        }
        return configService;
    }

    @POST
    @Path("/get")
    @ApiOperation(value = "Gets the config in a ServiceConfiguration",
            response = ServiceConfiguration.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Something went wrong in the server")
    })
    public ServiceConfiguration getSync(
            @ApiParam(value = "The request") final GetConfigRequest request) {
        LOGGER.debug("Invoking get: {}", request);
        return get(request).join();
    }

    @PUT
    @Path("/add")
    @ApiOperation(value = "Adds the config from this ServiceConfiguration",
            response = Boolean.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Something went wrong in the server")
    })
    public Boolean addSync(
            @ApiParam(value = "The request") final AddConfigRequest request) {
        LOGGER.debug("Invoking add: {}", request);
        return add(request).join();
    }

    @Override
    public CompletableFuture<ServiceConfiguration> get(final GetConfigRequest request) {
        return delegate.get(request);
    }

    @Override
    public CompletableFuture<Boolean> add(final AddConfigRequest request) {
        return delegate.add(request);
    }

    protected ConfigurationService getDelegate() {
        return delegate;
    }
}
