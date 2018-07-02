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


import org.mockito.Mockito;

import uk.gov.gchq.palisade.data.service.DataService;
import uk.gov.gchq.palisade.data.service.request.ReadRequest;
import uk.gov.gchq.palisade.data.service.request.ReadResponse;

import java.util.concurrent.CompletableFuture;

public class MockDataService implements DataService {
    private static DataService mock = Mockito.mock(DataService.class);

    public static DataService getMock() {
        return mock;
    }

    public static void setMock(final DataService mock) {
        if (null == mock) {
            MockDataService.mock = Mockito.mock(DataService.class);
        }
        MockDataService.mock = mock;
    }

    @Override
    public <T> CompletableFuture<ReadResponse<T>> read(final ReadRequest request) {
        return mock.read(request);
    }
}
