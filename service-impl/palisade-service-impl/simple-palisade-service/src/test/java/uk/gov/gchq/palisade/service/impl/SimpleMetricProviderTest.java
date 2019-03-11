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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import uk.gov.gchq.palisade.cache.service.CacheService;
import uk.gov.gchq.palisade.cache.service.request.ListCacheRequest;
import uk.gov.gchq.palisade.service.CommonMetrics;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class SimpleMetricProviderTest {

    private static CacheService mockCache = Mockito.mock(CacheService.class);

    @Before
    public void setCache() {
        when(mockCache.list(any(ListCacheRequest.class))).thenReturn(CompletableFuture.completedFuture(Stream.of(SimplePalisadeService.RES_COUNT_KEY + "123567890_5",
                SimplePalisadeService.RES_COUNT_KEY + "123567890_5",
                SimplePalisadeService.RES_COUNT_KEY + "123567890_5",
                SimplePalisadeService.RES_COUNT_KEY + "123567890_bad-integer",
                SimplePalisadeService.RES_COUNT_KEY + "123567890_6")));
    }

    @Test
    public void shouldGetResourceCount() {
        //Given
        SimpleMetricProvider metrics = new SimpleMetricProvider(mockCache);

        //When
        String actual = metrics.getNumberResourcesBeingRequested();

        //Then
        assertThat(actual, is(equalTo("21")));
    }

    @Test
    public void shouldComputeNothing() {
        //Given
        SimpleMetricProvider metrics = new SimpleMetricProvider(mockCache);

        //When
        Map<String, String> actual = metrics.computeMetrics(Collections.emptyList());

        //Then
        assertTrue(actual.isEmpty());
    }

    @Test
    public void shouldComputeMapOfSingleEntry() {
        //Given
        SimpleMetricProvider metrics = new SimpleMetricProvider(mockCache);
        Map<String, String> expected = new HashMap<>();
        expected.put(CommonMetrics.CURRENT_RESOURCE_COUNT.getMetricName(), "21");

        //When
        Map<String, String> actual = metrics.computeMetrics(Collections.singletonList(CommonMetrics.CURRENT_RESOURCE_COUNT.getMetricName()));

        //Then
        assertThat(actual, is(equalTo(expected)));
    }

    @Test
    public void shouldComputeEmptyMapFromFilter() {
        //Given
        SimpleMetricProvider metrics = new SimpleMetricProvider(mockCache);
        Map<String, String> expected = new HashMap<>();

        //When
        Map<String, String> actual = metrics.computeMetrics(Collections.singletonList(CommonMetrics.CURRENT_RESOURCE_COUNT.getMetricName() + "_test"));

        //Then
        assertThat(actual, is(equalTo(expected)));
    }

    @Test
    public void shouldComputeSingleMapFromWildcard() {
        //Given
        SimpleMetricProvider metrics = new SimpleMetricProvider(mockCache);
        Map<String, String> expected = new HashMap<>();
        expected.put(CommonMetrics.CURRENT_RESOURCE_COUNT.getMetricName(), "21");

        //When
        Map<String, String> actual = metrics.computeMetrics(Collections.singletonList(CommonMetrics.CURRENT_RESOURCE_COUNT.getMetricName().substring(0, 4) + "*"));

        //Then
        assertThat(actual, is(equalTo(expected)));
    }
}
