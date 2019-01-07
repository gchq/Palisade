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
    private final boolean wasRetrievedLocally;

    /**
     * If true, then this cache entry can be stored in a local cache.
     */
    private final boolean canRetrieveLocally;

    /**
     * Create a new metadata object.
     *
     * @param wasRetrievedLocally if the most recent retrieval came from the local cache or not
     * @param canRetrieveLocally  whether the corresponding entry can be safely cached locally
     */
    public CacheMetadata(final boolean wasRetrievedLocally, final boolean canRetrieveLocally) {
        this.wasRetrievedLocally = wasRetrievedLocally;
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
     * Creates a new cache object with populated metadata based on the value byte array in the given cache object. The new
     * cache object will have a populated metadata object and a new value byte array with the metadata removed.
     *
     * @param remoteRetrieve the cache object from the backing store
     * @return a new cache object
     * @throws IllegalStateException if the metadata is already populated in {@code remoteRetrieve}, or if no cache
     *                               value is present
     */
    public static SimpleCacheObject populateMetaData(final SimpleCacheObject remoteRetrieve) {
        requireNonNull(remoteRetrieve, "remoteRetrieve");
        if (remoteRetrieve.metadata().isPresent()) {
            throw new IllegalStateException("metadata already present");
        }

        byte[] withMeta = remoteRetrieve.getValue().orElseThrow(() -> new IllegalStateException("no cache value present"));
        //remove the metadata from array
        byte[] value = Arrays.copyOfRange(withMeta, 1, withMeta.length);

        CacheMetadata metadata = new CacheMetadata(false, withMeta[0] == 1);
        return new SimpleCacheObject(remoteRetrieve.getValueClass(), Optional.of(value), Optional.of(metadata));
    }

    /**
     * Create a new metadata object with the {@code wasRetrievedLocally} flag set to true.
     *
     * @return a new metadata object
     */
    public CacheMetadata retrievedLocally() {
        return new CacheMetadata(true, this.canRetrieveLocally);
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
