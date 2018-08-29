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

import java.util.Objects;
import java.util.function.Function;

public class GetCacheRequest<K> extends CacheRequest<K> {

    private Class<?> expectedClass;

    public GetCacheRequest() {
    }

    public GetCacheRequest expectedClass(final Class<?> expectedClass) {
        Objects.requireNonNull(expectedClass, "expectedClass");
        this.expectedClass = expectedClass;
        return this;
    }

    public void setExpectedClass(final Class<?> key) {
        expectedClass(expectedClass);
    }

    public Class<?> getExpectedClass() {
        Objects.requireNonNull(expectedClass, "expected class must be specified");
        return expectedClass;
    }

    public Function<byte[], Object> getValueDecoder() {
        return x -> JSONSerialiser.deserialise(x, getExpectedClass());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        GetCacheRequest that = (GetCacheRequest) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(expectedClass, that.expectedClass)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("expectedClass", expectedClass)
                .toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(3, 11)
                .appendSuper(super.hashCode())
                .append(expectedClass)
                .toHashCode();
    }
}
