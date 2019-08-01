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

package uk.gov.gchq.palisade.data.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.data.service.DataService;
import uk.gov.gchq.palisade.data.service.exception.NoCapacityException;
import uk.gov.gchq.palisade.data.service.request.ReadRequest;
import uk.gov.gchq.palisade.data.service.request.ReadResponse;
import uk.gov.gchq.palisade.rest.ProxyRestService;
import uk.gov.gchq.palisade.service.Service;

import javax.ws.rs.core.Response;

import java.io.InputStream;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class ProxyRestDataService extends ProxyRestService implements DataService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyRestDataService.class);

    /**
     * Data service requests should make several requests in the presence of a redirector.
     */
    private static final int DATA_SERVICE_RETRIES = 10;

    /**
     * Wait this amount between successive requests.
     */
    private static final Duration DATA_SERVICE_WAIT = Duration.ofSeconds(10);

    public ProxyRestDataService() {
        retryMax(DATA_SERVICE_RETRIES);
        retryPauseTime(DATA_SERVICE_WAIT);
    }

    @Override
    protected Class<? extends Service> getServiceClass() {
        return DataService.class;
    }

    public ProxyRestDataService(final String baseUrl) {
        this();
        setBaseUrl(baseUrl);
    }

    @Override
    public CompletableFuture<ReadResponse> read(final ReadRequest request) {
        LOGGER.debug("Invoking REST read: " + request);
        try {
            final CompletableFuture<Response> futureResponse = doPostAsync("read/chunked", request, Response.class);
            return futureResponse.thenApply(r -> ReadResponse.createClientReadResponse(r.readEntity(InputStream.class)));
        } catch (NoCapacityException e) {
            LOGGER.warn("Palisade instance has no capacity to process data request on available data services.");
            throw e;
        }
    }


}
