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

import static java.util.Objects.requireNonNull;

/**
 * This is one of the objects that is passed to the {@link uk.gov.gchq.palisade.audit.service.AuditService}
 * to be able to store an audit record. This class extends {@link AuditRequest} This class
 * is used for the indication to the Audit logs that a RegisterDataRequest request has been received.
 */
public class RegisterRequestReceivedAuditRequest extends AuditRequest {

    private Context context;
    private UserId userId;
    private String resourceId;
    private String clientIp;
    private String clientHostname;

    // no-arg constructor required
    public RegisterRequestReceivedAuditRequest() {
    }

    /**
     * @param context the context that was passed by the client to the palisade service
     * @return the {@link RegisterRequestReceivedAuditRequest}
     */
    public RegisterRequestReceivedAuditRequest context(final Context context) {
        requireNonNull(context, "The context cannot be set to null");
        this.context = context;
        return this;
    }

    public Context getContext() {
        requireNonNull(this.context, "The context has not been set");
        return context;
    }

    public void setContext(final Context context) {
        context(context);
    }

    /**
     * @param userId the {@link UserId} of the user making the data request which was passed by the client to the palisade service
     * @return the {@link RegisterRequestReceivedAuditRequest}
     */
    public RegisterRequestReceivedAuditRequest userId(final UserId userId) {
        requireNonNull(userId, "The userId cannot be set to null");
        this.userId = userId;
        return this;
    }

    public UserId getUserId() {
        requireNonNull(this.context, "The userId has not been set");
        return userId;
    }

    public void setUserId(final UserId userId) {
        userId(userId);
    }

    /**
     * @param resourceId the pointer to a resource that was passed by the client to the palisade service
     * @return the {@link RegisterRequestReceivedAuditRequest}
     */
    public RegisterRequestReceivedAuditRequest resourceId(final String resourceId) {
        requireNonNull(resourceId, "The resourceId cannot be set to null");
        this.resourceId = resourceId;
        return this;
    }

    public String getResourceId() {
        requireNonNull(this.context, "The resourceId has not been set");
        return resourceId;
    }

    public void setResourceId(final String resourceId) {
        resourceId(resourceId);
    }

    /**
     * @param clientIp the IP address of the client machine that the user triggered the data access request from
     * @return the {@link RegisterRequestReceivedAuditRequest}
     */
    public RegisterRequestReceivedAuditRequest clientIp(final String clientIp) {
        requireNonNull(clientIp, "The clientIp cannot be set to null");
        this.clientIp = clientIp;
        return this;
    }

    public String getClientIp() {
        requireNonNull(this.clientIp, "The clientIp has not been set");
        return clientIp;
    }

    public void setClientIp(final String clientIp) {
        clientIp(clientIp);
    }

    /**
     * @param clientHostname the hostname of the client machine that the user triggered the data access request from
     * @return the {@link RegisterRequestReceivedAuditRequest}
     */
    public RegisterRequestReceivedAuditRequest clientHostname(final String clientHostname) {
        requireNonNull(clientHostname, "The clientHostname cannot be set to null");
        this.clientHostname = clientHostname;
        return this;
    }

    public String getClientHostname() {
        requireNonNull(this.clientHostname, "The clientHostname has not been set");
        return clientHostname;
    }

    public void setClientHostname(final String clientHostname) {
        clientHostname(clientHostname);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final RegisterRequestReceivedAuditRequest that = (RegisterRequestReceivedAuditRequest) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(context, that.context)
                .append(userId, that.userId)
                .append(resourceId, that.resourceId)
                .append(clientIp, that.clientIp)
                .append(clientHostname, that.clientHostname)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(21, 37)
                .appendSuper(super.hashCode())
                .append(context)
                .append(userId)
                .append(resourceId)
                .append(clientIp)
                .append(clientHostname)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("context", context)
                .append("userid", userId)
                .append("resourceId", resourceId)
                .append("clientIp", clientIp)
                .append("clientHostname", clientHostname)
                .toString();
    }
}
