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

import org.glassfish.hk2.utilities.binding.AbstractBinder;

import uk.gov.gchq.palisade.service.Service;

import static java.util.Objects.requireNonNull;

/**
 * Creates a binder that allows Jersey to inject {@link Service} instances into the constructors of the various REST
 * microservice classes.
 *
 * @param <S> the Palisade service subtype
 */
public class ServiceBinder<S extends Service> extends AbstractBinder {
    /**
     * The {@link Service} instance that will be injected into a constructor upon creation.
     */
    private final S serviceDelegate;

    /**
     * The class that the service delegate implements, usually this should be the specific Palisade {@link Service} sub-type.
     */
    private final Class<S> bindingClass;

    /**
     * Create a service binder.
     *
     * @param delegate     the object to inject
     * @param bindingClass the service type
     */
    public ServiceBinder(final S delegate, final Class<S> bindingClass) {
        requireNonNull(delegate, "delegate");
        requireNonNull(bindingClass, "bindingClass");
        this.serviceDelegate = delegate;
        this.bindingClass = bindingClass;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configure() {
        bind(serviceDelegate).to(bindingClass);
    }
}
