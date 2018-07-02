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
import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.service.request.Request;

/**
 * This class is used to request a list of {@link uk.gov.gchq.palisade.resource.Resource}'s
 * from the {@link uk.gov.gchq.palisade.resource.service.ResourceService} based on a {@link uk.gov.gchq.palisade.resource.Resource}.
 * For example getting a list of all {@link uk.gov.gchq.palisade.resource.Resource}'s
 * contained in the given {@link uk.gov.gchq.palisade.resource.impl.DirectoryResource}, the same as an {@code ls} would in linux.
 */
public class GetResourcesByResourceRequest extends Request {
    private Resource resource;

    // no-args constructor required
    public GetResourcesByResourceRequest() {
    }

    /**
     * Default constructor
     *
     * @param resource The {@link Resource} you want to run an {@code ls} on.
     */
    public GetResourcesByResourceRequest(final Resource resource) {
        this.resource = resource;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(final Resource resource) {
        this.resource = resource;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final GetResourcesByResourceRequest that = (GetResourcesByResourceRequest) o;

        return new EqualsBuilder()
                .append(resource, that.resource)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(11, 37)
                .append(resource)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("resource", resource)
                .toString();
    }
}
