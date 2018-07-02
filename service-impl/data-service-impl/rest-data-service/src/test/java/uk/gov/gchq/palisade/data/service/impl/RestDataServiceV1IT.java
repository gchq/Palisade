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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.data.service.DataService;
import uk.gov.gchq.palisade.data.service.request.ReadRequest;
import uk.gov.gchq.palisade.data.service.request.ReadResponse;
import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.rest.EmbeddedHttpServer;
import uk.gov.gchq.palisade.service.request.ConnectionDetail;
import uk.gov.gchq.palisade.service.request.DataRequestResponse;
import uk.gov.gchq.palisade.service.request.SimpleConnectionDetail;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

public class RestDataServiceV1IT {

    private static ProxyRestDataService proxy;
    private static EmbeddedHttpServer server;

    @BeforeClass
    public static void beforeClass() throws IOException {
        System.setProperty(RestDataServiceV1.SERVICE_CONFIG, "mockConfig.json");
        proxy = new ProxyRestDataService("http://localhost:8084/data");
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
    public <T> void shouldRead() throws IOException {
        // Given
        final DataService dataService = Mockito.mock(DataService.class);
        MockDataService.setMock(dataService);

        final FileResource resource1 = new FileResource("file1");
        final FileResource resource2 = new FileResource("file1");
        final Map<Resource, ConnectionDetail> resources = new HashMap<>();
        resources.put(resource1, new SimpleConnectionDetail("details1"));
        resources.put(resource2, new SimpleConnectionDetail("details2"));

        final Stream<String> data = Stream.of("item1", "item2");
        final ReadRequest request = new ReadRequest(new DataRequestResponse(new RequestId("id1"), resources));

        final ReadResponse<String> expectedResult = new ReadResponse<>(data, "some message");
        final CompletableFuture futureExpectedResult = CompletableFuture.completedFuture(expectedResult);
        given(dataService.read(request)).willReturn(futureExpectedResult);

        // When
        final CompletableFuture<ReadResponse<String>> futureRead = proxy.read(request);
        final ReadResponse<String> result = futureRead.join();

        // Then
        assertEquals(expectedResult.getMessage(), result.getMessage());
        verify(dataService).read(request);
    }
}
