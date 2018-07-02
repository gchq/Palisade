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

package uk.gov.gchq.palisade.service.impl;

import uk.gov.gchq.palisade.rest.ProxyRestService;
import uk.gov.gchq.palisade.service.PalisadeService;
import uk.gov.gchq.palisade.service.request.DataRequestConfig;
import uk.gov.gchq.palisade.service.request.DataRequestResponse;
import uk.gov.gchq.palisade.service.request.RegisterDataRequest;

import java.util.concurrent.CompletableFuture;

public class ProxyRestPalisadeService extends ProxyRestService implements PalisadeService {
    public ProxyRestPalisadeService() {
    }

    public ProxyRestPalisadeService(final String baseUrl) {
        this();
        setBaseUrl(baseUrl);
    }

    @Override
    public CompletableFuture<DataRequestResponse> registerDataRequest(final RegisterDataRequest request) {
        return doPostAsync("registerDataRequest", request, DataRequestResponse.class);
    }

    @Override
    public CompletableFuture<DataRequestConfig> getDataRequestConfig(final DataRequestResponse request) {
        return doPostAsync("getDataRequestConfig", request, DataRequestConfig.class);
    }
}
