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

package uk.gov.gchq.palisade.redirect.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import uk.gov.gchq.palisade.cache.service.CacheService;
import uk.gov.gchq.palisade.cache.service.request.AddCacheRequest;
import uk.gov.gchq.palisade.cache.service.request.GetCacheRequest;
import uk.gov.gchq.palisade.redirect.RedirectionResult;
import uk.gov.gchq.palisade.redirect.exception.NoInstanceException;
import uk.gov.gchq.palisade.redirect.exception.RedirectionFailedException;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DummyRedirector extends HeartbeatRedirector<String> {

    @Override
    public RedirectionResult<String> redirectionFor(String host, Method method, Object... args) throws NoInstanceException, RedirectionFailedException {
        return null;
    }
}

public class HeartbeatRedirectorTest {
    private CacheService mockCache;
    private Method addMethod;
    private AddCacheRequest request;

    @Before
    public void setup() throws Exception {
        mockCache = Mockito.mock(CacheService.class);
        addMethod = CacheService.class.getMethod("add", AddCacheRequest.class);
        request = new AddCacheRequest();
    }

    @Test
    public void shouldCacheAddRequest() {
        //Given
        when(mockCache.add(any(AddCacheRequest.class))).thenReturn(CompletableFuture.completedFuture(Boolean.TRUE));

        DummyRedirector testObject = new DummyRedirector();
        testObject.cacheService(mockCache);

        //When
        testObject.logRedirect("host1", "host2", addMethod, null);

        //Then
        verify(mockCache).add(any());
    }

    @Test
    public void shouldComputeValidity() {
        //Given
        when(mockCache.get(any(GetCacheRequest.class))).thenReturn(CompletableFuture.completedFuture(Optional.of("test_instance")));

        DummyRedirector testObject = new DummyRedirector();
        testObject.cacheService(mockCache);

        //When
        boolean shouldBeInvalid = testObject.isRedirectionValid("some_host", "test_instance", addMethod, null);
        boolean shouldBeValid = testObject.isRedirectionValid("some_host", "test_other_instance", addMethod, null);

        //Then
        assertFalse(shouldBeInvalid);
        assertTrue(shouldBeValid);
    }

    @Test
    public void shouldBeValidWhenNoCache() {
        //Given
        when(mockCache.get(any(GetCacheRequest.class))).thenReturn(CompletableFuture.completedFuture(Optional.empty()));

        DummyRedirector testObject = new DummyRedirector();
        testObject.cacheService(mockCache);

        //When
        boolean shouldBeValid = testObject.isRedirectionValid("some_host", "test_instance", addMethod, null);

        //Then
        assertTrue(shouldBeValid);
    }
}
