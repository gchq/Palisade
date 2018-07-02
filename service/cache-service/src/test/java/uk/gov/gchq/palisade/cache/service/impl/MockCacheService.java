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

package uk.gov.gchq.palisade.cache.service.impl;


import org.mockito.Mockito;

import uk.gov.gchq.palisade.cache.service.CacheService;
import uk.gov.gchq.palisade.cache.service.request.AddCacheRequest;
import uk.gov.gchq.palisade.cache.service.request.GetCacheRequest;
import uk.gov.gchq.palisade.service.request.DataRequestConfig;

import java.util.concurrent.CompletableFuture;

public class MockCacheService implements CacheService {
    private static CacheService mock = Mockito.mock(CacheService.class);

    public static CacheService getMock() {
        return mock;
    }

    public static void setMock(final CacheService mock) {
        if (null == mock) {
            MockCacheService.mock = Mockito.mock(CacheService.class);
        }
        MockCacheService.mock = mock;
    }


    @Override
    public CompletableFuture<Boolean> add(final AddCacheRequest request) {
        return mock.add(request);
    }

    @Override
    public CompletableFuture<DataRequestConfig> get(final GetCacheRequest request) {
        return mock.get(request);
    }
}
