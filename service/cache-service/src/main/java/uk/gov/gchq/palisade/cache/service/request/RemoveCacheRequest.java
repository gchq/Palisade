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

package uk.gov.gchq.palisade.cache.service.request;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.codehaus.jackson.annotate.JsonIgnore;

import uk.gov.gchq.palisade.ToStringBuilder;
import uk.gov.gchq.palisade.exception.ForbiddenException;
import uk.gov.gchq.palisade.service.Service;

/**
 * This class is the type of request for removing things from the cache. The service and key specify which entry to remove.
 */
public class RemoveCacheRequest extends CacheRequest {
    public RemoveCacheRequest() {
    }

    /**
     * {@inheritDoc}
     */
    public RemoveCacheRequest key(final String key) {
        super.key(key);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public RemoveCacheRequest service(final Class<? extends Service> service) {
        super.service(service);
        return this;
    }

    @JsonIgnore
    @Override
    public void setOriginalRequestId(final String originalRequestId) {
        throw new ForbiddenException("Should not call RemoveCacheRequest.setOriginalRequestId()");
    }

    @JsonIgnore
    @Override
    public String getOriginalRequestId() {
        throw new ForbiddenException("Should not call RemoveCacheRequest.getOriginalRequestId()");
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RemoveCacheRequest that = (RemoveCacheRequest) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(5, 13)
                .appendSuper(super.hashCode())
                .toHashCode();
    }
}
