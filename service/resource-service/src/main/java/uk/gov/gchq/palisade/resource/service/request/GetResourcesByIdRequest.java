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
import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.service.request.Request;

import static java.util.Objects.requireNonNull;

/**
 * This class is used to request a list of {@link uk.gov.gchq.palisade.resource.Resource}'s
 * from the {@link uk.gov.gchq.palisade.resource.service.ResourceService} based on the identifier of a {@link uk.gov.gchq.palisade.resource.Resource}.
 * For example getting a list of all {@link uk.gov.gchq.palisade.resource.Resource}'s
 * contained in a {@link uk.gov.gchq.palisade.resource.impl.DirectoryResource} with the given identifier, the same as an {@code ls} would in linux.
 */
public class GetResourcesByIdRequest extends Request {
    private String resourceId;
    private UserId userId;

    // no-args constructor required
    public GetResourcesByIdRequest() {
    }

    /**
     * @param resourceId the unique identifier of the resource that you want to {@code ls}
     * @return the {@link GetResourcesByIdRequest}
     */
    public GetResourcesByIdRequest resourceId(final String resourceId) {
        requireNonNull(resourceId, "The resource id cannot be set to null.");
        this.resourceId = resourceId;
        return this;
    }

    /**
     * @param userId the unique identifier of the userId that you want to {@code ls}
     * @return the {@link GetResourcesByIdRequest}
     */
    public GetResourcesByIdRequest userId(final UserId userId) {
        requireNonNull(userId, "The userId cannot be set to null.");
        this.userId = userId;
        return this;
    }

    public String getResourceId() {
        requireNonNull(resourceId, "The resource id has not been set.");
        return resourceId;
    }

    public void setResourceId(final String resourceId) {
        resourceId(resourceId);
    }

    public UserId getUserId() {
        requireNonNull(userId, "The user id has not been set.");
        return userId;
    }

    public void setUserId(final UserId userId) {
        this.userId = userId;
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
                .appendSuper(super.equals(o))
                .append(resourceId, that.resourceId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 37)
                .appendSuper(super.hashCode())
                .append(resourceId)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("resourceId", resourceId)
                .toString();
    }
}
