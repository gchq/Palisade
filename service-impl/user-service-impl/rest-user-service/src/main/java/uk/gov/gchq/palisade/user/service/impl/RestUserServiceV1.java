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

package uk.gov.gchq.palisade.user.service.impl;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.palisade.user.service.UserService;
import uk.gov.gchq.palisade.user.service.request.AddUserRequest;
import uk.gov.gchq.palisade.user.service.request.GetUserRequest;
import uk.gov.gchq.palisade.util.StreamUtil;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@Api(value = "/")
public class RestUserServiceV1 implements UserService {
    public static final String SERVICE_CONFIG = "palisade.rest.user.service.config.path";
    private static final Logger LOGGER = LoggerFactory.getLogger(RestUserServiceV1.class);

    private final UserService delegate;

    public RestUserServiceV1() {
        this(System.getProperty(SERVICE_CONFIG));
    }

    public RestUserServiceV1(final String serviceConfigPath) {
        this(createService(serviceConfigPath));
    }

    public RestUserServiceV1(final UserService delegate) {
        this.delegate = delegate;
    }

    private static UserService createService(final String serviceConfigPath) {
        final InputStream stream = StreamUtil.openStream(RestUserServiceV1.class, serviceConfigPath);
        return JSONSerialiser.deserialise(stream, UserService.class);
    }

    @POST
    @Path("/getUser")
    @ApiOperation(value = "Gets a user",
            response = User.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Something went wrong in the server")
    })
    public User getUserSync(final GetUserRequest request) {
        return getUser(request).join();
    }

    @Override
    public CompletableFuture<User> getUser(final GetUserRequest request) {
        LOGGER.debug("Invoking getUser: {}", request);
        return delegate.getUser(request);
    }

    @PUT
    @Path("/addUser")
    @ApiOperation(value = "Adds a user")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Something went wrong in the server")
    })

    public Boolean addUserSync(final AddUserRequest request) {
        return addUser(request).join();
    }

    @PUT
    @Path("/addUser/async")
    @ApiOperation(value = "Adds a user asynchronously")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Something went wrong in the server")
    })
    public void addUserAsync(final AddUserRequest request) {
        addUser(request);
    }

    @Override
    public CompletableFuture<Boolean> addUser(final AddUserRequest request) {
        LOGGER.debug("Invoking addUser: {}", request);
        return delegate.addUser(request);
    }

    protected UserService getDelegate() {
        return delegate;
    }
}
