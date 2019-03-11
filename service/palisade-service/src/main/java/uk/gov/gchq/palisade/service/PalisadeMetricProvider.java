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

package uk.gov.gchq.palisade.service;

import uk.gov.gchq.palisade.service.request.GetMetricRequest;
import uk.gov.gchq.palisade.service.request.Request;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * The core API for retrieving metrics about the system state of a Palisade instance. This is primarily intended for external
 * systems to provide monitor Palisade.
 */
public interface PalisadeMetricProvider extends Service {

    /**
     * Requests metrics on the Palisade system from the metrics provider. The request object can contain filters
     * that specify which keys it is interested in.
     *
     * @param request the request
     * @return the metrics
     */
    CompletableFuture<Map<String, String>> getMetrics(final GetMetricRequest request);

    @Override
    default CompletableFuture<?> process(final Request request) {
        if (request instanceof GetMetricRequest) {
            return getMetrics((GetMetricRequest) request);
        }
        return Service.super.process(request);
    }

}
