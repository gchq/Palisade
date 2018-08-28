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

import uk.gov.gchq.palisade.ToStringBuilder;


import java.util.Objects;

public class AddCacheGenericRequest<T> extends AddCacheRequest {

    private String key;
    private T cache;

    public AddCacheGenericRequest() {

    }

    public AddCacheGenericRequest key(final String key) {
        Objects.requireNonNull(key, "key");
        this.key = key;
        return this;
    }

    public void setKey(final String key) {
        key(key);
    }

    public String getKey() {
        return key;
    }

    public AddCacheGenericRequest cache(final T cache) {
        Objects.requireNonNull(cache, "cached");
        this.cache = cache;
        return this;
    }

    public void setCache(final T cache) {
        cache(cache);
    }

    public T getCached() {
        return cache;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("key", key)
                .append("cache", cache)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        AddCacheGenericRequest<?> that = (AddCacheGenericRequest<?>) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(getKey(), that.getKey())
                .append(cache, that.cache)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 41)
                .appendSuper(super.hashCode())
                .append(getKey())
                .append(cache)
                .toHashCode();
    }
}
