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
import uk.gov.gchq.palisade.service.PalisadeMetricProvider;
import uk.gov.gchq.palisade.service.Service;
import uk.gov.gchq.palisade.service.request.GetMetricRequest;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ProxyRestPalisadeMetricProvider extends ProxyRestService implements PalisadeMetricProvider {
    public ProxyRestPalisadeMetricProvider() {
    }

    @Override
    protected Class<? extends Service> getServiceClass() {
        return PalisadeMetricProvider.class;
    }

    public ProxyRestPalisadeMetricProvider(final String baseUrl) {
        this();
        setBaseUrl(baseUrl);
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletableFuture<Map<String, String>> getMetrics(final GetMetricRequest request) {
        return (CompletableFuture) this.doPostAsync("getMetrics", request, Map.class);
    }
}
