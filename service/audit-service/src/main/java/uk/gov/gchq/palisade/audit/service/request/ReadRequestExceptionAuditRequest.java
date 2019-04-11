/*
 * Copyright 2019 Crown Copyright
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
package uk.gov.gchq.palisade.audit.service.request;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import uk.gov.gchq.palisade.ToStringBuilder;
import uk.gov.gchq.palisade.service.request.Request;

import static java.util.Objects.requireNonNull;

/**
 * This is one of the objects that is passed to the {@link uk.gov.gchq.palisade.audit.service.AuditService}
 * to be able to store an audit record. This class extends {@link Request} This class
 * is used for the indication to the Audit logs that an exception has been received.
 */
public class ReadRequestExceptionAuditRequest extends AuditRequest {
    private Throwable exception;

    public ReadRequestExceptionAuditRequest() {
    }

    /**
     * @param ex {@link Throwable} is the type of the exception while processing
     * @return the {@link ReadRequestExceptionAuditRequest}
     */
    public ReadRequestExceptionAuditRequest exception(final Throwable ex) {
        requireNonNull(exception, "The exception type cannot be null");
        this.exception = exception;
        return this;
    }


    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ReadRequestExceptionAuditRequest that = (ReadRequestExceptionAuditRequest) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(exception, that.exception)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(20, 39)
                .appendSuper(super.hashCode())
                .append(exception)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("exception", exception)
                .toString();
    }

    public Throwable getException() {
        requireNonNull(exception, "The exception type cannot be null");
        return exception;
    }
}
