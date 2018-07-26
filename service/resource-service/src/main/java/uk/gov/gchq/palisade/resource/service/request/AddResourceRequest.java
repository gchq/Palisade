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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import uk.gov.gchq.palisade.ToStringBuilder;
import uk.gov.gchq.palisade.resource.ParentResource;
import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.service.request.ConnectionDetail;
import uk.gov.gchq.palisade.service.request.Request;

/**
 * This class is used to request that details about a resource is added to the {@link uk.gov.gchq.palisade.resource.service.ResourceService}.
 */
public class AddResourceRequest extends Request {
    private ParentResource parent;
    private Resource resource;
    private ConnectionDetail connectionDetail;

    // no-args constructor required
    public AddResourceRequest() {
    }

    /**
     * Constructs an {@link AddResourceRequest} with no connection details
     *
     * @param parent   The parent resource, so a {@link uk.gov.gchq.palisade.resource.impl.SystemResource}
     *                 if it is a {@link uk.gov.gchq.palisade.resource.impl.StreamResource}/{@link uk.gov.gchq.palisade.resource.impl.DirectoryResource} to be added,
     *                 or a {@link uk.gov.gchq.palisade.resource.impl.DirectoryResource} if it is a {@link uk.gov.gchq.palisade.resource.impl.FileResource}.
     * @param resource The {@link Resource} to be added.
     */
    public AddResourceRequest(final ParentResource parent, final Resource resource) {
        this.parent = parent;
        this.resource = resource;
    }

    /**
     * Constructs an {@link AddResourceRequest}
     *
     * @param parent           The parent resource, so a {@link uk.gov.gchq.palisade.resource.impl.SystemResource}
     *                         if it is a {@link uk.gov.gchq.palisade.resource.impl.StreamResource}/{@link uk.gov.gchq.palisade.resource.impl.DirectoryResource} to be added,
     *                         or a {@link uk.gov.gchq.palisade.resource.impl.DirectoryResource} if it is a {@link uk.gov.gchq.palisade.resource.impl.FileResource}.
     * @param resource         The {@link Resource} to be added.
     * @param connectionDetail Details of how to get to the data from the {@code DataService}.
     */
    public AddResourceRequest(final ParentResource parent, final Resource resource, final ConnectionDetail connectionDetail) {
        this.parent = parent;
        this.resource = resource;
        this.connectionDetail = connectionDetail;
    }

    public ParentResource getParent() {
        return parent;
    }

    public void setParent(final ParentResource parent) {
        this.parent = parent;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(final Resource resource) {
        this.resource = resource;
    }

    public ConnectionDetail getConnectionDetail() {
        return connectionDetail;
    }

    public void setConnectionDetail(final ConnectionDetail connectionDetail) {
        this.connectionDetail = connectionDetail;
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
                .append(parent, that.parent)
                .append(resource, that.resource)
                .append(connectionDetail, that.connectionDetail)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(parent)
                .append(resource)
                .append(connectionDetail)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("parent", parent)
                .append("resource", resource)
                .append("connectionDetail", connectionDetail)
                .toString();
    }
}
