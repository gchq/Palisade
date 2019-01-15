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
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.Util;
import uk.gov.gchq.palisade.exception.Error;
import uk.gov.gchq.palisade.exception.NoConfigException;
import uk.gov.gchq.palisade.exception.PalisadeRuntimeException;
import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.palisade.service.Service;
import uk.gov.gchq.palisade.service.request.ServiceConfiguration;

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
import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;

import static java.util.Objects.requireNonNull;

public abstract class ProxyRestService implements Service {
    public static final String CHARSET = "UTF8";
    public static final String VERSION = "v1";
    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyRestService.class);
    private static final String URL_CONF_KEY_SUFFIX = ".proxy.rest.url";
    private static final String NUM_RETRIES_KEY = ".proxy.retry.max";
    private static final String RETRY_WAIT_TIME = ".proxy.retry.pause";
    private static final Duration DEFAULT_RETRY_TIME = Duration.ofSeconds(10);
    private static final Duration MINIMUM_RETRY_TIME = Duration.ofSeconds(1);
    private static final int DEFAULT_RETRY_COUNT = 1;
    private String baseUrl;
    private String baseUrlWithVersion;
    private Duration retryPauseTime = DEFAULT_RETRY_TIME;
    private int retryMax = DEFAULT_RETRY_COUNT;
    private Client client;

    public ProxyRestService() {
        this.client = createClient();
    }

    public ProxyRestService baseUrl(final String baseUrl) {
        requireNonNull(baseUrl, "The base url cannot be set to null.");
        if (baseUrl.isEmpty()) {
            throw new NullPointerException("The base url cannot be empty.");
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
        return this;
    }

    public String getBaseUrl() {
        requireNonNull(baseUrl, "The base url has not been set.");
        return baseUrl;
    }

    public void setBaseUrl(final String baseUrl) {
        baseUrl(baseUrl);
    }

    public String getBaseUrlWithVersion() {
        requireNonNull(baseUrlWithVersion, "The base url with version has not been set.");
        return baseUrlWithVersion;
    }

    /**
     * Set the number of attempts this service will make to contact the REST service.
     *
     * @param retryMax the maximum number of attempts
     * @return this object
     * @throws IllegalArgumentException if {@code retryMax} is less than 1
     */
    public ProxyRestService retryMax(final int retryMax) {
        if (retryMax < 1) {
            throw new IllegalArgumentException("retryMax must be >=1");
        }
        this.retryMax = retryMax;
        return this;
    }

    /**
     * Set the number of attempts this service will make to contact the REST service.
     *
     * @param retryMax the maximum number of attempts
     * @throws IllegalArgumentException if {@code retryMax} is less than 1
     */
    public void setRetryMax(final int retryMax) {
        retryMax(retryMax);
    }

    /**
     * Get the maximum number of connection retry attempts.
     *
     * @return the maximum number of attempts
     */
    public int getRetryMax() {
        return retryMax;
    }

    /**
     * Set the time to wait between retrying connection attempts.
     *
     * @param retryPauseTime the time between attempts
     * @return this object
     * @throws IllegalArgumentException if the pause time is less than {@link ProxyRestService#MINIMUM_RETRY_TIME}
     */
    public ProxyRestService retryPauseTime(final Duration retryPauseTime) {
        requireNonNull(retryPauseTime, "retryPauseTime");
        checkPauseTime(retryPauseTime);
        this.retryPauseTime = retryPauseTime;
        return this;
    }

    /**
     * Checks the given duration meets minimum requirements.
     *
     * @param pauseTime the candidateTime
     * @throws IllegalArgumentException if  {@code pauseTime } is negative or less than the {@link ProxyRestService#MINIMUM_RETRY_TIME}
     */
    private static void checkPauseTime(final Duration pauseTime) {
        if (pauseTime.isNegative() || MINIMUM_RETRY_TIME.compareTo(pauseTime) > 0) {
            throw new IllegalArgumentException("retryPauseTime must be at least " + MINIMUM_RETRY_TIME.toMillis() + " ms");
        }
    }

    /**
     * Set the time to wait between retrying connection attempts.
     *
     * @param retryPauseTime the time between attempts
     * @throws IllegalArgumentException if the pause time is less than {@link ProxyRestService#MINIMUM_RETRY_TIME}
     */
    public void setRetryPauseTime(final Duration retryPauseTime) {
        retryPauseTime(retryPauseTime);
    }

    /**
     * Get the delay between attempts.
     *
     * @return the wait time
     */
    public Duration getRetryPauseTime() {
        return retryPauseTime;
    }

    @Override
    public void applyConfigFrom(final ServiceConfiguration config) throws NoConfigException {
        requireNonNull(config, "config");
        try {
            String base = config.get(this.getClass().getTypeName() + URL_CONF_KEY_SUFFIX);
            baseUrl(base);

            Duration retryPause = Duration.parse(config.get(this.getClass().getTypeName() + RETRY_WAIT_TIME));
            retryPauseTime(retryPause);

            int retryMaxCount = Integer.parseInt(config.get(this.getClass().getTypeName() + NUM_RETRIES_KEY));
            retryMax(retryMaxCount);
        } catch (NoSuchElementException e) {
            throw new NoConfigException(e);
        }
    }

    @Override
    public void recordCurrentConfigTo(final ServiceConfiguration config) {
        requireNonNull(config, "config");
        config.put(getServiceClass().getTypeName(), getClass().getTypeName());
        config.put(this.getClass().getTypeName() + URL_CONF_KEY_SUFFIX, this.baseUrl);
        config.put(this.getClass().getTypeName() + NUM_RETRIES_KEY, String.valueOf(this.retryMax));
        config.put(this.getClass().getTypeName() + RETRY_WAIT_TIME, this.retryPauseTime.toString());
    }

    protected abstract Class<? extends Service> getServiceClass();

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
        return getBaseUrlWithVersion();
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
        return Util.retryThunker(() -> {
            final O responseObj;
            try {
                final Invocation.Builder request = createRequest(jsonBody, url);
                final Response response = request.post(Entity.json(jsonBody));
                responseObj = handleResponse(response, outputType);
            } catch (final Exception e) {
                LOGGER.debug("Request to {}: \n{}\n failed due to {}\n", url, e.getMessage(), e);
                throw e;
            }
            LOGGER.debug("Request to {} was successful", url);
            return responseObj;
        }, getRetryMax(), getRetryPauseTime());
    }

    protected <O> O doPost(final URL url, final String jsonBody, final Class<O> outputType) {
        return Util.retryThunker(() -> {
            final O responseObj;

            final Invocation.Builder request = createRequest(jsonBody, url);
            try {
                final Response response = request.post(Entity.json(jsonBody));
                responseObj = handleResponse(response, outputType);
            } catch (final Exception e) {
                LOGGER.debug("Request to {}: \n{}\n failed due to {}\n", url, e.getMessage(), e);
                throw e;
            }
            LOGGER.debug("Request to {} was successful", url);
            return responseObj;
        }, getRetryMax(), getRetryPauseTime());
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
        return Util.retryThunker(() -> {
            final O responseObj;
            try {
                final Invocation.Builder request = createRequest(jsonBody, url);
                final Response response;
                response = request.put(Entity.json(jsonBody));
                responseObj = handleResponse(response, outputType);
            } catch (final Exception e) {
                LOGGER.debug("Request to {} failed", url);
                throw e;
            }
            LOGGER.debug("Request to {} was successful", url);
            return responseObj;
        }, getRetryMax(), getRetryPauseTime());
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
        if (Response.class.equals(outputType)) {
            validate(response);
            return (O) response;
        }

        final String outputJson = extractJsonResponse(response);
        O output = null;
        if (null != outputJson) {
            output = deserialise(outputJson, outputType);
        }

        return output;
    }

    private String extractJsonResponse(final Response response) {
        final String outputJson = response.hasEntity() ? response.readEntity(String.class) : null;
        validate(response, outputJson);
        if (null != outputJson) {
            LOGGER.debug("Request returned json: {}", outputJson);
        }
        return outputJson;
    }

    private void validate(final Response response) {
        if (Family.SUCCESSFUL != response.getStatusInfo().getFamily()) {
            validate(response, response.hasEntity() ? response.readEntity(String.class) : null);
        }
    }

    private void validate(final Response response, final String outputJson) {
        if (Family.SUCCESSFUL != response.getStatusInfo().getFamily()) {
            final Error error;
            if (null == outputJson) {
                throw new PalisadeRuntimeException("Returned status: " + response.getStatus() + ". With null response content");
            } else {
                try {
                    error = JSONSerialiser.deserialise(outputJson.getBytes(CHARSET), Error.class);
                } catch (final Exception e) {
                    LOGGER.error("Unable to recreate error object. Bad status {}. Detail: {}", response.getStatus(), outputJson);
                    throw new PalisadeRuntimeException("Returned status: " + response.getStatus() + ". Response content was: " + outputJson, e);
                }
            }
            LOGGER.error("Bad status {}. Detail: {}", response.getStatus(), outputJson);
            throw error.createException();
        }
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

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ProxyRestService that = (ProxyRestService) o;

        return new EqualsBuilder()
                .append(baseUrl, that.baseUrl)
                .append(client, that.client)
                .append(retryMax, that.retryMax)
                .append(retryPauseTime, that.retryPauseTime)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 23)
                .append(baseUrl)
                .append(client)
                .append(retryMax)
                .append(retryPauseTime)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("baseUrl", baseUrl)
                .append("client", client)
                .append("retryMax", retryMax)
                .append("retryPauseTime", retryPauseTime)
                .toString();
    }
}
