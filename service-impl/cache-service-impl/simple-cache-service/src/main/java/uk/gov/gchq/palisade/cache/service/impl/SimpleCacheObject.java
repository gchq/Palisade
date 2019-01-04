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

/**
 * Represents the basic cache entry that will be stored and retrieved from the backing store. If a store is unable to
 * get a given key then it will return an instance of this class with an empty value. No entries in this class may
 * be <code>null</code>.
 */
public class SimpleCacheObject {

    /**
     * The class of the object being stored in the cache. This should a @{link Class} for the standard form of
     * <code>value</code>.
     */
    private final Class<?> valueClass;
    /**
     * The holder for the object being cached. This may be empty on get requests where they key couldn't be found.
     */
    private final Optional<byte[]> value;

    /**
     * Can this value be retrieved locally from the cache?
     */
    private final boolean canRetrieveLocally;

    /**
     * Was this value retrieved locally from the cache instead of being retrieved from the backing store?
     */
    private final boolean wasRetrievedLocally;

    /**
     * Create a cache object.
     *
     * @param valueClass         the type of the value being cached
     * @param value              the optional cache value, may be empty if no valid entry is present
     * @param canRetrieveLocally whether this value can be retrieved locally
     */
    public SimpleCacheObject(final Class<?> valueClass, final Optional<byte[]> value, final boolean canRetrieveLocally) {
        this(valueClass, value, canRetrieveLocally, false);
    }

    /**
     * Create a cache object.
     *
     * @param valueClass          the type of the value being cached
     * @param value               the optional cache value, may be empty if no valid entry is present
     * @param canRetrieveLocally  whether this value can be retrieved locally
     * @param wasRetrievedLocally whether this value was retrieved locally
     */
    public SimpleCacheObject(final Class<?> valueClass, final Optional<byte[]> value, final boolean canRetrieveLocally, final boolean wasRetrievedLocally) {
        Objects.requireNonNull(valueClass, "valueClass");
        Objects.requireNonNull(value, "value");
        this.valueClass = valueClass;
        this.value = value;
        this.canRetrieveLocally = canRetrieveLocally;
        this.wasRetrievedLocally = wasRetrievedLocally;
    }

    /**
     * Get the class of the object being cached.
     *
     * @return the {@link Class} instance of the cached entry
     */
    public Class<?> getValueClass() {
        return valueClass;
    }

    /**
     * Get the cached value.
     *
     * @return cached value or an empty {@link Optional}
     */
    public Optional<byte[]> getValue() {
        return value;
    }

    /**
     * Get whether this item can be safely cached locally.
     *
     * @return true if the item can be stored locally in a cache
     */
    public boolean canRetrieveLocally() {
        return canRetrieveLocally;
    }

    /**
     * Get whether this item actually was retrieved from a local cache.
     *
     * @return true if the item was retrieved locally from a cache
     */
    public boolean wasRetrieveLocally() {
        return wasRetrievedLocally;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("valueClass", valueClass)
                .append("value", value)
                .append("canRetrieveLocally", canRetrieveLocally)
                .append("wasRetrievedLocally", wasRetrievedLocally)
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

        SimpleCacheObject that = (SimpleCacheObject) o;

        return new EqualsBuilder()
                .append(valueClass, that.valueClass)
                .append(value, that.value)
                .append(canRetrieveLocally, that.canRetrieveLocally)
                .append(wasRetrievedLocally, that.wasRetrievedLocally)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 29)
                .appendSuper(super.hashCode())
                .append(valueClass)
                .append(value)
                .append(canRetrieveLocally)
                .append(wasRetrievedLocally)
                .toHashCode();
    }
}
