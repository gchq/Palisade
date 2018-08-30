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
import java.util.function.Function;

public abstract class CacheRequest<K> extends Request {

    private K key;

    private Class<? extends Service> service;

    public static final String SEPARATOR = ":";

    public CacheRequest() {
    }

    public CacheRequest service(final Service service) {
        Objects.requireNonNull(service, "service");
        this.service = service.getClass();
        return this;
    }

    public Class<? extends Service> getService() {
        Objects.requireNonNull(service, "service cannot be left null");
        return service;
    }

    public void setService(final Service service) {
        service(service);
    }

    public CacheRequest key(final K key) {
        Objects.requireNonNull(key, "key");
        this.key = key;
        return this;
    }

    public void setKey(final K key) {
        key(key);
    }

    public K getKey() {
        Objects.requireNonNull(key, "key cannot be null");
        return key;
    }

    public Function<K, String> getKeyEncoder() {
        return x -> x.toString();
    }

    public String makeBaseName() {
        return getService().getCanonicalName() + SEPARATOR + getKeyEncoder().apply(getKey());
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
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

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
