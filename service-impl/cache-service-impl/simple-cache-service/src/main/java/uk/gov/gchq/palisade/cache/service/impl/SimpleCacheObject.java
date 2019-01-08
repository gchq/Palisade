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

import java.util.Optional;

import static java.util.Objects.requireNonNull;

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
    private Class<?> valueClass;
    /**
     * The holder for the object being cached. This may be empty on get requests where they key couldn't be found.
     */
    private Optional<byte[]> value;
    /**
     * Extra information about this entry and it's retrieval.
     */
    private Optional<CacheMetadata> metadata;

    /**
     * Create a cache object.
     *
     * @param valueClass the type of the value being cached
     * @param value      the optional cache value, may be empty if no valid entry is present
     */
    public SimpleCacheObject(final Class<?> valueClass, final Optional<byte[]> value) {
        requireNonNull(valueClass, "valueClass");
        requireNonNull(value, "value");
        this.valueClass = valueClass;
        this.value = value;
        this.metadata = Optional.empty();
    }

    /**
     * Set the metadata for this cache object.
     *
     * @param metadata the new metadata
     * @return this object
     */
    public SimpleCacheObject metadata(final Optional<CacheMetadata> metadata) {
        requireNonNull(metadata, "metadata");
        this.metadata = metadata;
        return this;
    }

    /**
     * Set the metadata for this cache object.
     *
     * @param metadata the new metadata
     */
    public void setMetadata(final Optional<CacheMetadata> metadata) {
        metadata(metadata);
    }

    /**
     * Get the metadata about this cache entry.
     *
     * @return the metadata
     */
    public Optional<CacheMetadata> getMetadata() {
        return metadata;
    }

    /**
     * Set the cache entry.
     *
     * @param value the new cache entry
     * @return this object
     */
    public SimpleCacheObject value(final Optional<byte[]> value) {
        requireNonNull(value, "value");
        this.value = value;
        return this;
    }

    /**
     * Set the cache entry.
     *
     * @param value the new cache entry
     */
    public void setValue(final Optional<byte[]> value) {
        value(value);
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
     * Get the class of the object being cached.
     *
     * @return the {@link Class} instance of the cached entry
     */
    public Class<?> getValueClass() {
        return valueClass;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("valueClass", valueClass)
                .append("value", value)
                .append("metadata", metadata)
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
                .append(metadata, that.metadata)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 29)
                .append(valueClass)
                .append(value)
                .append(metadata)
                .toHashCode();
    }
}
