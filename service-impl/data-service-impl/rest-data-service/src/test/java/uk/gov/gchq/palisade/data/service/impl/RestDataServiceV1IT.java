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

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.data.service.DataService;
import uk.gov.gchq.palisade.data.service.request.ReadRequest;
import uk.gov.gchq.palisade.data.service.request.ReadResponse;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.rest.EmbeddedHttpServer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertArrayEquals;
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

        final FileResource resource = new FileResource().id("file1");
        final byte[] data = "value1\nvalue2".getBytes();
        final InputStream dataStream = new ByteArrayInputStream(data);
        final ReadRequest request = new ReadRequest().requestId(new RequestId().id("id1")).resource(resource);

        final ReadResponse expectedResult = new ReadResponse().data(dataStream).message("some message");
        final CompletableFuture futureExpectedResult = CompletableFuture.completedFuture(expectedResult);
        given(dataService.read(request)).willReturn(futureExpectedResult);

        // When
        final CompletableFuture<ReadResponse> futureRead = proxy.read(request);
        final ReadResponse result = futureRead.join();

        // Then
        assertArrayEquals(data, IOUtils.toByteArray(result.getData()));
        verify(dataService).read(request);
    }
}
