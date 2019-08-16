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

import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.ToStringBuilder;
import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.service.Service;

import static java.util.Objects.requireNonNull;

/**
 * This is one of the objects that is passed to the {@link uk.gov.gchq.palisade.audit.service.AuditService}
 * to be able to store an audit record. This class extends {@link AuditRequest} This class
 * is used for the indication to the Audit logs that an exception has been received while processing the RegisterDataRequest
 * and which service it was that triggered the exception.
 */
public class RegisterRequestExceptionAuditRequest extends AuditRequest {

    private UserId userId;
    private String resourceId;
    private Context context;
    private Throwable exception;
    private Class<? extends Service> serviceClass;

    // no-arg constructor required
    public RegisterRequestExceptionAuditRequest() {
    }

    /**
     * @param userId {@link UserId} is the user id provided in the register request
     * @return the {@link RegisterRequestExceptionAuditRequest}
     */
    public RegisterRequestExceptionAuditRequest userId(final UserId userId) {
        requireNonNull(userId, "The userId cannot be null");
        this.userId = userId;
        return this;
    }

    /**
     * @param resourceId {@link String} is the resource id provided in the register request
     * @return the {@link RegisterRequestExceptionAuditRequest}
     */
    public RegisterRequestExceptionAuditRequest resourceId(final String resourceId) {
        requireNonNull(resourceId, "The resourceId cannot be null");
        this.resourceId = resourceId;
        return this;
    }

    /**
     * @param exception {@link Throwable} is the type of the exception while processing
     * @return the {@link RegisterRequestExceptionAuditRequest}
     */
    public RegisterRequestExceptionAuditRequest exception(final Throwable exception) {
        requireNonNull(exception, "The exception type cannot be null");
        this.exception = exception;
        return this;
    }

    /**
     * @param serviceClass {@link Class} is the palisade service that the exception was triggered by.
     * @return the {@link RegisterRequestExceptionAuditRequest}
     */
    public RegisterRequestExceptionAuditRequest service(final Class<? extends Service> serviceClass) {
        requireNonNull(serviceClass, "The serviceClass cannot be null");
        this.serviceClass = serviceClass;
        return this;
    }

    /**
     * @param context the context that was passed by the client to the palisade service
     * @return the {@link RegisterRequestExceptionAuditRequest}
     */
    public RegisterRequestExceptionAuditRequest context(final Context context) {
        requireNonNull(context, "The context cannot be set to null");
        this.context = context;
        return this;
    }

    public UserId getUserId() {
        requireNonNull(this.userId, "The user id has not been set");
        return userId;
    }

    public void setUserId(final UserId userId) {
        userId(userId);
    }

    public String getResourceId() {
        requireNonNull(this.resourceId, "The resource id has not been set");
        return resourceId;
    }

    public void setResourceId(final String resourceId) {
        resourceId(resourceId);
    }

    public Context getContext() {
        requireNonNull(this.context, "The context has not been set");
        return context;
    }

    public void setContext(final Context context) {
        context(context);
    }

    public Throwable getException() {
        requireNonNull(exception, "The exception type has not been set");
        return exception;
    }

    public void setException(final Throwable exception) {
        exception(exception);
    }

    public Class<? extends Service> getServiceClass() {
        requireNonNull(serviceClass, "The serviceClass has not been set");
        return serviceClass;
    }

    public void setServiceClass(final Class<? extends Service> service) {
        service(service);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final RegisterRequestExceptionAuditRequest that = (RegisterRequestExceptionAuditRequest) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(userId, that.userId)
                .append(resourceId, that.resourceId)
                .append(context, that.context)
                .append(exception, that.exception)
                .append(serviceClass, that.serviceClass)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(27, 41)
                .appendSuper(super.hashCode())
                .append(userId)
                .append(resourceId)
                .append(context)
                .append(exception)
                .append(serviceClass)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("userId", userId)
                .append("resourceId", resourceId)
                .append("context", context)
                .append("exception", exception)
                .append("serviceClass", serviceClass)
                .toString();
    }
}
