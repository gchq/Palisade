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
import uk.gov.gchq.palisade.data.service.request.ReadRequest;
import uk.gov.gchq.palisade.data.service.request.ReadResponse;
import uk.gov.gchq.palisade.rest.ProxyRestService;

import java.util.concurrent.CompletableFuture;

public class ProxyRestDataService extends ProxyRestService implements DataService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyRestDataService.class);

    public ProxyRestDataService() {
    }

    public ProxyRestDataService(final String baseUrl) {
        this();
        setBaseUrl(baseUrl);
    }

    @Override
    public <T> CompletableFuture<ReadResponse<T>> read(final ReadRequest request) {
        LOGGER.debug("Invoking REST read: " + request);
        final CompletableFuture<ReadResponse> response = doPostAsync("read", request, ReadResponse.class);
        return (CompletableFuture) response;
    }
}
