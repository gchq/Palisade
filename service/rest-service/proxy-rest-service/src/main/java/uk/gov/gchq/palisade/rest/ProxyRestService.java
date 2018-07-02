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

import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.exception.Error;
import uk.gov.gchq.palisade.exception.PalisadeRuntimeException;
import uk.gov.gchq.palisade.exception.PalisadeWrappedErrorRuntimeException;
import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.palisade.service.Service;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public abstract class ProxyRestService implements Service {
    public static final String CHARSET = "UTF8";
    public static final String VERSION = "v1";
    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyRestService.class);
    private String baseUrl;
    private String baseUrlWithVersion;
    private Client client;

    public ProxyRestService() {
        this.client = createClient();
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(final String baseUrl) {
        if (null == baseUrl || baseUrl.isEmpty()) {
            this.baseUrl = null;
            this.baseUrlWithVersion = null;
        } else {
            this.baseUrl = baseUrl;
            if (baseUrl.endsWith(VERSION + "/")) {
                this.baseUrlWithVersion = baseUrl;
            } else if (baseUrl.endsWith(VERSION)) {
                this.baseUrlWithVersion = baseUrl + "/";
            } else if (baseUrl.endsWith("/")) {
                this.baseUrlWithVersion = baseUrl + VERSION + "/";
            } else {
                this.baseUrlWithVersion = baseUrl + "/" + VERSION + "/";
            }
        }
    }

    public String getBaseUrlWithVersion() {
        return baseUrlWithVersion;
    }

    protected URL getUrl() {
        return getUrl(null);
    }

    protected URL getUrl(final String endpoint) {
        try {
            return new URL(getStringUrl() + (null != endpoint ? endpoint : ""));
        } catch (final MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    protected String getStringUrl() {
        Objects.requireNonNull(baseUrlWithVersion);
        return baseUrlWithVersion;
    }

    protected <O> CompletableFuture<O> doPostAsync(final String endpoint, final Object body, final TypeReference<O> outputType) {
        return CompletableFuture.supplyAsync(() -> doPost(endpoint, body, outputType));
    }

    protected <O> CompletableFuture<O> doPostAsync(final String endpoint, final Object body, final Class<O> outputType) {
        return CompletableFuture.supplyAsync(() -> doPost(endpoint, body, outputType));
    }

    protected <O> O doPost(final String endpoint, final Object body, final TypeReference<O> outputType) {
        return doPost(getUrl(endpoint), body, outputType);
    }

    protected <O> O doPost(final String endpoint, final Object body, final Class<O> outputType) {
        return doPost(getUrl(endpoint), body, outputType);
    }

    protected <O> O doPost(final URL url, final Object body, final TypeReference<O> outputType) {
        try {
            return doPost(url, new String(JSONSerialiser.serialise(body), "UTF-8"), outputType);
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    protected <O> O doPost(final URL url, final Object body, final Class<O> outputType) {
        try {
            return doPost(url, new String(JSONSerialiser.serialise(body), "UTF-8"), outputType);
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    protected <O> O doPost(final URL url, final String jsonBody, final TypeReference<O> outputType) {
        final O responseObj;
        try {
            final Invocation.Builder request = createRequest(jsonBody, url);
            final Response response = request.post(Entity.json(jsonBody));
            responseObj = handleResponse(response, outputType);
        } catch (final Exception e) {
            LOGGER.debug("Request to {} with body: \n{}\n failed due to {}\n", url, jsonBody, e.getMessage(), e);
            throw e;
        }
        LOGGER.debug("Request to {} with body: \n{}\n returned: {}\n", url, jsonBody, responseObj);
        return responseObj;
    }

    protected <O> O doPost(final URL url, final String jsonBody, final Class<O> outputType) {
        final O responseObj;

        final Invocation.Builder request = createRequest(jsonBody, url);
        try {
            final Response response = request.post(Entity.json(jsonBody));
            responseObj = handleResponse(response, outputType);
        } catch (final Exception e) {
            LOGGER.debug("Request to {} with body: \n{}\n failed due to {}\n", url, jsonBody, e.getMessage(), e);
            throw e;
        }
        LOGGER.debug("Request to {} with body: \n{}\n returned: {}\n", url, jsonBody, responseObj);
        return responseObj;
    }

    protected <O> CompletableFuture<O> doPutAsync(final String endpoint, final Object body, final Class<O> outputType) {
        return CompletableFuture.supplyAsync(() -> doPut(endpoint, body, outputType));
    }

    protected <O> O doPut(final String endpoint, final Object body, final Class<O> outputType) {
        return doPut(getUrl(endpoint), body, outputType);
    }

    protected <O> O doPut(final URL url, final Object body, final Class<O> outputType) {
        try {
            return doPut(url, new String(JSONSerialiser.serialise(body), "UTF-8"), outputType);
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    protected <O> O doPut(final URL url, final String jsonBody, final Class<O> outputType) {
        final O responseObj;
        try {
            final Invocation.Builder request = createRequest(jsonBody, url);
            final Response response;
            response = request.put(Entity.json(jsonBody));
            responseObj = handleResponse(response, outputType);
        } catch (final Exception e) {
            LOGGER.debug("Request to {} with body: \n{}\n failed", url, jsonBody);
            throw e;
        }
        LOGGER.debug("Request to {} with body: \n{}\n returned: {}\n", url, jsonBody, responseObj);
        return responseObj;
    }

    protected Invocation.Builder createRequest(final String body, final URL url) {
        final Invocation.Builder request = client.target(url.toString())
                .request();
        if (null != body) {
            request.header("Content", MediaType.APPLICATION_JSON_TYPE);
            request.build(body);
        }
        return request;
    }

    protected <O> O handleResponse(final Response response, final TypeReference<O> outputType) {
        final String outputJson = extractJsonResponse(response);

        O output = null;
        if (null != outputJson) {
            output = deserialise(outputJson, outputType);
        }

        return output;
    }

    protected <O> O handleResponse(final Response response, final Class<O> outputType) {
        final String outputJson = extractJsonResponse(response);

        O output = null;
        if (null != outputJson) {
            output = deserialise(outputJson, outputType);
        }

        return output;
    }

    private String extractJsonResponse(final Response response) {
        final String outputJson = response.hasEntity() ? response.readEntity(String.class) : null;
        if (Family.SUCCESSFUL != response.getStatusInfo().getFamily()) {
            final Error error;
            if (null == outputJson) {
                throw new PalisadeRuntimeException("Returned status: " + response.getStatus() + ". With null response content");
            } else {
                try {
                    error = JSONSerialiser.deserialise(outputJson.getBytes(CHARSET), Error.class);
                } catch (final Exception e) {
                    LOGGER.error("Bad status {}. Detail: {}", response.getStatus(), outputJson);
                    throw new PalisadeRuntimeException("Returned status: " + response.getStatus() + ". Response content was: " + outputJson, e);
                }
            }
            LOGGER.error("Error: Bad status {}. Detail: {}", response.getStatus(), outputJson);
            throw new PalisadeWrappedErrorRuntimeException(error);
        }
        if (null != outputJson) {
            LOGGER.debug("Request returned json: {}", outputJson);
        }
        return outputJson;
    }

    protected <O> O deserialise(final String jsonString, final TypeReference<O> outputType) {
        final byte[] jsonBytes = toBytes(jsonString);

        return JSONSerialiser.deserialise(jsonBytes, outputType);
    }

    protected <O> O deserialise(final String jsonString, final Class<O> outputType) {
        final byte[] jsonBytes = toBytes(jsonString);

        return JSONSerialiser.deserialise(jsonBytes, outputType);
    }

    protected byte[] toBytes(final String jsonString) {
        final byte[] jsonBytes;
        try {
            jsonBytes = jsonString.getBytes(CHARSET);
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException("Unable to deserialise JSON: " + jsonString, e);
        }
        return jsonBytes;
    }

    protected Client createClient() {
        return ClientBuilder.newClient();
    }
}
