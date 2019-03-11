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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import uk.gov.gchq.palisade.rest.EmbeddedHttpServer;
import uk.gov.gchq.palisade.service.CommonMetrics;
import uk.gov.gchq.palisade.service.PalisadeMetricProvider;
import uk.gov.gchq.palisade.service.request.GetMetricRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

public class RestPalisadeMetricProviderV1IT {

    private static ProxyRestPalisadeMetricProvider proxy;
    private static EmbeddedHttpServer server;

    @BeforeClass
    public static void beforeClass() throws IOException {
        RestPalisadeMetricProviderV1.setDefaultDelegate(new MockPalisadeMetricProvider());
        RestPalisadeServiceV1.setDefaultDelegate(new MockPalisadeService());
        proxy = (ProxyRestPalisadeMetricProvider) new ProxyRestPalisadeMetricProvider("http://localhost:8080/palisade").retryMax(1);
        server = new EmbeddedHttpServer(proxy.getBaseUrlWithVersion(), new ApplicationConfigV1());
        server.startServer();
    }

    @AfterClass
    public static void afterClass() {
        if (null != server) {
            server.stopServer();
        }
    }

    @Test
    public void shouldGetMetrics() throws IOException {
        // Given
        final PalisadeMetricProvider palisadeMetricProvider = Mockito.mock(PalisadeMetricProvider.class);
        MockPalisadeMetricProvider.setMock(palisadeMetricProvider);

        final GetMetricRequest request = new GetMetricRequest().addFilter("p*");

        final Map<String, String> expected = new HashMap<>();
        expected.put(CommonMetrics.CURRENT_RESOURCE_COUNT.getMetricName(), "23");
        given(palisadeMetricProvider.getMetrics(request)).willReturn(CompletableFuture.completedFuture(expected));

        // When
        final Map<String, String> result = proxy.getMetrics(request).join();

        // Then
        assertEquals(expected, result);
        verify(palisadeMetricProvider).getMetrics(request);
    }
}
