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
import uk.gov.gchq.palisade.cache.service.heart.HeartUtil;
import uk.gov.gchq.palisade.cache.service.request.AddCacheRequest;
import uk.gov.gchq.palisade.cache.service.request.GetCacheRequest;
import uk.gov.gchq.palisade.redirect.RedirectionResult;
import uk.gov.gchq.palisade.redirect.exception.NoInstanceException;
import uk.gov.gchq.palisade.redirect.result.StringRedirectResult;
import uk.gov.gchq.palisade.service.Service;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

class TestService implements Service {
}

class NoLiveService implements Service {
}

public class SimpleRandomRedirectorTest {

    private CacheService mockCache;
    private Method addMethod;
    private AddCacheRequest request;

    @Before
    public void setup() throws Exception {
        mockCache = Mockito.mock(CacheService.class);
        addMethod = CacheService.class.getMethod("add", AddCacheRequest.class);
        request = new AddCacheRequest();
    }

    @Test(expected = NoInstanceException.class)
    public void throwOnNoLiveInstance() {
        //Given
        when(mockCache.list(any())).thenReturn(CompletableFuture.completedFuture(Stream.empty()));
        SimpleRandomRedirector redirect = new SimpleRandomRedirector();
        redirect.cacheService(mockCache)
                .redirectionClass(NoLiveService.class);

        //When
        redirect.redirectionFor(null, addMethod, request);

        //Then
        fail("exception expected");
    }

    @Test
    public void shouldReturnLiveInstance() {
        //Given
        when(mockCache.list(any())).thenReturn(CompletableFuture.completedFuture(Stream.of(HeartUtil.makeKey("test_instance"))));
        when(mockCache.get(any(GetCacheRequest.class))).thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        when(mockCache.add(any(AddCacheRequest.class))).thenReturn(CompletableFuture.completedFuture(Boolean.TRUE));

        SimpleRandomRedirector redirect = new SimpleRandomRedirector();
        redirect.cacheService(mockCache)
                .redirectionClass(TestService.class);

        RedirectionResult<String> expected = new StringRedirectResult("test_instance");

        //When
        RedirectionResult<String> actual = redirect.redirectionFor(null, addMethod, request);

        //Then
        assertThat(actual, is(equalTo(expected)));
    }
}
