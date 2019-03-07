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

package uk.gov.gchq.palisade.rest;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import uk.gov.gchq.palisade.service.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Creates a binder that allows Jersey to inject {@link Service} instances into the constructors of the various REST
 * microservice classes.
 */
public class ServiceBinder extends AbstractBinder {
    /**
     * The map of which instances to bind to which classes when asked to configure.
     */
    private final Map<Service, List<Class<? extends Service>>> bindMap = new HashMap<>();

    /**
     * Create an empty service binder.
     */
    public ServiceBinder() {
    }

    /**
     * Create a service binder of the given binding. This will be added to the map of instances to bind.
     *
     * @param delegate     the object to inject
     * @param bindingClass the service type
     */
    public <S extends Service> ServiceBinder(final S delegate, final Class<S> bindingClass) {
        requireNonNull(delegate, "delegate");
        requireNonNull(bindingClass, "bindingClass");
        register(delegate, bindingClass);
    }

    /**
     * Registers a service instance and class for dependency injection. When Jersey sees an inject annotation
     * for the given {@code serviceClass} it will inject the given instance.
     *
     * @param service      the instance to inject
     * @param serviceClass the class to bind the instance to
     * @return this object
     * @throws ClassCastException if {@code service} is not an instance of {@code serviceClass}
     */
    public <S extends Service> ServiceBinder register(final S service, final Class<S> serviceClass) {
        requireNonNull(service, "service");
        requireNonNull(serviceClass, "serviceClass");
        if (!serviceClass.isInstance(service)) {
            throw new ClassCastException(service.getClass() + " is not an instance of " + serviceClass.getTypeName());
        }
        bindMap.computeIfAbsent(service, key -> new ArrayList<>()).add(serviceClass);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configure() {
        bindMap.entrySet().stream()
                .forEach(entry -> entry.getValue().stream()
                        .forEach(
                                clazz -> {
                                    @SuppressWarnings("unchecked")
                                    Class<Service> cc = (Class<Service>) clazz;
                                    bind(entry.getKey()).to(cc);
                                }
                        )
                );
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ServiceBinder that = (ServiceBinder) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(bindMap, that.bindMap)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(41, 47)
                .appendSuper(super.hashCode())
                .append(bindMap)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("bindMap", bindMap)
                .toString();
    }
}
