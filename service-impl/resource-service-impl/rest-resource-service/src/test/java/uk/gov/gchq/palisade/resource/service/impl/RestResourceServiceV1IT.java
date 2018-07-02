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

package uk.gov.gchq.palisade.resource.service.impl;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import uk.gov.gchq.palisade.resource.ContainerResource;
import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.resource.impl.DirectoryResource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.service.ResourceService;
import uk.gov.gchq.palisade.resource.service.request.AddResourceRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByFormatRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByIdRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByResourceRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByTypeRequest;
import uk.gov.gchq.palisade.rest.EmbeddedHttpServer;
import uk.gov.gchq.palisade.service.request.ConnectionDetail;
import uk.gov.gchq.palisade.service.request.SimpleConnectionDetail;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

public class RestResourceServiceV1IT {

    private static ProxyRestResourceService proxy;
    private static EmbeddedHttpServer server;

    @BeforeClass
    public static void beforeClass() throws IOException {
        System.setProperty(RestResourceServiceV1.SERVICE_CONFIG, "mockConfig.json");
        proxy = new ProxyRestResourceService("http://localhost:8082/resource");
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
    public void shouldAddResource() throws IOException {
        // Given
        final ResourceService resourceService = Mockito.mock(ResourceService.class);
        MockResourceService.setMock(resourceService);

        final ContainerResource dir = new DirectoryResource("dir1", "type1", "format1");
        final FileResource file = new FileResource("file1", "type1", "format1");
        final ConnectionDetail connectionDetail = new SimpleConnectionDetail("details");
        final AddResourceRequest request = new AddResourceRequest(dir, file, connectionDetail);

        given(resourceService.addResource(request)).willReturn(CompletableFuture.completedFuture(true));

        // When
        final Boolean result = proxy.addResource(request).join();

        // Then
        assertTrue(result);
        verify(resourceService).addResource(request);
    }

    @Test
    public void shouldGetResourcesByResource() throws IOException {
        // Given
        final ResourceService resourceService = Mockito.mock(ResourceService.class);
        MockResourceService.setMock(resourceService);

        final GetResourcesByResourceRequest request = new GetResourcesByResourceRequest(new DirectoryResource("dir1"));

        final Map<Resource, ConnectionDetail> expectedResult = new HashMap<>();
        expectedResult.put(new FileResource("file1"), new SimpleConnectionDetail("details1"));
        expectedResult.put(new FileResource("file2"), new SimpleConnectionDetail("details2"));

        given(resourceService.getResourcesByResource(request)).willReturn(CompletableFuture.completedFuture(expectedResult));

        // When
        final Map<Resource, ConnectionDetail> result = proxy.getResourcesByResource(request).join();

        // Then
        assertEquals(expectedResult, result);
        verify(resourceService).getResourcesByResource(request);
    }

    @Test
    public void shouldGetResourcesById() throws IOException {
        // Given
        final ResourceService resourceService = Mockito.mock(ResourceService.class);
        MockResourceService.setMock(resourceService);

        final GetResourcesByIdRequest request = new GetResourcesByIdRequest("file1");

        final Map<Resource, ConnectionDetail> expectedResult = new HashMap<>();
        expectedResult.put(new FileResource("file1"), new SimpleConnectionDetail("details"));

        given(resourceService.getResourcesById(request)).willReturn(CompletableFuture.completedFuture(expectedResult));

        // When
        final Map<Resource, ConnectionDetail> result = proxy.getResourcesById(request).join();

        // Then
        assertEquals(expectedResult, result);
        verify(resourceService).getResourcesById(request);
    }

    @Test
    public void shouldGetResourcesByType() throws IOException {
        // Given
        final ResourceService resourceService = Mockito.mock(ResourceService.class);
        MockResourceService.setMock(resourceService);

        final GetResourcesByTypeRequest request = new GetResourcesByTypeRequest("type1");

        final Map<Resource, ConnectionDetail> expectedResult = new HashMap<>();
        expectedResult.put(new FileResource("file1", "type1", "format1"), new SimpleConnectionDetail("details"));

        given(resourceService.getResourcesByType(request)).willReturn(CompletableFuture.completedFuture(expectedResult));

        // When
        final Map<Resource, ConnectionDetail> result = proxy.getResourcesByType(request).join();

        // Then
        assertEquals(expectedResult, result);
        verify(resourceService).getResourcesByType(request);
    }

    @Test
    public void shouldGetResourcesByFormat() throws IOException {
        // Given
        final ResourceService resourceService = Mockito.mock(ResourceService.class);
        MockResourceService.setMock(resourceService);

        final GetResourcesByFormatRequest request = new GetResourcesByFormatRequest("format1");

        final Map<Resource, ConnectionDetail> expectedResult = new HashMap<>();
        expectedResult.put(new FileResource("file1", "type1", "format1"), new SimpleConnectionDetail("details"));

        given(resourceService.getResourcesByFormat(request)).willReturn(CompletableFuture.completedFuture(expectedResult));

        // When
        final Map<Resource, ConnectionDetail> result = proxy.getResourcesByFormat(request).join();

        // Then
        assertEquals(expectedResult, result);
        verify(resourceService).getResourcesByFormat(request);
    }
}
