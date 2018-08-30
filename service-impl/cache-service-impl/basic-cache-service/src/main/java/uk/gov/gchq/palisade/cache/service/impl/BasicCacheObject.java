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
package uk.gov.gchq.palisade.cache.service.impl;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Objects;
import java.util.Optional;

public class BasicCacheObject<T, V> {

    private final Class<V> valueClass;
    private final Optional<T> value;

    public BasicCacheObject(final Class<V> valueClass, final Optional<T> value) {
        Objects.requireNonNull(valueClass, "valueClass");
        Objects.requireNonNull(value, "value");
        this.valueClass = valueClass;
        this.value = value;
    }

    public Class<V> getValueClass() {
        return valueClass;
    }

    public Optional<T> getValue() {
        return value;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("valueClass", valueClass)
                .append("value", value)
                .toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BasicCacheObject<T, V> that = (BasicCacheObject<T, V>) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(valueClass, that.valueClass)
                .append(value, that.value)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 27)
                .appendSuper(super.hashCode())
                .append(valueClass)
                .append(value)
                .toHashCode();
    }
}
