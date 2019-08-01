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
import uk.gov.gchq.palisade.data.service.exception.NoCapacityException;
import uk.gov.gchq.palisade.data.service.request.ClientReadResponse;
import uk.gov.gchq.palisade.data.service.request.ReadRequest;
import uk.gov.gchq.palisade.data.service.request.ReadResponse;
import uk.gov.gchq.palisade.exception.RequestFailedException;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.impl.SystemResource;
import uk.gov.gchq.palisade.rest.EmbeddedHttpServer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

public class RestDataServiceV1IT {

    private static ProxyRestDataService proxy;
    private static EmbeddedHttpServer server;

    private static final SystemResource sysResource = new SystemResource().id("File");
    private static final FileResource resource = new FileResource().type("type01").serialisedFormat("format01").parent(sysResource).id("file1");
    private static final ReadRequest request = new ReadRequest().requestId(new RequestId().id("id1")).resource(resource);

    @BeforeClass
    public static void beforeClass() throws IOException {
        String portNumber = System.getProperty("restDataServicePort");
        request.setOriginalRequestId(new RequestId().id("id1"));
        RestDataServiceV1.setDefaultDelegate(new MockDataService());
        proxy = (ProxyRestDataService) new ProxyRestDataService("http://localhost:" + portNumber + "/data").retryMax(1);
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
    public void shouldRead() throws IOException {
        // Given
        final DataService dataService = Mockito.mock(DataService.class);
        MockDataService.setMock(dataService);

        final byte[] data = "value1\nvalue2".getBytes();
        final InputStream dataStream = new ByteArrayInputStream(data);

        final ReadResponse expectedResult = new ClientReadResponse(dataStream).message("some message");
        final CompletableFuture futureExpectedResult = CompletableFuture.completedFuture(expectedResult);
        given(dataService.read(request)).willReturn(futureExpectedResult);

        // When
        final CompletableFuture<ReadResponse> futureRead = proxy.read(request);
        final ReadResponse result = futureRead.join();

        // Then
        assertArrayEquals(data, IOUtils.toByteArray(result.asInputStream()));
        verify(dataService).read(request);
    }

    @Test
    public void proxyServiceShouldThrowOriginalException() {
        // Given
        final DataService dataService = Mockito.mock(DataService.class);
        MockDataService.setMock(dataService);

        final String message = "some message";
        given(dataService.read(request)).willThrow(new IllegalArgumentException(message));

        // When / Then
        try {
            final CompletableFuture<ReadResponse> futureRead = proxy.read(request);
            futureRead.join();
            fail("Exception expected");
        } catch (final CompletionException e) {
            assertTrue("CompletionException cause should be an IllegalArgumentException",
                    e.getCause() instanceof IllegalArgumentException);
            assertEquals(message, e.getCause().getMessage());
        } catch (final IllegalArgumentException e) {
            assertEquals(message, e.getMessage());
        }
    }

    @Test(expected = NoCapacityException.class)
    public void throwNoCapacityException() {
        //Given
        final DataService dataService = Mockito.mock(DataService.class);
        MockDataService.setMock(dataService);

        given(dataService.read(request)).willThrow(new NoCapacityException("cannot serve request"));

        //When / Then
        try {
            ReadResponse response = proxy.read(request).join();

            fail("Exception expected");
        } catch (CompletionException e) {
            throw (RequestFailedException) e.getCause();
        }
    }
}
