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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.exception.ForbiddenException;
import uk.gov.gchq.palisade.service.Service;

/**
 * This class is used to request the cache service return a list of known cache entries. The <code>key</code> methods
 * are overridden to throw UnsupportedOperationException. Clients must use the <code>prefix</code> methods instead.
 */
public class ListCacheRequest extends CacheRequest {

    /**
     * Overriden to throw UnsupportedOperationException.
     *
     * @param key the cache key
     * @return this object
     * @throws UnsupportedOperationException always
     */
    @Override
    public ListCacheRequest key(final String key) {
        throw new UnsupportedOperationException("cannot set key on list request, do you mean prefix()?");
    }

    /**
     * Overriden to throw UnsupportedOperationException.
     *
     * @param key the cache key
     * @throws UnsupportedOperationException always
     */
    @Override
    public void setKey(final String key) {
        throw new UnsupportedOperationException("cannot set key on list request, do you mean setPrefix()");
    }

    /**
     * Set the prefix for the cache list request. This will be used to filter the entries that are returned from the
     * cache.
     *
     * @param prefix the prefix to use for filtering
     * @return this object
     */
    @JsonIgnoreProperties(value = {"originalRequestId"})
    public ListCacheRequest prefix(final String prefix) {
        super.key(prefix);
        return this;
    }

    /**
     * Set the prefix for the cache list request. This will be used to filter the entries that are returned from the
     * cache.
     *
     * @param prefix the prefix to use for filtering
     */
    public void setPrefix(final String prefix) {
        super.setKey(prefix);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ListCacheRequest service(final Class<? extends Service> service) {
        super.service(service);
        return this;
    }

    @Override
    public void setOriginalRequestId(final RequestId originalRequestId) {
        throw new ForbiddenException("Should not call ListCacheRequest.setOriginalRequestId()");
    }

    @Override
    public RequestId getOriginalRequestId() {
        throw new ForbiddenException("Should not call ListCacheRequest.getOriginalRequestId()");
    }
}
