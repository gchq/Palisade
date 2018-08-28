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

public class GetCacheGenericRequest extends GetCacheRequest {

    private String key;

    public GetCacheGenericRequest() {
    }

    public GetCacheGenericRequest key(final String key) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        GetCacheGenericRequest that = (GetCacheGenericRequest) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(getKey(), that.getKey())
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("key", key)
                .toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(3, 11)
                .appendSuper(super.hashCode())
                .append(getKey())
                .toHashCode();
    }
}
