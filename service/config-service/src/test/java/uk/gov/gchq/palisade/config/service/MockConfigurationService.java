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

import org.mockito.Mockito;

import uk.gov.gchq.palisade.config.service.request.AddConfigRequest;
import uk.gov.gchq.palisade.config.service.request.GetConfigRequest;
import uk.gov.gchq.palisade.service.ServiceState;

import java.util.concurrent.CompletableFuture;

public class MockConfigurationService implements ConfigurationService {
    private static ConfigurationService mock = Mockito.mock(ConfigurationService.class);

    public static ConfigurationService getMock() {
        return mock;
    }

    public static void setMock(final ConfigurationService mock) {
        if (null == mock) {
            MockConfigurationService.mock = Mockito.mock(ConfigurationService.class);
        }
        MockConfigurationService.mock = mock;
    }

    @Override
    public CompletableFuture<ServiceState> get(final GetConfigRequest request) {
        return mock.get(request);
    }

    @Override
    public CompletableFuture<Boolean> add(final AddConfigRequest request) {
        return mock.add(request);
    }
}
