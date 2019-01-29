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


import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.ToStringBuilder;
import uk.gov.gchq.palisade.util.DebugUtil;

import java.lang.reflect.Constructor;

import static java.util.Objects.requireNonNull;

/**
 * Simple serialisable POJO for containing details of errors.
 * An {@link uk.gov.gchq.palisade.exception.Error} object is typically
 * created automatically by a Jersey ExceptionMapper and should not be created
 * manually.
 */
public final class Error {
    private static final Logger LOGGER = LoggerFactory.getLogger(Error.class);

    private int statusCode;
    private Status status;
    private String simpleMessage;
    private String detailMessage;
    private Class<? extends RuntimeException> exceptionClass;

    public Error() {
    }

    private Error(final ErrorBuilder builder) {
        this.statusCode = builder.statusCode;
        this.status = builder.status;
        this.simpleMessage = builder.simpleMessage;
        this.detailMessage = builder.detailMessage;
        this.exceptionClass = builder.exceptionClass;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Status getStatus() {
        return status;
    }

    public String getSimpleMessage() {
        return simpleMessage;
    }

    public String getDetailMessage() {
        return detailMessage;
    }

    public void setStatusCode(final int statusCode) {
        this.statusCode = statusCode;
    }

    public void setStatus(final Status status) {
        this.status = status;
    }

    public void setSimpleMessage(final String simpleMessage) {
        this.simpleMessage = simpleMessage;
    }

    public void setDetailMessage(final String detailMessage) {
        this.detailMessage = detailMessage;
    }

    public Class<? extends RuntimeException> getExceptionClass() {
        return exceptionClass;
    }

    public void setExceptionClass(final Class<? extends RuntimeException> exceptionClass) {
        this.exceptionClass = exceptionClass;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Error error = (Error) o;

        return new EqualsBuilder()
                .append(statusCode, error.statusCode)
                .append(status, error.status)
                .append(simpleMessage, error.simpleMessage)
                .append(detailMessage, error.detailMessage)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 11)
                .append(statusCode)
                .append(status)
                .append(simpleMessage)
                .append(detailMessage)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("statusCode", statusCode)
                .append("status", status)
                .append("simpleMessage", simpleMessage)
                .append("detailMessage", detailMessage)
                .toString();
    }

    public RuntimeException createException() {
        if (null == exceptionClass) {
            return new PalisadeWrappedErrorRuntimeException(this);
        }

        if (null != simpleMessage) {
            try {
                final Constructor<? extends RuntimeException> constructor = exceptionClass.getConstructor(String.class);
                return constructor.newInstance(simpleMessage);
            } catch (final Exception e) {
                LOGGER.error("Unable to recreate exception with message for error {}", this, e);
            }
        }

        try {
            return exceptionClass.getDeclaredConstructor().newInstance();
        } catch (final Exception e) {
            // ignore
        }

        return new PalisadeWrappedErrorRuntimeException(this);

    }


    public static final class ErrorBuilder {
        private int statusCode;
        private Status status;
        private String simpleMessage;
        private String detailMessage;
        private Class<? extends RuntimeException> exceptionClass;

        public ErrorBuilder() {
            // Empty
        }

        public ErrorBuilder statusCode(final int statusCode) {
            this.statusCode = statusCode;
            this.status = Status.fromStatusCode(statusCode);
            return this;
        }

        public ErrorBuilder status(final Status status) {
            this.status = status;
            this.statusCode = status.getStatusCode();
            return this;
        }

        public ErrorBuilder simpleMessage(final String simpleMessage) {
            this.simpleMessage = simpleMessage;
            return this;
        }

        public ErrorBuilder detailMessage(final String detailMessage) {
            this.detailMessage = detailMessage;
            return this;
        }

        public ErrorBuilder exceptionClass(final RuntimeException exception) {
            requireNonNull(exception, "exception is required");
            this.exceptionClass = exception.getClass();
            return this;
        }

        public ErrorBuilder exceptionClass(final Exception exception) {
            requireNonNull(exception, "exception is required");
            if (exception instanceof RuntimeException) {
                this.exceptionClass = exception.getClass().asSubclass(RuntimeException.class);
            } else {
                this.exceptionClass = PalisadeRuntimeException.class;
            }
            return this;
        }

        public ErrorBuilder exceptionClass(final Class<? extends Exception> exceptionClass) {
            requireNonNull(exceptionClass, "exceptionClass is required");
            if (RuntimeException.class.isAssignableFrom(exceptionClass)) {
                this.exceptionClass = exceptionClass.asSubclass(RuntimeException.class);
            } else {
                this.exceptionClass = PalisadeRuntimeException.class;
            }
            return this;
        }

        public Error build() {
            return new Error(DebugUtil.checkDebugMode() ? this : this.detailMessage(null));
        }
    }
}
