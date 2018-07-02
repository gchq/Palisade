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

package uk.gov.gchq.palisade.service.request;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.ToStringBuilder;
import uk.gov.gchq.palisade.resource.Resource;

import java.util.HashMap;
import java.util.Map;

/**
 *  This is the high level API object that is used to pass back to the client the
 *  information it requires to connect to the correct data service implementations
 *  and to decide how best to parallelise their job.
 *
 *  It is also the object that the client then passes to the data service to access
 *  the data. When it is passed to the data service the resources field might have
 *  been changed to be a subset of the resources.
 */
public class DataRequestResponse {
    public RequestId requestId;
    public Map<Resource, ConnectionDetail> resources;

    public DataRequestResponse() {
        this(new RequestId());
    }

    public DataRequestResponse(final RequestId requestId) {
        this(requestId, new HashMap<>());
    }

    public DataRequestResponse(final RequestId requestId, final Map<Resource, ConnectionDetail> resources) {
        this.requestId = requestId;
        this.resources = resources;
    }

    public DataRequestResponse(final RequestId requestId, final Resource resource, final ConnectionDetail connectionDetail) {
        this(requestId);
        resources.put(resource, connectionDetail);
    }

    public RequestId getRequestId() {
        return requestId;
    }

    public void setRequestId(final RequestId requestId) {
        this.requestId = requestId;
    }

    public Map<Resource, ConnectionDetail> getResources() {
        return resources;
    }

    public void setResources(final Map<Resource, ConnectionDetail> resources) {
        this.resources = resources;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final DataRequestResponse that = (DataRequestResponse) o;

        return new EqualsBuilder()
                .append(requestId, that.requestId)
                .append(resources, that.resources)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 67)
                .append(requestId)
                .append(resources)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("requestId", requestId)
                .append("resources", resources)
                .toString();
    }
}
