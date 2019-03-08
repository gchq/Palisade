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

package uk.gov.gchq.palisade.redirect.impl;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.cache.service.CacheService;
import uk.gov.gchq.palisade.cache.service.heart.Stethoscope;
import uk.gov.gchq.palisade.redirect.Redirector;
import uk.gov.gchq.palisade.service.Service;

import java.lang.reflect.Method;

import static java.util.Objects.requireNonNull;

/**
 * A base redirector class that can be used to implement other redirectors. Sub-classes should call {@link HeartbeatRedirector#isRedirectionValid(String, String, Method, Object...)}
 * before returning a definite result.
 */
public abstract class HeartbeatRedirector<T> implements Redirector<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(HeartbeatRedirector.class);

    /**
     * The cache service to use for heartbeats.
     */
    private CacheService cache;

    /**
     * The type of service being redirected.
     */
    private Class<? extends Service> redirectClass;

    /**
     * Stethoscope for finding the instances.
     */
    private final Stethoscope scope;

    /**
     * Constructor for empty instance.
     */
    public HeartbeatRedirector() {
        scope = new Stethoscope();
    }

    /**
     * Set the cache for this redirector.
     *
     * @param cacheService the cache service
     * @return this object
     */
    public HeartbeatRedirector cacheService(final CacheService cacheService) {
        requireNonNull(cacheService, "cacheService");
        this.cache = cacheService;
        scope.cacheService(cache);
        return this;
    }

    /**
     * Set the cache for this redirector.
     *
     * @param cacheService the cache service
     */
    public void setCacheService(final CacheService cacheService) {
        cacheService(cacheService);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        HeartbeatRedirector<?> that = (HeartbeatRedirector<?>) o;

        return new EqualsBuilder()
                .append(cache, that.cache)
                .append(redirectClass, that.redirectClass)
                .append(getScope(), that.getScope())
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append(super.toString())
                .append("cache", cache)
                .append("redirectClass", redirectClass)
                .append("scope", scope)
                .toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(11, 37)
                .appendSuper(super.hashCode())
                .append(cache)
                .append(redirectClass)
                .append(getScope())
                .toHashCode();
    }

    /**
     * Get the cache service for this redirector.
     *
     * @return the cache service
     */
    public CacheService getCacheService() {
        requireNonNull(cache, "cache must be initialised");
        return cache;
    }

    /**
     * Set the redirection class type for this redirector. This is the type of {@link Service} that can be redirected.
     *
     * @param redirectClass the type of redirection service
     * @return this object
     */
    public HeartbeatRedirector redirectionClass(final Class<? extends Service> redirectClass) {
        requireNonNull(redirectClass, "redirectClass");
        this.redirectClass = redirectClass;
        scope.serviceClass(redirectClass);
        return this;
    }

    /**
     * Set the redirection class type for this redirector. This is the type of {@link Service} that can be redirected.
     *
     * @param redirectClass the type of redirection service
     */
    public void setRedirectionClass(final Class<? extends Service> redirectClass) {
        redirectionClass(redirectClass);
    }

    /**
     * Get the redirection class type store for this redirector.
     *
     * @return the redirectClass store
     */
    public Class<? extends Service> getRedirectionClass() {
        requireNonNull(redirectClass, "redirectClass must be initialised");
        return redirectClass;
    }

    /**
     * The stethoscope instance for this redirector.
     *
     * @return the stethoscope
     */
    protected Stethoscope getScope() {
        return scope;
    }

    /**
     * Check that a redirection result is valid. This may be overridden by sub-classes. This class' implementation checks
     * that the redirection destination is not the same as a request made by the same host within a recent time frame.
     *
     * @param host        the originating host (may be {@code null})
     * @param destination the intended destination
     * @param method      the method being re-directed
     * @param args        the method arguments
     */
    protected boolean isRedirectionValid(final String host, final String destination, final Method method, final Object... args) {
        LOGGER.info("Call from host {} to \"{}\" intending to be redirected to {}", host, method.getDeclaringClass().getTypeName() + "." + method.getName(), destination);
        return true;
    }

}
