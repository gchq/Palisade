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

import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.data.service.impl.MockDataService;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.impl.SystemResource;
import uk.gov.gchq.palisade.resource.service.ResourceService;
import uk.gov.gchq.palisade.resource.service.request.AddResourceRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByIdRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByResourceRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesBySerialisedFormatRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByTypeRequest;
import uk.gov.gchq.palisade.rest.EmbeddedHttpServer;
import uk.gov.gchq.palisade.service.ConnectionDetail;
import uk.gov.gchq.palisade.service.request.StubConnectionDetail;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class RestResourceServiceV1IT {

    private static ProxyRestResourceService proxy;
    private static EmbeddedHttpServer server;

    private static final SystemResource sysResource = new SystemResource().id("file");

    @BeforeClass
    public static void beforeClass() throws IOException {
        String portNumber = System.getProperty("restResourceServicePort");
        RestResourceServiceV1.setDefaultDelegate(new MockResourceService());
        proxy = (ProxyRestResourceService) new ProxyRestResourceService("http://localhost:"+portNumber+"/resource").retryMax(1);
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
        final ResourceService resourceService = mock(ResourceService.class);
        MockResourceService.setMock(resourceService);

        final FileResource file = new FileResource().id("file1").type("type1").serialisedFormat("format1").parent(sysResource);


        final AddResourceRequest request = new AddResourceRequest();

        request.resource(file).connectionDetail(new StubConnectionDetail().setServiceToCreate(new MockDataService()));

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
        final ResourceService resourceService = mock(ResourceService.class);
        MockResourceService.setMock(resourceService);

        final GetResourcesByResourceRequest request = new GetResourcesByResourceRequest().resource(sysResource);
        request.setOriginalRequestId(new RequestId().id("test shouldGetResourcesByResource"));

        final Map<LeafResource, ConnectionDetail> expectedResult = new HashMap<>();
        expectedResult.put(new FileResource().id("file1").type("type1").serialisedFormat("format1").parent(sysResource), new StubConnectionDetail().setServiceToCreate(new MockDataService()));
        expectedResult.put(new FileResource().id("file2").type("type1").serialisedFormat("format1").parent(sysResource), new StubConnectionDetail().setServiceToCreate(new MockDataService()));

        given(resourceService.getResourcesByResource(request)).willReturn(CompletableFuture.completedFuture(expectedResult));

        // When
        final Map<LeafResource, ConnectionDetail> result = proxy.getResourcesByResource(request).join();

        // Then
        assertEquals(expectedResult, result);
        verify(resourceService).getResourcesByResource(request);
    }

    @Test
    public void shouldGetResourcesById() throws IOException {
        // Given
        final ResourceService resourceService = mock(ResourceService.class);
        MockResourceService.setMock(resourceService);

        final GetResourcesByIdRequest request = new GetResourcesByIdRequest().resourceId("file1");
        request.setOriginalRequestId(new RequestId().id("test shouldGetResourceById"));

        final Map<LeafResource, ConnectionDetail> expectedResult = new HashMap<>();
        expectedResult.put(new FileResource().id("file1").type("testType").serialisedFormat("testFormat").parent(sysResource), new StubConnectionDetail().setServiceToCreate(new MockDataService()));

        given(resourceService.getResourcesById(request)).willReturn(CompletableFuture.completedFuture(expectedResult));

        // When
        final Map<LeafResource, ConnectionDetail> result = proxy.getResourcesById(request).join();

        // Then
        assertEquals(expectedResult, result);
        verify(resourceService).getResourcesById(request);
    }

    @Test
    public void shouldGetResourcesByType() throws IOException {
        // Given
        final ResourceService resourceService = mock(ResourceService.class);
        MockResourceService.setMock(resourceService);

        final GetResourcesByTypeRequest request = new GetResourcesByTypeRequest().type("type1");
        request.setOriginalRequestId(new RequestId().id("test shouldGetResourcesByType"));


        final Map<LeafResource, ConnectionDetail> expectedResult = new HashMap<>();
        expectedResult.put(new FileResource().id("file1").type("type1").serialisedFormat("format1").parent(sysResource), new StubConnectionDetail().setServiceToCreate(new MockDataService()));

        given(resourceService.getResourcesByType(request)).willReturn(CompletableFuture.completedFuture(expectedResult));

        // When
        final Map<LeafResource, ConnectionDetail> result = proxy.getResourcesByType(request).join();

        // Then
        assertEquals(expectedResult, result);
        verify(resourceService).getResourcesByType(request);
    }

    @Test
    public void shouldGetResourcesByFormat() throws IOException {
        // Given
        final ResourceService resourceService = mock(ResourceService.class);
        MockResourceService.setMock(resourceService);

        final GetResourcesBySerialisedFormatRequest request = new GetResourcesBySerialisedFormatRequest().serialisedFormat("format1");
        request.setOriginalRequestId(new RequestId().id("test shouldGetResourcesByFormat"));

        final Map<LeafResource, ConnectionDetail> expectedResult = new HashMap<>();
        expectedResult.put(new FileResource().id("file1").type("type1").serialisedFormat("format1").parent(sysResource), new StubConnectionDetail().setServiceToCreate(new MockDataService()));

        given(resourceService.getResourcesBySerialisedFormat(request)).willReturn(CompletableFuture.completedFuture(expectedResult));

        // When
        final Map<LeafResource, ConnectionDetail> result = proxy.getResourcesBySerialisedFormat(request).join();

        // Then
        assertEquals(expectedResult, result);
        verify(resourceService).getResourcesBySerialisedFormat(request);
    }
}
