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
import uk.gov.gchq.palisade.service.request.Request;

/**
 * This class is used to request a list of {@link uk.gov.gchq.palisade.resource.Resource}'s
 * from the {@link uk.gov.gchq.palisade.resource.service.ResourceService} based on the identifier of a {@link uk.gov.gchq.palisade.resource.Resource}.
 * For example getting a list of all {@link uk.gov.gchq.palisade.resource.Resource}'s
 * contained in a {@link uk.gov.gchq.palisade.resource.impl.DirectoryResource} with the given identifier, the same as an {@code ls} would in linux.
 */
public class GetResourcesByIdRequest extends Request {
    private String resourceId;

    // no-args constructor required
    public GetResourcesByIdRequest() {
    }

    /**
     * Default constructor
     *
     * @param resourceId The unique identifier of the resource that you want to {@code ls}
     */
    public GetResourcesByIdRequest(final String resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(final String resourceId) {
        this.resourceId = resourceId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final GetResourcesByIdRequest that = (GetResourcesByIdRequest) o;

        return new EqualsBuilder()
                .append(resourceId, that.resourceId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 37)
                .append(resourceId)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("resourceId", resourceId)
                .toString();
    }
}
