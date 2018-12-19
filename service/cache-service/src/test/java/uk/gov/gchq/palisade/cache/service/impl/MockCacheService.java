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
import uk.gov.gchq.palisade.cache.service.request.ListCacheRequest;
import uk.gov.gchq.palisade.cache.service.request.RemoveCacheRequest;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

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
    public <V> CompletableFuture<Boolean> add(AddCacheRequest<V> request) {
        return mock.add(request);
    }

    @Override
    public <V> CompletableFuture<Optional<V>> get(GetCacheRequest<V> request) {
        return mock.get(request);
    }

    @Override
    public CompletableFuture<Stream<String>> list(ListCacheRequest request) {
        return mock.list(request);
    }

    @Override
    public CompletableFuture<Boolean> remove(RemoveCacheRequest request) {
        return mock.remove(request);
    }
}
