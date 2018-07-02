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


import org.mockito.Mockito;

import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.resource.service.ResourceService;
import uk.gov.gchq.palisade.resource.service.request.AddResourceRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByFormatRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByIdRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByResourceRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByTypeRequest;
import uk.gov.gchq.palisade.service.request.ConnectionDetail;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MockResourceService implements ResourceService {
    private static ResourceService mock = Mockito.mock(ResourceService.class);

    public static ResourceService getMock() {
        return mock;
    }

    public static void setMock(final ResourceService mock) {
        if (null == mock) {
            MockResourceService.mock = Mockito.mock(ResourceService.class);
        }
        MockResourceService.mock = mock;
    }

    @Override
    public CompletableFuture<Map<Resource, ConnectionDetail>> getResourcesByResource(final GetResourcesByResourceRequest request) {
        return mock.getResourcesByResource(request);
    }

    @Override
    public CompletableFuture<Map<Resource, ConnectionDetail>> getResourcesById(final GetResourcesByIdRequest request) {
        return mock.getResourcesById(request);
    }

    @Override
    public CompletableFuture<Map<Resource, ConnectionDetail>> getResourcesByType(final GetResourcesByTypeRequest request) {
        return mock.getResourcesByType(request);
    }

    @Override
    public CompletableFuture<Map<Resource, ConnectionDetail>> getResourcesByFormat(final GetResourcesByFormatRequest request) {
        return mock.getResourcesByFormat(request);
    }

    @Override
    public CompletableFuture<Boolean> addResource(final AddResourceRequest request) {
        return mock.addResource(request);
    }
}
