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
import uk.gov.gchq.palisade.resource.LeafResource;

import static java.util.Objects.requireNonNull;

/**
 * This is one of the objects that is passed to the {@link uk.gov.gchq.palisade.audit.service.AuditService}
 * to be able to store an audit record. This class extends {@link AuditRequest} This class
 * is used for the indication to the Audit logs that processing has been completed.
 */
public class ReadRequestCompleteAuditRequest extends AuditRequest {

    private LeafResource resource;
    private long numberOfRecordsReturned;

    public ReadRequestCompleteAuditRequest() {
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

    public LeafResource getResource() {
        requireNonNull(resource, "The resource has not been set");
        return resource;
    }

    public void setResource(LeafResource resource) {
        resource(resource);
    }

    public long getNumberOfRecordsReturned() {
        requireNonNull(numberOfRecordsReturned, "The numberOfRecordsReturned has not been set");
        return numberOfRecordsReturned;
    }

    public void setNumberOfRecordsReturned(long numberOfRecordsReturned) {
        numberOfRecordsReturned(numberOfRecordsReturned);
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
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(29, 37)
                .appendSuper(super.hashCode())
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .toString();
    }

}
