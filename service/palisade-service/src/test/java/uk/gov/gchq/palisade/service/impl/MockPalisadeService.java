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


import org.mockito.Mockito;

import uk.gov.gchq.palisade.service.PalisadeService;
import uk.gov.gchq.palisade.service.request.DataRequestConfig;
import uk.gov.gchq.palisade.service.request.DataRequestResponse;
import uk.gov.gchq.palisade.service.request.RegisterDataRequest;

import java.util.concurrent.CompletableFuture;

public class MockPalisadeService implements PalisadeService {
    private static PalisadeService mock = Mockito.mock(PalisadeService.class);

    public static PalisadeService getMock() {
        return mock;
    }

    public static void setMock(final PalisadeService mock) {
        if (null == mock) {
            MockPalisadeService.mock = Mockito.mock(PalisadeService.class);
        }
        MockPalisadeService.mock = mock;
    }

    @Override
    public CompletableFuture<DataRequestResponse> registerDataRequest(final RegisterDataRequest request) {
        return mock.registerDataRequest(request);
    }

    @Override
    public CompletableFuture<DataRequestConfig> getDataRequestConfig(final DataRequestResponse request) {
        return mock.getDataRequestConfig(request);
    }
}
