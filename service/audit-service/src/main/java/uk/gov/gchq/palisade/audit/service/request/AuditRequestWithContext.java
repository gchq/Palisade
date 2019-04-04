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

public abstract class AuditRequestWithContext extends c {
    private Context context;
    private UserId userId;
    private String resourceId;

    /**
     * @return the auditLog representation of this class
     */
    @Override
    public String constructAuditLog() {
        return "" + " 'userId' " + getUserId().getId()
                + " 'purpose' " + getContext().getPurpose()
                + "' resourceId '" + getResourceId()
                + "' id '" + getId()
                + "' originalRequestId '" + getOriginalRequestId();
    }

    /**
     * @param context {@link Context} is the reason for the user accessing the resource
     * @param type    the derived return type to perform the cast upon
     * @param <T>     return type derived from AuditRequestWithContext
     * @return the {@link AuditRequestWithContext}
     */
    public <T extends AuditRequestWithContext> T context(final Context context, final Class<T> type) {
        requireNonNull(context, "The context cannot be set to null.");
        this.context = context;
        return type.cast(this);
    }


    /**
     * @param userId the userId associated with this auditRequest
     * @param type   the derived return type to perform the cast upon
     * @param <T>    return type derived from AuditRequestWithContext
     * @return the {@link AuditRequestWithContext}
     */
    public <T extends AuditRequestWithContext> T userId(final UserId userId, final Class<T> type) {
        requireNonNull(userId, "The userId cannot be set to null.");
        this.userId = userId;
        return type.cast(this);
    }

    /**
     * @param resourceId is the resourceId for the resource
     * @param type       the derived return type to perform the cast upon
     * @param <T>        return type derived from AuditRequestWithContext
     * @return the {@link AuditRequestWithContext}
     */
    public <T extends AuditRequestWithContext> T resourceId(final String resourceId, final Class<T> type) {
        requireNonNull(resourceId, "The resourceId cannot be set to null.");
        this.resourceId = resourceId;
        return type.cast(this);
    }

    public AuditRequestWithContext() {
    }

    public Context getContext() {
        requireNonNull(context, "The context has not been set.");
        return context;
    }

    public UserId getUserId() {
        requireNonNull(userId, "The userId has not been set.");
        return userId;
    }

    public String getResourceId() {
        requireNonNull(resourceId, "The resourceId has not been set.");
        return resourceId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final AuditRequestWithContext that = (AuditRequestWithContext) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(context, that.context)
                .append(userId, that.context)
                .append(resourceId, that.context)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(20, 39)
                .appendSuper(super.hashCode())
                .append(context)
                .append(userId)
                .append(resourceId)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("context", context)
                .append("userId", userId)
                .append("resouceId", resourceId)
                .toString();
    }
}
