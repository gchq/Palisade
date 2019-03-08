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
import uk.gov.gchq.palisade.service.ServiceState;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

/**
 * This class implements the REST protocol version of redirection for Palisade services. Using the redirector module, it is
 * able to redirect incoming calls to any REST call on any Palisade service by issuing an HTTP 307 TEMPORARY REDIRECTION
 * to the desired server. The proxy REST client will then transparently redirect to the actual service location.
 * <p>
 * The REST end point for Palisade that is being re-directed MUST have a constructor of the following form that all actual
 * calls are delegated to:
 *
 * <pre> {@code
 *
 * {@literal @}
 * public RestSomeService(final SomeService delegate) {...}
 * }</pre>
 * <p>
 * The working of this class is a little complex so the flow of a request is as follows:
 * <ol>
 * <li>Client makes REST API call to this redirector.</li>
 * <li>The call is received and the pre-request filter will capture the host name of the client.</li>
 * <li>The servlet container will send it to the REST implementation class resource.</li>
 * <li>The REST resource will send the call to its delegate which we will have set as the redirection proxy.</li>
 * <li>The proxy will receive the call and trigger the redirector logic to be called {@link RedirectionMarshall}</li>
 * <li>The proxy call will return null or 0 as appropriate.</li>
 * <li>The servlet container will call post-request filter which will retrieve the host to redirect to from the marshall.</li>
 * <li>The post-request filter will change the response code to a TEMPORARY_REDIRECT 307 and set the Location header.</li>
 * </ol>
 *
 * @param <S> the type of Palisade service being redirected
 * @param <T> the class type that implements S
 */
public class RESTRedirector<S extends Service, T extends S> extends AbstractApplicationConfigV1 implements Service, ContainerRequestFilter, ContainerResponseFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(RESTRedirector.class);

    /**
     * Configuration key for the redirector instance.
     */
    private static final String REDIRECTOR_KEY = "rest.redirect.redirector";
    /**
     * Configuration key for the (probably an interface) class type for the Palisade service being redirected.
     */
    private static final String REDIRECTION_CLASS_KEY = "rest.redirect.class";
    /**
     * Configuration key for the class type that implements the REST end point for this service.
     */
    private static final String REST_IMPL_CLASS_KEY = "rest.redirect.rest_impl.class";

    /**
     * The Palisade REST service type endpoint for the service type being redirected.
     */
    private Class<T> restImplementationClass;
    /**
     * The class type being redirected. This should be a Palisade {@link Service} class.
     */
    private Class<S> redirectionClass;

    /**
     * The redirector that contains the redirection business logic.
     */
    private Redirector<String> redirector;

    /**
     * The marshall that creates the proxy and handles the actual redirection.
     */
    private final RedirectionMarshall<String> marshall;

    /**
     * Has configuration already occurred?
     */
    private boolean configured;

    /**
     * This is injected by the servlet container e.g. Jersey/HK2 when a REST API call is made. This is used to extract
     * the host name of the client so we can pass it to the redirector.
     */
    @Context
    private HttpServletRequest servletRequest;

    /**
     * Create a redirector from a configuration service which will be contacted via data stored in a {@link uk.gov.gchq.palisade.config.service.ConfigurationService}.
     *
     * @see RestUtil#CONFIG_SERVICE_PATH
     */
    public RESTRedirector() {
        this(System.getProperty(RestUtil.CONFIG_SERVICE_PATH));
    }

    /**
     * Create a redirector from a configuration service which will be contacted via data stored in the given path.
     *
     * @param serviceConfigPath the path to load the {@link uk.gov.gchq.palisade.config.service.ConfigurationService} from
     */
    public RESTRedirector(final String serviceConfigPath) {
        requireNonNull(serviceConfigPath, "serviceConfigPath");
        selfConfigure(serviceConfigPath);
        //now make our proxy delegate, give it to the correct rest service and then register that with the resourceconfig
        marshall = new RedirectionMarshall<>(getRedirector());
        configureRedirection();
    }

    /**
     * Package private constructor for testing.
     *
     * @param redirectionClass        the class type being redirected
     * @param restImplementationClass the REST implementation class for the redirection class
     * @param redirector              the redirector instance
     */
    public RESTRedirector(final Class<S> redirectionClass, final Class<T> restImplementationClass, final Redirector<String> redirector) {
        requireNonNull(redirectionClass, "redirectionClass");
        requireNonNull(restImplementationClass, "restImplementationClass");
        requireNonNull(redirector, "redirector");
        this.redirectionClass = redirectionClass;
        this.restImplementationClass = restImplementationClass;
        this.redirector = redirector;
        this.marshall = new RedirectionMarshall<>(getRedirector());
        configureRedirection();
    }

    /**
     * Load our configuration. This will create a {@link uk.gov.gchq.palisade.config.service.ConfigurationService} from
     * the JSON serialised version in the path and then try to load the configuration from it. All configuration keys
     * may be overridden by system properties.
     *
     * @param serviceConfigPath the path to load the JSON from
     */
    private void selfConfigure(final String serviceConfigPath) {
        ServiceState conf = RestUtil.retrieveConfig(RESTRedirector.class, serviceConfigPath, RESTRedirector.class);
        ServiceState overridden = Configurator.applyOverrides(conf, REDIRECTOR_KEY, REDIRECTION_CLASS_KEY, REST_IMPL_CLASS_KEY);
        //self configure
        applyConfigFrom(overridden);
    }

    /**
     * Set up the redirection system. This must create the redirection proxy from the {@link RedirectionMarshall} and then
     * set this to be injected into the REST implementation end point when it is created, so that the proxy becomes
     * the object called by the REST end point. As Jersey will create the REST end point via dependency injection
     * we register a {@link ServiceBinder} to ensure the redirection proxy is injected into the constructor.
     */
    private void configureRedirection() {
        //check we haven't already tried this
        if (configured) {
            throw new IllegalStateException("already configured");
        }
        configured = true;
        //manufacture the delegate
        S service = marshall.createProxyFor(getRedirectionClass());
        //register the original implementation class as a resource
        register(getRestImplementationClass());
        //register the binder that will instantiate it
        register(new ServiceBinder<>(service, getRedirectionClass()));
        //register ourselves as request and response filter
        register(this);
    }

    /**
     * This is the pre-request filter. This is called by Jersey before calling the REST API method in the REST endpoint.
     * We intercept this purely so we can grab the client hostname making the call. This information is set in the marshall
     * so that when the redirector is called, the client hostname is available to it.
     *
     * @param requestContext the servlet request information
     */
    @Override
    public void filter(final ContainerRequestContext requestContext) {
        if (nonNull(servletRequest)) {
            String remHost = servletRequest.getRemoteHost();
            String destination = requestContext.getUriInfo().getAbsolutePath().toString();
            LOGGER.info("Received request from {} to {}", remHost, destination);
            //set in the marshall
            marshall.host(remHost);
        } else {
            LOGGER.warn("No host information available on incoming request");
        }
    }

    /**
     * This is the post-request filter. This is called by Jersey after the call to the REST API method has been made.
     * Here is where we implement the actual redirection behaviour for HTTP. If the marshall states that a redirection
     * has occurred (but not been retrieved) we replace the response body with {@code null} and send an HTTP 307 back
     * to the client and set the location to the new hostname as given by the marshall. Otherwise, we just pass back
     * the original response so things like 404 and everything else get passed back correctly.
     *
     * @param requestContext  the servlet request information
     * @param responseContext the servlet response information
     */
    @Override
    public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext) {
        if (marshall.isRedirectPending()) {
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
                //set location header
                responseContext.getHeaders().putSingle("Location", location.toString());
                LOGGER.info("Redirection occurred, issuing {} {} to {}", Response.Status.TEMPORARY_REDIRECT.getStatusCode(), Response.Status.TEMPORARY_REDIRECT.getReasonPhrase(), location.toString());
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void applyConfigFrom(final ServiceState config) throws NoConfigException {
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
                @SuppressWarnings("unchecked")
                Class<S> deClassRedirect = (Class<S>) Class.forName(serialisedRedirectClass);
                setRedirectionClass(deClassRedirect);
            } else {
                throw new NoConfigException("no redirect service class specified in configuration");
            }

            String serialisedRestImplClass = config.getOrDefault(REST_IMPL_CLASS_KEY, null);
            if (nonNull(serialisedRestImplClass)) {
                @SuppressWarnings("unchecked")
                Class<T> deClassRedirectImpl = (Class<T>) Class.forName(serialisedRestImplClass);
                setRestImplementationClass(deClassRedirectImpl);
            } else {
                throw new NoConfigException("no service class specified in configuration");
            }
        } catch (ClassNotFoundException e) {
            throw new NoConfigException("can't create class object", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void recordCurrentConfigTo(final ServiceState config) {
        requireNonNull(config, "config");
        config.put(REDIRECTOR_KEY, new String(JSONSerialiser.serialise(redirector), StandardCharsets.UTF_8));
        config.put(REDIRECTION_CLASS_KEY, redirectionClass.getTypeName());
        config.put(REST_IMPL_CLASS_KEY, restImplementationClass.getTypeName());
    }

    /**
     * Set the redirector. This is the object that contains the actual business logic for redirection.
     *
     * @param redirector the new redirector
     * @return this object
     */
    public RESTRedirector redirector(final Redirector<String> redirector) {
        requireNonNull(redirector, "redirector");
        this.redirector = redirector;
        return this;
    }

    /**
     * Set the redirector. This is the object that contains the actual business logic for redirection.
     *
     * @param redirector the new redirector
     */
    public void setRedirector(final Redirector<String> redirector) {
        redirector(redirector);
    }

    /**
     * Get the current redirector.
     *
     * @return the redirector
     */
    public Redirector<String> getRedirector() {
        requireNonNull(redirector, "redirector must be set to non null");
        return redirector;
    }

    /**
     * Set the redirection class. This is the class type of the Palisade {@link Service} that is being redirected
     * by this REST redirector.
     *
     * @param redirectionClass the redirection class type
     * @return this object
     */
    public RESTRedirector redirectionClass(final Class<S> redirectionClass) {
        requireNonNull(redirectionClass, "redirectionClass");
        this.redirectionClass = redirectionClass;
        return this;
    }

    /**
     * Set the redirection class. This is the class type of the Palisade {@link Service} that is being redirected
     * by this REST redirector.
     *
     * @param redirectionClass the redirection class type
     */
    public void setRedirectionClass(final Class<S> redirectionClass) {
        redirectionClass(redirectionClass);
    }

    /**
     * Get the redirection class type for the Palisade service being redirected.
     *
     * @return the class type being redirected
     */
    public Class<S> getRedirectionClass() {
        requireNonNull(redirectionClass, "redirectionClass must be set to non null");
        return redirectionClass;
    }

    /**
     * Set the Palisade REST service end point. This is the REST endpoint class type that must extend from the redirection class type.
     *
     * @param restImplementationClass the REST end point class type
     * @return this object
     * @see RESTRedirector#setRedirectionClass(Class)
     */
    public RESTRedirector restImplementationClass(final Class<T> restImplementationClass) {
        requireNonNull(restImplementationClass, "restImplementationClass");
        this.restImplementationClass = restImplementationClass;
        return this;
    }

    /**
     * Set the Palisade REST service end point. This is the REST endpoint class type that must extend from the redirection class type.
     *
     * @param restImplementationClass the REST end point class type
     * @see RESTRedirector#setRedirectionClass(Class)
     */
    public void setRestImplementationClass(final Class<T> restImplementationClass) {
        restImplementationClass(restImplementationClass);
    }

    /**
     * Get the Palisde REST service end point.
     *
     * @return the REST end point class type
     */
    public Class<T> getRestImplementationClass() {
        requireNonNull(restImplementationClass, "restImplementationClass must be set to non null");
        return restImplementationClass;
    }
}
