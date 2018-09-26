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

package uk.gov.gchq.palisade.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.exception.Error.ErrorBuilder;

import java.util.Arrays;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

/**
 * Static utility class to standardise the instantiation of {@link uk.gov.gchq.palisade.exception.Error}
 * objects.
 */
public final class ErrorFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorFactory.class);

    /**
     * Empty, private constructor to prevent instantiation.
     */
    private ErrorFactory() {
        // Empty
    }

    /**
     * Create an {@link uk.gov.gchq.palisade.exception.Error} object from a
     * {@link uk.gov.gchq.palisade.exception.PalisadeCheckedException}.
     *
     * @param gex the exception object
     * @return a newly constructed {@link uk.gov.gchq.palisade.exception.Error}
     */
    public static Error from(final PalisadeCheckedException gex) {
        LOGGER.error("Error: {}", gex.getMessage(), gex);
        return new ErrorBuilder()
                .status(gex.getStatus())
                .simpleMessage(gex.getMessage())
                .detailMessage(Arrays.asList(gex.getStackTrace()).stream().map(StackTraceElement::toString).collect(Collectors.joining("\n")))
                .exceptionClass(gex)
                .build();
    }

    /**
     * Create an {@link uk.gov.gchq.palisade.exception.Error} object from a
     * {@link uk.gov.gchq.palisade.exception.PalisadeRuntimeException}.
     *
     * @param gex the exception object
     * @return a newly constructed {@link uk.gov.gchq.palisade.exception.Error}
     */
    public static Error from(final PalisadeRuntimeException gex) {
        LOGGER.error("Error: {}", gex.getMessage(), gex);
        return new ErrorBuilder()
                .status(gex.getStatus())
                .simpleMessage(gex.getMessage())
                .detailMessage(Arrays.asList(gex.getStackTrace()).stream().map(StackTraceElement::toString).collect(Collectors.joining("\n")))
                .exceptionClass(gex)
                .build();
    }

    /**
     * Create an {@link uk.gov.gchq.palisade.exception.Error} object from a
     * {@link uk.gov.gchq.palisade.exception.PalisadeWrappedErrorRuntimeException}.
     *
     * @param gex the exception object
     * @return the error from within the exception
     */
    public static Error from(final PalisadeWrappedErrorRuntimeException gex) {
        LOGGER.error("Error: {}", gex.getError().getSimpleMessage(), gex);
        return gex.getError();
    }

    /**
     * Create an {@link uk.gov.gchq.palisade.exception.Error} object from an
     * {@link Exception}.
     *
     * @param ex the exception object
     * @return a newly constructed {@link uk.gov.gchq.palisade.exception.Error}
     */
    public static Error from(final Exception ex) {
        final Exception unwrappedEx = unwrapCompletionException(ex);
        LOGGER.error("Error: {}", ex.getMessage(), ex);
        return new ErrorBuilder()
                .status(Status.INTERNAL_SERVER_ERROR)
                .simpleMessage(unwrappedEx.getMessage())
                .detailMessage(Arrays.asList(unwrappedEx.getStackTrace()).stream().map(StackTraceElement::toString).collect(Collectors.joining("\n")))
                .exceptionClass(unwrappedEx)
                .build();
    }

    private static Exception unwrapCompletionException(final Exception ex) {
        Exception unwrappedEx = ex;
        while (unwrappedEx instanceof CompletionException && unwrappedEx.getCause() instanceof Exception) {
            unwrappedEx = (Exception) ex.getCause();
        }

        return unwrappedEx;
    }
}
