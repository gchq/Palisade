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
import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.palisade.service.Service;

import java.util.function.BiFunction;

public class GetCacheRequest<K,V> extends CacheRequest<K> {

    public GetCacheRequest() {
    }

    public BiFunction<byte[], Class<V>, V> getValueDecoder() {
        return (ob, expectedClass) -> JSONSerialiser.deserialise(ob, expectedClass);
    }

    public GetCacheRequest key(final K key) {
        super.key(key);
        return this;
    }

    public GetCacheRequest service(final Service service) {
        super.service(service);
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        GetCacheRequest that = (GetCacheRequest) o;

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
        return new HashCodeBuilder(3, 11)
                .appendSuper(super.hashCode())
                .toHashCode();
    }
}
