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

import uk.gov.gchq.palisade.cache.service.request.AddCacheRequest;

import java.util.Arrays;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Metadata information about a cache entry.
 */
public class CacheMetadata {

    /**
     * If true, then this cache entry has been retrieved from a local cache.
     */
    private boolean wasRetrievedLocally;

    /**
     * If true, then this cache entry can be stored in a local cache.
     */
    private final boolean canRetrieveLocally;

    /**
     * Create a new metadata object.
     *
     * @param canRetrieveLocally whether the corresponding entry can be safely cached locally
     */
    public CacheMetadata(final boolean canRetrieveLocally) {
        this.wasRetrievedLocally = false;
        this.canRetrieveLocally = canRetrieveLocally;
    }

    /**
     * Encodes extra metadata needed about this entry into the start of the byte array given to the backing store.
     *
     * @param encodedValue the encoded cache value object
     * @param request      the cache request
     * @return a new byte array containing metadata
     */
    public static byte[] addMetaData(final byte[] encodedValue, final AddCacheRequest<?> request) {
        requireNonNull(encodedValue, "encodedValue");
        requireNonNull(request, "request");
        byte[] withMeta = new byte[encodedValue.length + 1];
        System.arraycopy(encodedValue, 0, withMeta, 1, encodedValue.length);
        if (request.getLocallyCacheable()) {
            withMeta[0] = 1;
        }
        return withMeta;
    }

    /**
     * Populates the cache metadata for the given entry. This will replace the value array and the metadata object inside
     * the cache object.
     *
     * @param remoteRetrieve the cache object from the backing store
     * @throws IllegalStateException if the metadata is already populated in {@code remoteRetrieve}, or if no cache
     *                               value is present
     */
    public static void populateMetaData(final SimpleCacheObject remoteRetrieve) {
        requireNonNull(remoteRetrieve, "remoteRetrieve");
        if (remoteRetrieve.getMetadata().isPresent()) {
            throw new IllegalStateException("metadata already present");
        }

        byte[] withMeta = remoteRetrieve.getValue().orElseThrow(() -> new IllegalStateException("no cache value present"));
        //remove the metadata from array
        byte[] value = Arrays.copyOfRange(withMeta, 1, withMeta.length);

        CacheMetadata metadata = new CacheMetadata(withMeta[0] == 1);
        remoteRetrieve.setMetadata(Optional.of(metadata));
        remoteRetrieve.setValue(Optional.of(value));
    }

    /**
     * Sets the flag for whether the associated entry has been locally retrieved.
     *
     * @param wasRetrievedLocally how was the most recent retrieval done
     * @return this object
     */
    public CacheMetadata setWasRetrievedLocally(final boolean wasRetrievedLocally) {
        this.wasRetrievedLocally = wasRetrievedLocally;
        return this;
    }

    /**
     * @return true if this entry was retrieved from a local cache
     */
    public boolean wasRetrievedLocally() {
        return wasRetrievedLocally;
    }

    /**
     * @return true if this entry can be stored locally
     */
    public boolean canBeRetrievedLocally() {
        return canRetrieveLocally;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CacheMetadata that = (CacheMetadata) o;

        return new EqualsBuilder()
                .append(wasRetrievedLocally, that.wasRetrievedLocally)
                .append(canRetrieveLocally, that.canRetrieveLocally)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(wasRetrievedLocally)
                .append(canRetrieveLocally)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("wasRetrievedLocally", wasRetrievedLocally)
                .append("canRetrieveLocally", canRetrieveLocally)
                .toString();
    }
}
