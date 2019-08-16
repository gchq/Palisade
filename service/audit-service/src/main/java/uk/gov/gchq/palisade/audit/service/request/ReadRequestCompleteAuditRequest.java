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
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.rule.Rules;

import static java.util.Objects.requireNonNull;

/**
 * This is one of the objects that is passed to the {@link uk.gov.gchq.palisade.audit.service.AuditService}
 * to be able to store an audit record. This class extends {@link AuditRequest} This class
 * is used for the indication to the Audit logs that processing has been completed.
 */
public class ReadRequestCompleteAuditRequest extends AuditRequest {

    private User user;
    private Context context;
    private LeafResource resource;
    private Rules rulesApplied;
    private long numberOfRecordsReturned;
    private long numberOfRecordsProcessed;

    public ReadRequestCompleteAuditRequest() {
    }

    /**
     * @param user {@link User} is the user that made the initial registration request to access data
     * @return the {@link ReadRequestCompleteAuditRequest}
     */
    public ReadRequestCompleteAuditRequest user(final User user) {
        requireNonNull(user, "The user type cannot be null");
        this.user = user;
        return this;
    }

    /**
     * @param resource the {@link LeafResource} which the data has just finished being read
     * @return the {@link ReadRequestCompleteAuditRequest}
     */
    public ReadRequestCompleteAuditRequest resource(final LeafResource resource) {
        requireNonNull(resource, "The resource cannot be null");
        this.resource = resource;
        return this;
    }

    /**
     * @param numberOfRecordsReturned is the number of records that was returned to the user from this resource
     * @return the {@link ReadRequestCompleteAuditRequest}
     */
    public ReadRequestCompleteAuditRequest numberOfRecordsReturned(final long numberOfRecordsReturned) {
        requireNonNull(numberOfRecordsReturned, "The numberOfRecordsReturned cannot be null");
        this.numberOfRecordsReturned = numberOfRecordsReturned;
        return this;
    }

    /**
     * @param numberOfRecordsProcessed is the number of records that was processed from this resource
     * @return the {@link ReadRequestCompleteAuditRequest}
     */
    public ReadRequestCompleteAuditRequest numberOfRecordsProcessed(final long numberOfRecordsProcessed) {
        requireNonNull(numberOfRecordsProcessed, "The numberOfRecordsProcessed cannot be null");
        this.numberOfRecordsProcessed = numberOfRecordsProcessed;
        return this;
    }

    /**
     * @param rulesApplied {@link Rules} is the rules that are being applied to this resource for this request
     * @return the {@link ReadRequestCompleteAuditRequest}
     */
    public ReadRequestCompleteAuditRequest rulesApplied(final Rules rulesApplied) {
        requireNonNull(rulesApplied, "The rulesApplied cannot be null");
        this.rulesApplied = rulesApplied;
        return this;
    }

    /**
     * @param context the context that was passed by the client to the palisade service
     * @return the {@link ReadRequestCompleteAuditRequest}
     */
    public ReadRequestCompleteAuditRequest context(final Context context) {
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

    public Rules getRulesApplied() {
        requireNonNull(rulesApplied, "The field rulesApplied has not been set.");
        return rulesApplied;
    }

    public void setRulesApplied(final Rules rulesApplied) {
        rulesApplied(rulesApplied);
    }

    public User getUser() {
        requireNonNull(user, "The user has not been set.");
        return user;
    }

    public void setUser(final User user) {
        user(user);
    }

    public LeafResource getResource() {
        requireNonNull(resource, "The resource has not been set");
        return resource;
    }

    public void setResource(final LeafResource resource) {
        resource(resource);
    }

    public long getNumberOfRecordsReturned() {
        requireNonNull(numberOfRecordsReturned, "The numberOfRecordsReturned has not been set");
        return numberOfRecordsReturned;
    }

    public void setNumberOfRecordsReturned(final long numberOfRecordsReturned) {
        numberOfRecordsReturned(numberOfRecordsReturned);
    }

    public long getNumberOfRecordsProcessed() {
        requireNonNull(numberOfRecordsProcessed, "The numberOfRecordsProcessed has not been set");
        return numberOfRecordsProcessed;
    }

    public void setNumberOfRecordsProcessed(final long numberOfRecordsProcessed) {
        numberOfRecordsProcessed(numberOfRecordsProcessed);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ReadRequestCompleteAuditRequest that = (ReadRequestCompleteAuditRequest) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(user, that.user)
                .append(context, that.context)
                .append(resource, that.resource)
                .append(rulesApplied, that.rulesApplied)
                .append(numberOfRecordsReturned, that.numberOfRecordsReturned)
                .append(numberOfRecordsProcessed, that.numberOfRecordsProcessed)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(29, 37)
                .appendSuper(super.hashCode())
                .append(user)
                .append(context)
                .append(resource)
                .append(rulesApplied)
                .append(numberOfRecordsReturned)
                .append(numberOfRecordsProcessed)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("user", user)
                .append("context", context)
                .append("resource", resource)
                .append("rulesApplied", rulesApplied)
                .append("numberOfRecordsReturned", numberOfRecordsReturned)
                .append("numberOfRecordsProcessed", numberOfRecordsProcessed)
                .toString();
    }

}
