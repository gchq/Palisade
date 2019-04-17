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

package uk.gov.gchq.palisade.resource.service.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import uk.gov.gchq.palisade.ToStringBuilder;
import uk.gov.gchq.palisade.exception.ForbiddenException;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.service.ConnectionDetail;
import uk.gov.gchq.palisade.service.request.Request;

import static java.util.Objects.requireNonNull;

/**
 * This class is used to request that details about a resource is added to the {@link uk.gov.gchq.palisade.resource.service.ResourceService}.
 */
@JsonIgnoreProperties(value = {"originalRequestId"})
public class AddResourceRequest extends Request {
    private LeafResource resource;
    private ConnectionDetail connectionDetail;

    // no-args constructor required
    public AddResourceRequest() {
    }

    /**
     * @param resource The {@link LeafResource} to be added.
     * @return the {@link AddResourceRequest}
     */
    public AddResourceRequest resource(final LeafResource resource) {
        requireNonNull(resource, "The resource cannot be set to null.");
        this.resource = resource;
        return this;
    }

    /**
     * @param connectionDetail Details of how to get to the data from the {@code DataService}.
     * @return the {@link AddResourceRequest}
     */
    public AddResourceRequest connectionDetail(final ConnectionDetail connectionDetail) {
        requireNonNull(connectionDetail, "The connection details cannot be set to null.");
        this.connectionDetail = connectionDetail;
        return this;
    }

    public LeafResource getResource() {
        requireNonNull(resource, "The resource has not been set.");
        return resource;
    }

    public void setResource(final LeafResource resource) {
        resource(resource);
    }

    @Override
    public void setOriginalRequestId(final String originalRequestId) {
        throw new ForbiddenException("Should not call AddResourceRequest.setOriginalRequestId()");
    }

    @Override
    public String getOriginalRequestId() {
        throw new ForbiddenException("Should not call AddResourceRequest.getOriginalRequestId()");
    }

    public ConnectionDetail getConnectionDetail() {
        requireNonNull(connectionDetail, "The connection details have not been set.");
        return connectionDetail;
    }

    public void setConnectionDetail(final ConnectionDetail connectionDetail) {
        connectionDetail(connectionDetail);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final AddResourceRequest that = (AddResourceRequest) o;

        return new EqualsBuilder()
        .appendSuper(super.equals(o))
        .append(resource, that.resource)
        .append(connectionDetail, that.connectionDetail)
        .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
        .appendSuper(super.hashCode())
        .append(resource)
        .append(connectionDetail)
        .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
        .appendSuper(super.toString())
        .append("resource", resource)
        .append("connectionDetail", connectionDetail)
        .toString();
    }
}
