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

package uk.gov.gchq.palisade.resource.service.impl;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.resource.service.ResourceService;
import uk.gov.gchq.palisade.resource.service.request.AddResourceRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByFormatRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByIdRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByResourceRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByTypeRequest;
import uk.gov.gchq.palisade.service.request.ConnectionDetail;
import uk.gov.gchq.palisade.service.request.DataRequestConfig;
import uk.gov.gchq.palisade.util.StreamUtil;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@Api(value = "/")
public class RestResourceServiceV1 implements ResourceService {
    public static final String SERVICE_CONFIG = "palisade.rest.resource.service.config.path";
    private static final Logger LOGGER = LoggerFactory.getLogger(RestResourceServiceV1.class);

    private final ResourceService delegate;

    public RestResourceServiceV1() {
        this(System.getProperty(SERVICE_CONFIG));
    }

    public RestResourceServiceV1(final String serviceConfigPath) {
        this(createService(serviceConfigPath));
    }

    public RestResourceServiceV1(final ResourceService delegate) {
        this.delegate = delegate;
    }

    private static ResourceService createService(final String serviceConfigPath) {
        final InputStream stream = StreamUtil.openStream(RestResourceServiceV1.class, serviceConfigPath);
        return JSONSerialiser.deserialise(stream, ResourceService.class);
    }

    @POST
    @Path("/getResourcesByResource")
    @ApiOperation(value = "Returns the resources",
            response = DataRequestConfig.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Something went wrong in the server")
    })
    public Map<Resource, ConnectionDetail> getResourcesByResourceSync(
            @ApiParam(value = "The request") final GetResourcesByResourceRequest request) {
        return getResourcesByResource(request).join();
    }

    @Override
    public CompletableFuture<Map<Resource, ConnectionDetail>> getResourcesByResource(
            final GetResourcesByResourceRequest request) {
        LOGGER.debug("Invoking getResourcesByResource: {}", request);
        return delegate.getResourcesByResource(request);
    }

    @POST
    @Path("/getResourcesById")
    @ApiOperation(value = "Returns the resources",
            response = DataRequestConfig.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Something went wrong in the server")
    })
    public Map<Resource, ConnectionDetail> getResourcesByIdSync(
            @ApiParam(value = "The request") final GetResourcesByIdRequest request) {
        return getResourcesById(request).join();
    }

    @Override
    public CompletableFuture<Map<Resource, ConnectionDetail>> getResourcesById(
            final GetResourcesByIdRequest request) {
        LOGGER.debug("Invoking getResourcesById: {}", request);
        return delegate.getResourcesById(request);
    }

    @POST
    @Path("/getResourcesByType")
    @ApiOperation(value = "Returns the resources",
            response = DataRequestConfig.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Something went wrong in the server")
    })
    public Map<Resource, ConnectionDetail> getResourceByTypeSync(
            @ApiParam(value = "The request") final GetResourcesByTypeRequest request) {
        return getResourcesByType(request).join();
    }

    @Override
    public CompletableFuture<Map<Resource, ConnectionDetail>> getResourcesByType(
            final GetResourcesByTypeRequest request) {
        LOGGER.debug("Invoking getResourcesByType: {}", request);
        return delegate.getResourcesByType(request);
    }

    @POST
    @Path("/getResourcesByFormat")
    @ApiOperation(value = "Returns the resources",
            response = DataRequestConfig.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Something went wrong in the server")
    })
    public Map<Resource, ConnectionDetail> getResourcesByFormatSync(
            @ApiParam(value = "The request") final GetResourcesByFormatRequest request) {
        return getResourcesByFormat(request).join();
    }

    @Override
    public CompletableFuture<Map<Resource, ConnectionDetail>> getResourcesByFormat(
            final GetResourcesByFormatRequest request) {
        LOGGER.debug("Invoking getResourcesByFormat: {}", request);
        return delegate.getResourcesByFormat(request);
    }

    @PUT
    @ApiOperation(value = "Add the resource",
            response = DataRequestConfig.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Something went wrong in the server")
    })
    public Boolean addResourceSync(
            @ApiParam(value = "The request") final AddResourceRequest request) {
        return addResource(request).join();
    }

    @PUT
    @Path("async")
    @ApiOperation(value = "Add the resource asynchronously",
            response = DataRequestConfig.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Something went wrong in the server")
    })
    public void addResourceAsync(
            @ApiParam(value = "The request") final AddResourceRequest request) {
        addResource(request);
    }

    @Override
    public CompletableFuture<Boolean> addResource(final AddResourceRequest request) {
        LOGGER.debug("Invoking addResource: {}", request);
        return delegate.addResource(request);
    }

    protected ResourceService getDelegate() {
        return delegate;
    }
}
