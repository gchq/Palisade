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

package uk.gov.gchq.palisade.data.service.impl;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.glassfish.jersey.server.ChunkedOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.data.service.DataService;
import uk.gov.gchq.palisade.data.service.request.ReadRequest;
import uk.gov.gchq.palisade.data.service.request.ReadResponse;
import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.palisade.util.StreamUtil;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@Api(value = "/")
public class RestDataServiceV1 implements DataService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestDataServiceV1.class);

    public static final String SERVICE_CONFIG = "palisade.rest.data.service.config.path";
    private final DataService delegate;

    public RestDataServiceV1() {
        this(System.getProperty(SERVICE_CONFIG));
    }

    public RestDataServiceV1(final String serviceConfigPath) {
        this(createService(serviceConfigPath));
    }

    public RestDataServiceV1(final DataService delegate) {
        this.delegate = delegate;
    }

    private static DataService createService(final String serviceConfigPath) {
        final InputStream stream = StreamUtil.openStream(RestDataServiceV1.class, serviceConfigPath);
        return JSONSerialiser.deserialise(stream, DataService.class);
    }

    @POST
    @Path("/read")
    @ApiOperation(value = "Reads some data",
            response = ReadRequest.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Something went wrong in the server")
    })
    public ReadResponse readSync(final ReadRequest request) {
        LOGGER.debug("Invoking read: {}", request);
        return read(request).join();
    }

    @POST
    @Path("/read/chunked")
    @ApiOperation(value = "Reads some data and return it chunked",
            response = ReadRequest.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Something went wrong in the server")
    })
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.CLASS,
            include = As.WRAPPER_OBJECT,
            property = "class"
    )
    @SuppressFBWarnings
    public ChunkedOutput<byte[]> readSyncChunked(final ReadRequest request) {
        // Create chunked output instance
        final ChunkedOutput<byte[]> output = new ChunkedOutput<>(String.class, "\r\n");

        read(request).thenAccept(response -> {
            // write chunks to the chunked output object
            response.getData()
                    .map(JSONSerialiser::serialise)
                    .forEach((chunk) -> {
                        try {
                            output.write(chunk);
                        } catch (final IOException e) {
                            throw new RuntimeException("Unable to write chunk to output", e);
                        }
                    });
        });

        return output;
    }

    @Override
    public <T> CompletableFuture<ReadResponse<T>> read(final ReadRequest request) {
        return delegate.read(request);
    }

    protected DataService getDelegate() {
        return delegate;
    }
}
