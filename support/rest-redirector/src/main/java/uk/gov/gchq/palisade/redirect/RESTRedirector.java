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

package uk.gov.gchq.palisade.redirect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.config.service.Configurator;
import uk.gov.gchq.palisade.exception.NoConfigException;
import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.palisade.rest.RestUtil;
import uk.gov.gchq.palisade.rest.application.AbstractApplicationConfigV1;
import uk.gov.gchq.palisade.service.Service;
import uk.gov.gchq.palisade.service.request.ServiceConfiguration;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

public class RESTRedirector extends AbstractApplicationConfigV1 implements Service {
    private static final Logger LOGGER = LoggerFactory.getLogger(RESTRedirector.class);

    private static final String REDIRECTOR_KEY = "rest.redirect.redirector";
    private static final String REDIRECTION_CLASS_KEY = "rest.redirect.class";
    private static final String REST_IMPL_CLASS_KEY = "rest.redirect.rest_impl.class";

    private Class<? extends Service> restImplementationClass;

    private Class<? extends Service> redirectionClass;

    private Redirector<URL> redirector;

    private final RedirectionMarshall<URL> marshall;

    public RESTRedirector() {
        this(System.getProperty(RestUtil.CONFIG_SERVICE_PATH));
    }

    public RESTRedirector(final String serviceConfigPath) {
        selfConfigure(serviceConfigPath);
        //now make our proxy delegate, give it to the correct rest service and then register that with the resourceconfig
        marshall = new RedirectionMarshall<>(getRedirector());
        configureRedirection();
    }

    private void selfConfigure(final String serviceConfigPath) {
        ServiceConfiguration conf = RestUtil.retrieveConfig(RESTRedirector.class, serviceConfigPath, RESTRedirector.class);
        ServiceConfiguration overridden = Configurator.applyOverrides(conf, REDIRECTOR_KEY, REDIRECTION_CLASS_KEY);
        //self configure
        applyConfigFrom(overridden);
    }

    private void configureRedirection() {
        //manufacture the delegate
        Service service = marshall.createProxyFor(getRedirectionClass());
        //now create the REST implementation and set the delegate
        try {
            //try to find the constructor that takes the redirection class as a delegate and create it
            Service restImpl = getRestImplementationClass().getConstructor(getRedirectionClass()).newInstance(service);
            //register that with javax.ws.rs API
            register(restImpl);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("can't find constructor (Service) for " + restImplementationClass.getTypeName(), e);
        }
        //create the response filter to handle the redirect

        //TODO: make the response filter that will catch the errors and send a 307
    }

    RESTRedirector(Class<? extends Service> restImplementationClass, Class<? extends Service> redirectionClass, Redirector<URL> redirector) {
        this.restImplementationClass = restImplementationClass;
        this.redirectionClass = redirectionClass;
        this.redirector = redirector;
        this.marshall = new RedirectionMarshall<>(getRedirector());
        configureRedirection();
    }

    @Override
    public void applyConfigFrom(final ServiceConfiguration config) throws NoConfigException {
        requireNonNull(config, "config");
        String serialisedRedirect = config.getOrDefault(REDIRECTOR_KEY, null);
        if (nonNull(serialisedRedirect)) {
            setRedirector(JSONSerialiser.deserialise(serialisedRedirect.getBytes(StandardCharsets.UTF_8), Redirector.class));
        } else {
            throw new NoConfigException("no redirector specified in configuration");
        }
        try {
            String serialisedRedirectClass = config.getOrDefault(REDIRECTION_CLASS_KEY, null);
            if (nonNull(serialisedRedirectClass)) {
                setRedirectionClass(Class.forName(serialisedRedirectClass).asSubclass(Service.class));
            } else {
                throw new NoConfigException("no redirect service class specified in configuration");
            }

            String serialisedRestImplClass = config.getOrDefault(REST_IMPL_CLASS_KEY, null);
            if (nonNull(serialisedRestImplClass)) {
                setRestImplementationClass(Class.forName(serialisedRestImplClass).asSubclass(Service.class));
            } else {
                throw new NoConfigException("no service class specified in configuration");
            }
        } catch (ClassNotFoundException e) {
            throw new NoConfigException("can't create class object", e);
        }
    }

    @Override
    public void recordCurrentConfigTo(final ServiceConfiguration config) {
        requireNonNull(config, "config");
        config.put(REDIRECTOR_KEY, new String(JSONSerialiser.serialise(redirector), StandardCharsets.UTF_8));
        config.put(REDIRECTION_CLASS_KEY, redirectionClass.getTypeName());
        config.put(REST_IMPL_CLASS_KEY, restImplementationClass.getTypeName());
    }

    public RESTRedirector redirector(final Redirector<URL> redirector) {
        requireNonNull(redirector, "redirector");
        this.redirector = redirector;
        return this;
    }

    public void setRedirector(final Redirector<URL> redirector) {
        redirector(redirector);
    }

    public Redirector<URL> getRedirector() {
        requireNonNull(redirector, "redirector must be set to non null");
        return redirector;
    }

    public RESTRedirector redirectionClass(final Class<? extends Service> redirectionClass) {
        requireNonNull(redirectionClass, "redirectionClass");
        this.redirectionClass = redirectionClass;
        return this;
    }

    public void setRedirectionClass(final Class<? extends Service> redirectionClass) {
        redirectionClass(redirectionClass);
    }

    public Class<? extends Service> getRedirectionClass() {
        requireNonNull(redirectionClass, "redirectionClass must be set to non null");
        return redirectionClass;
    }

    public RESTRedirector restImplementationClass(final Class<? extends Service> restImplementationClass) {
        requireNonNull(restImplementationClass, "restImplementationClass");
        this.restImplementationClass = restImplementationClass;
        return this;
    }

    public void setRestImplementationClass(final Class<? extends Service> restImplementationClass) {
        restImplementationClass(restImplementationClass);
    }

    public Class<? extends Service> getRestImplementationClass() {
        requireNonNull(restImplementationClass, "restImplementationClass must be set to non null");
        return restImplementationClass;
    }
}
