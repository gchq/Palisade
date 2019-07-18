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

import uk.gov.gchq.palisade.ToStringBuilder;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.service.ConnectionDetail;

import java.util.Map;
import java.util.TreeMap;

import static java.util.Objects.requireNonNull;

/**
 * This is the high level API object that is used to pass back to the client the information it requires to connect to
 * the correct data service implementations and to decide how best to parallelise their job.
 * <p>
 * It is also the object that the client then passes to the data service to access the data. When it is passed to the
 * data service the resources field might have been changed to be a subset of the resources.
 */
public class DataRequestResponse extends Request {
    private String token;
    private Map<LeafResource, ConnectionDetail> resources;

    public DataRequestResponse() {
    }

    public DataRequestResponse token(final String token) {
        requireNonNull(token, "The token cannot be null.");
        this.token = token;
        return this;
    }

    public void setToken(final String token) {
        token(token);
    }

    public String getToken() {
        requireNonNull(token, "The token has not been set.");
        return token;
    }

    public DataRequestResponse resource(final LeafResource resource, final ConnectionDetail connectionDetail) {
        requireNonNull(resource, "The resource cannot be null.");
        requireNonNull(connectionDetail, "The connection details cannot be null.");
        if (null == resources) {
            resources = new TreeMap<>();
        }
        resources.put(resource, connectionDetail);
        return this;
    }

    public DataRequestResponse resources(final Map<LeafResource, ConnectionDetail> resources) {
        requireNonNull(resources, "The resources cannot be null.");
        this.resources = resources;
        return this;
    }

    public void setResources(final Map<LeafResource, ConnectionDetail> resources) {
        resources(resources);
    }

    public Map<LeafResource, ConnectionDetail> getResources() {
        requireNonNull(resources, "The Resources have not been set.");
        return resources;
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
                .append(token, that.token)
                .append(resources, that.resources)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 67)
                .append(token)
                .append(resources)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("token", token)
                .append("resources", resources)
                .toString();
    }
}
