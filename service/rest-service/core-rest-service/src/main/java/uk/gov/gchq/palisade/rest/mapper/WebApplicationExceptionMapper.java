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
package uk.gov.gchq.palisade.rest.mapper;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.exception.Error;
import uk.gov.gchq.palisade.exception.Error.ErrorBuilder;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import java.util.Arrays;
import java.util.stream.Collectors;

import static uk.gov.gchq.palisade.rest.ServiceConstants.PALISADE_MEDIA_TYPE;
import static uk.gov.gchq.palisade.rest.ServiceConstants.PALISADE_MEDIA_TYPE_HEADER;

/**
 * Jersey {@link javax.ws.rs.ext.ExceptionMapper} used to handle internal
 * {@link javax.ws.rs.WebApplicationException}s thrown by the Jersey framework.
 */
@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebApplicationExceptionMapper.class);

    @Override
    public Response toResponse(final WebApplicationException ex) {
        LOGGER.error("Error: {}", ex.getMessage(), ex);

        final Error error = new ErrorBuilder()
                .statusCode(ex.getResponse().getStatus())
                .simpleMessage(ex.getMessage())
                .detailMessage(Arrays.asList(ex.getStackTrace()).stream().map(StackTraceElement::toString).collect(Collectors.joining("\n")))
                .build();

        return Response.status(ex.getResponse().getStatus())
                .header(PALISADE_MEDIA_TYPE_HEADER, PALISADE_MEDIA_TYPE)
                .entity(error)
                .build();
    }
}
