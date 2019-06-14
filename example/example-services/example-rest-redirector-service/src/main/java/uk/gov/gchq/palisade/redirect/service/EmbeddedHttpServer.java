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

package uk.gov.gchq.palisade.redirect.service;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.servlet.ServletRegistration;
import org.glassfish.grizzly.servlet.WebappContext;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.rest.filter.OriginFilter;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;

import java.net.URI;
import java.util.EnumSet;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

/**
 * An embedded web server that uses Grizzly and Jersey to start a servlet container.
 */
public class EmbeddedHttpServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddedHttpServer.class);

    /**
     * The servlet configuration class.
     */
    private final ResourceConfig config;
    /**
     * URL that the servlet will be served from.
     */
    private final String baseUrl;
    /**
     * The server instance.
     */
    private HttpServer server;

    /**
     * Create an HTTP server to serve pages from.
     *
     * @param baseUrl the base URL to serve the servlet from
     * @param config  the servlet configuration object
     */
    public EmbeddedHttpServer(final String baseUrl, final ResourceConfig config) {
        requireNonNull(baseUrl, "baseUrl");
        requireNonNull(config, "config");
        this.baseUrl = baseUrl.replaceAll("/$", "");
        this.config = config;
    }

    /**
     * Start the web server. This will create a non-daemon server thread that will stop the JVM from exiting.
     */
    public void startServer() {
        if (isNull(server)) {
            URI uri = URI.create(baseUrl);
            server = GrizzlyHttpServerFactory.createHttpServer(uri);
            ServletContainer container = new ServletContainer(config);

            WebappContext context = new WebappContext("PalisadeEmbeddedServer", uri.getPath());
            ServletRegistration registration = context.addServlet(container.getClass().getName(), container);

            FilterRegistration filterReg = context.addFilter("OriginFilter", OriginFilter.class);
            filterReg.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), false, "/*");

            registration.addMapping("/*");
            context.deploy(server);

            LOGGER.debug("Started http server {}", baseUrl);
        }
    }

    /**
     * Stop the web server.
     */
    public void stopServer() {
        if (nonNull(server)) {
            server.shutdownNow();
            server = null;
        }
    }

    /**
     * Checks if the web server is running.
     *
     * @return {@code true} if the server is running
     */
    public boolean isRunning() {
        return nonNull(server);
    }

    /**
     * Get the URL this server is serving pages from.
     *
     * @return the base URL for this server
     */
    public String getBaseUrl() {
        return baseUrl;
    }
}
