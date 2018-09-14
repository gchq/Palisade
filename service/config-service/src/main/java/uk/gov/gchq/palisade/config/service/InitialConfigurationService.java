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
package uk.gov.gchq.palisade.config.service;

import uk.gov.gchq.palisade.config.service.request.GetConfigRequest;
import uk.gov.gchq.palisade.service.request.InitialConfig;
import uk.gov.gchq.palisade.service.Service;
import uk.gov.gchq.palisade.service.request.Request;

import java.util.concurrent.CompletableFuture;

public interface InitialConfigurationService extends Service {

    CompletableFuture<InitialConfig> get(final GetConfigRequest request);

    @Override
    default CompletableFuture<?> process(final Request request) {
        if (request instanceof GetConfigRequest) {
            get((GetConfigRequest) request);
            return null;
        }
        return Service.super.process(request);
    }
}
