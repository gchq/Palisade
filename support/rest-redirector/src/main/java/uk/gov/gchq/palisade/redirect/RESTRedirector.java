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
import uk.gov.gchq.palisade.rest.ServiceBinder;
import uk.gov.gchq.palisade.rest.application.AbstractApplicationConfigV1;
import uk.gov.gchq.palisade.service.Service;
import uk.gov.gchq.palisade.service.request.ServiceConfiguration;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

public class RESTRedirector extends AbstractApplicationConfigV1 implements Service, ContainerRequestFilter, ContainerResponseFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(RESTRedirector.class);

    private static final String REDIRECTOR_KEY = "rest.redirect.redirector";
    private static final String REDIRECTION_CLASS_KEY = "rest.redirect.class";
    private static final String REST_IMPL_CLASS_KEY = "rest.redirect.rest_impl.class";

    private Class<? extends Service> restImplementationClass;

    private Class<? extends Service> redirectionClass;

    private Redirector<String> redirector;

    private final RedirectionMarshall<String> marshall;

    @Context
    private HttpServletRequest servletRequest;

    public RESTRedirector() {
        this(System.getProperty(RestUtil.CONFIG_SERVICE_PATH));
    }

    public RESTRedirector(final String serviceConfigPath) {
        selfConfigure(serviceConfigPath);
        //now make our proxy delegate, give it to the correct rest service and then register that with the resourceconfig
        marshall = new RedirectionMarshall<>(getRedirector());
        configureRedirection();
    }

    RESTRedirector(Class<? extends Service> restImplementationClass, Class<? extends Service> redirectionClass, Redirector<String> redirector) {
        this.restImplementationClass = restImplementationClass;
        this.redirectionClass = redirectionClass;
        this.redirector = redirector;
        this.marshall = new RedirectionMarshall<>(getRedirector());
        configureRedirection();
    }

    private void selfConfigure(final String serviceConfigPath) {
        ServiceConfiguration conf = RestUtil.retrieveConfig(RESTRedirector.class, serviceConfigPath, RESTRedirector.class);
        ServiceConfiguration overridden = Configurator.applyOverrides(conf, REDIRECTOR_KEY, REDIRECTION_CLASS_KEY);
        //self configure
        applyConfigFrom(overridden);
    }

    @SuppressWarnings("unchecked")
    private void configureRedirection() {
        //manufacture the delegate
        Service service = marshall.createProxyFor(getRedirectionClass());
        //register the original implementation class as a resource
        register(getRestImplementationClass());
        //register the binder that will instantiate it
        register(new ServiceBinder(service, getRedirectionClass()));
        //register ourselves as request and response filter
        register(this);
    }

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {
        if (nonNull(servletRequest)) {
            String remHost = servletRequest.getRemoteHost();
            String destination = requestContext.getUriInfo().getAbsolutePath().toString();
            LOGGER.debug("Received request from {} to {}", remHost, destination);
            //set in the marshall
            marshall.host(remHost);
        } else {
            LOGGER.warn("No host information available on incoming request");
        }
    }

    @Override
    public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext) throws IOException {
        try {
            //the original request will have failed, so we throw it away and set a new response
            responseContext.setEntity(null);
            //set the response to HTTP 307 Temporary Redirect
            responseContext.setStatusInfo(Response.Status.TEMPORARY_REDIRECT);
            //set new location
            URI original = requestContext.getUriInfo().getAbsolutePath();
            //construct URI from components of the original
            URI location = new URI(original.getScheme(), original.getUserInfo(), marshall.redirect((Object) null), original.getPort(),
                    original.getPath(), original.getQuery(), original.getFragment());
            responseContext.getHeaders().putSingle("Location", location.toString());
            LOGGER.debug("Redirection occurred, issuing {} {} to {}", Response.Status.TEMPORARY_REDIRECT.getStatusCode(), Response.Status.TEMPORARY_REDIRECT.getReasonPhrase(), location.toString());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
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

    public RESTRedirector redirector(final Redirector<String> redirector) {
        requireNonNull(redirector, "redirector");
        this.redirector = redirector;
        return this;
    }

    public void setRedirector(final Redirector<String> redirector) {
        redirector(redirector);
    }

    public Redirector<String> getRedirector() {
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
