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
import org.apache.commons.lang3.builder.ToStringBuilder;

import uk.gov.gchq.palisade.service.Service;
import uk.gov.gchq.palisade.service.request.Request;

import java.util.Objects;

/**
 * This is the basic cache request object. Each cache request is associated with a {@link Service} which is used to
 * namespace the items in the cache. A key must also be provided which is used to look things up in the cache.
 * <p>
 * All methods in this class throw {@link NullPointerException} if parameters are unset.
 */
public abstract class CacheRequest extends Request {

    /**
     * The key to use when interacting with the cache.
     */
    private String key;

    /**
     * The service class that is interacting with the cache.
     */
    private Class<? extends Service> service;

    /**
     * Separator string used internally to the cache.
     */
    public static final String SEPARATOR = ":";

    public CacheRequest() {
    }

    /**
     * Set the service that is interacting with the cache.
     *
     * @param service the service
     * @return this object
     */
    public CacheRequest service(final Class<? extends Service> service) {
        Objects.requireNonNull(service, "service");
        this.service = service;
        return this;
    }

    /**
     * Return the service class being used for this request.
     *
     * @return the service class
     */
    public Class<? extends Service> getService() {
        Objects.requireNonNull(service, "service cannot be left null");
        return service;
    }

    /**
     * Set the service that is interacting with the cache.
     *
     * @param service the service class
     */
    public void setService(final Class<? extends Service> service) {
        service(service);
    }

    /**
     * Set the key for cache insertion. This key should not be <code>null</code> or empty.
     *
     * @param key the cache key
     * @return this object
     */
    public CacheRequest key(final String key) {
        Objects.requireNonNull(key, "key");
        this.key = key;
        return this;
    }

    /**
     * Set the key for cache interaction. This key should not be <code>null</code> or empty.
     *
     * @param key the cache key
     */
    public void setKey(final String key) {
        key(key);
    }

    /**
     * Get the cache key.
     *
     * @return the key
     */
    public String getKey() {
        Objects.requireNonNull(key, "key cannot be null");
        return key;
    }

    /**
     * Utility method for creating a unique namespaced name in the cache. This can be overridden by subclasses that need
     * to provide a specific way of making a key name.
     *
     * @return a unique key for this request
     */
    public String makeBaseName() {
        return getServiceStringForm() + SEPARATOR + getKey();
    }

    /**
     * Get the string form of the {@link Service}.
     *
     * @return typically this is the class name of the {@link Service} instance
     */
    public String getServiceStringForm() {
        return getService().getTypeName();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("service", service)
                .append("key", key)
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

        CacheRequest that = (CacheRequest) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(service, that.service)
                .append(key, that.key)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(23, 41)
                .appendSuper(super.hashCode())
                .append(service)
                .append(key)
                .toHashCode();
    }
}
