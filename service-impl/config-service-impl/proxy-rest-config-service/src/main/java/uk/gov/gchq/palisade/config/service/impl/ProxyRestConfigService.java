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
package uk.gov.gchq.palisade.config.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.config.service.ConfigurationService;
import uk.gov.gchq.palisade.config.service.request.AddConfigRequest;
import uk.gov.gchq.palisade.config.service.request.GetConfigRequest;
import uk.gov.gchq.palisade.rest.ProxyRestService;
import uk.gov.gchq.palisade.service.Service;
import uk.gov.gchq.palisade.service.request.ServiceConfiguration;

import java.util.concurrent.CompletableFuture;

public class ProxyRestConfigService extends ProxyRestService implements ConfigurationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyRestConfigService.class);

    public ProxyRestConfigService() {
    }

    @Override
    protected Class<? extends Service> getServiceClass() {
        return ConfigurationService.class;
    }

    public ProxyRestConfigService(final String baseUrl) {
        this();
        setBaseUrl(baseUrl);
    }

    @Override
    public CompletableFuture<ServiceConfiguration> get(final GetConfigRequest request) {
        return doPostAsync("get", request, ServiceConfiguration.class);
    }

    @Override
    public CompletableFuture<Boolean> add(final AddConfigRequest request) {
        return doPutAsync("add", request, Boolean.class);
    }
}
