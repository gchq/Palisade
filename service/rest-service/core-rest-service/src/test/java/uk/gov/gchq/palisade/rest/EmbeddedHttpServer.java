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

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.servlet.ServletRegistration;
import org.glassfish.grizzly.servlet.WebappContext;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.io.IOException;
import java.net.URI;

public class EmbeddedHttpServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddedHttpServer.class);

    protected final Client client = ClientBuilder.newClient();
    protected final ResourceConfig config;
    protected final String baseUrl;
    protected HttpServer server;

    public EmbeddedHttpServer(final String baseUrl, final ResourceConfig config) {
        this.baseUrl = baseUrl.replaceAll("/$", "");
        this.config = config;
    }

    public void startServer() throws IOException {
        if (null == server) {
            URI uri = URI.create(baseUrl);
            server = GrizzlyHttpServerFactory.createHttpServer(uri);
            ServletContainer container = new ServletContainer(config);

            WebappContext context = new WebappContext("PalisadeEmbeddedServer", uri.getPath());
            ServletRegistration registration = context.addServlet(container.getClass().getName(), container);

            registration.addMapping("/*");
            context.deploy(server);

            LOGGER.debug("Started http server {}", baseUrl);
        }
    }

    public void stopServer() {
        if (null != server) {
            server.shutdownNow();
            server = null;
        }
    }

    public boolean isRunning() {
        return null != server;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
