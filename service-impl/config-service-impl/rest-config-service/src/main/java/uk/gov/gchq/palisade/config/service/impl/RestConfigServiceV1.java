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

import uk.gov.gchq.palisade.config.service.InitialConfigurationService;
import uk.gov.gchq.palisade.config.service.request.GetConfigRequest;
import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.palisade.service.request.InitialConfig;
import uk.gov.gchq.palisade.util.StreamUtil;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@Api(value = "/")
public class RestConfigServiceV1 implements InitialConfigurationService {
    public static final String SERVICE_CONFIG = "palisade.rest.config.service.config.path";
    private static final Logger LOGGER = LoggerFactory.getLogger(RestConfigServiceV1.class);

    private final InitialConfigurationService delegate;

    private static InitialConfigurationService configService;

    public RestConfigServiceV1() {
        this(System.getProperty(SERVICE_CONFIG));
    }

    public RestConfigServiceV1(final String serviceConfigPath) {
        this(createService(serviceConfigPath));
    }

    public RestConfigServiceV1(final InitialConfigurationService delegate) {
        this.delegate = delegate;
    }

    private static synchronized InitialConfigurationService createService(final String serviceConfigPath) {
        InitialConfigurationService ret;
        if (configService == null) {
            final InputStream stream = StreamUtil.openStream(RestConfigServiceV1.class, serviceConfigPath);
            configService = JSONSerialiser.deserialise(stream, InitialConfigurationService.class);
            ret = configService;
        } else {
            ret = configService;
        }
        return ret;
    }

    @POST
    @Path("/get")
    @ApiOperation(value = "Gets the config in a InitialConfig",
            response = InitialConfig.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Something went wrong in the server")
    })
    public InitialConfig getSync(
            @ApiParam(value = "The request") final GetConfigRequest request) {
        LOGGER.debug("Invoking get: {}", request);
        return get(request).join();
    }

    @Override
    public CompletableFuture<InitialConfig> get(final GetConfigRequest request) {
        return delegate.get(request);
    }

    protected InitialConfigurationService getDelegate() {
        return delegate;
    }
}
