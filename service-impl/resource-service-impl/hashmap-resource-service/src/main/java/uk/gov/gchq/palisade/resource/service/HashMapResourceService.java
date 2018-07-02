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

package uk.gov.gchq.palisade.resource.service;

import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.resource.service.request.AddResourceRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByFormatRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByIdRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByResourceRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByTypeRequest;
import uk.gov.gchq.palisade.service.request.ConnectionDetail;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * A HashMapResourceService is a  simple implementation of a {@link ResourceService}
 * that simply stores the resources in a {@link ConcurrentHashMap}. More
 * precisely it uses multiple maps to allow the resources to be indexed by
 * resource, id, type and format.
 * </p>
 * <p>
 * By default the map is static so it will be shared across the same JVM.
 * </p>
 */
public class HashMapResourceService implements ResourceService {
    private static final Map<Resource, Map<Resource, ConnectionDetail>> RES_TO_RES = new ConcurrentHashMap<>();
    private static final Map<String, Map<Resource, ConnectionDetail>> ID_TO_RES = new ConcurrentHashMap<>();
    private static final Map<String, Map<Resource, ConnectionDetail>> TYPE_TO_RES = new ConcurrentHashMap<>();
    private static final Map<String, Map<Resource, ConnectionDetail>> FORMAT_TO_RES = new ConcurrentHashMap<>();

    private final Map<Resource, Map<Resource, ConnectionDetail>> resourceToResources;
    private final Map<String, Map<Resource, ConnectionDetail>> idToResources;
    private final Map<String, Map<Resource, ConnectionDetail>> typeToResources;
    private final Map<String, Map<Resource, ConnectionDetail>> formatToResources;

    public HashMapResourceService() {
        this(true);
    }

    public HashMapResourceService(final boolean useStatic) {
        if (useStatic) {
            resourceToResources = RES_TO_RES;
            idToResources = ID_TO_RES;
            typeToResources = TYPE_TO_RES;
            formatToResources = FORMAT_TO_RES;
        } else {
            resourceToResources = new ConcurrentHashMap<>();
            idToResources = new ConcurrentHashMap<>();
            typeToResources = new ConcurrentHashMap<>();
            formatToResources = new ConcurrentHashMap<>();
        }
    }

    @Override
    public CompletableFuture<Map<Resource, ConnectionDetail>> getResourcesByResource(final GetResourcesByResourceRequest request) {
        Map<Resource, ConnectionDetail> result = resourceToResources.get(request.getResource());
        if (null == result) {
            result = Collections.emptyMap();
        }
        return CompletableFuture.completedFuture(result);
    }

    @Override
    public CompletableFuture<Map<Resource, ConnectionDetail>> getResourcesById(final GetResourcesByIdRequest request) {
        Map<Resource, ConnectionDetail> result = idToResources.get(request.getResourceId());
        if (null == result) {
            result = Collections.emptyMap();
        }
        return CompletableFuture.completedFuture(result);
    }

    @Override
    public CompletableFuture<Map<Resource, ConnectionDetail>> getResourcesByType(final GetResourcesByTypeRequest request) {
        Map<Resource, ConnectionDetail> result = typeToResources.get(request.getType());
        if (null == result) {
            result = Collections.emptyMap();
        }
        return CompletableFuture.completedFuture(result);
    }

    @Override
    public CompletableFuture<Map<Resource, ConnectionDetail>> getResourcesByFormat(final GetResourcesByFormatRequest request) {
        Map<Resource, ConnectionDetail> result = formatToResources.get(request.getFormat());
        if (null == result) {
            result = Collections.emptyMap();
        }
        return CompletableFuture.completedFuture(result);
    }

    @Override
    public CompletableFuture<Boolean> addResource(final AddResourceRequest request) {
        indexResource(request.getResource(), request.getResource(), request.getConnectionDetail(), resourceToResources);
        indexResource(request.getResource().getId(), request.getResource(), request.getConnectionDetail(), idToResources);
        indexResource(request.getResource().getType(), request.getResource(), request.getConnectionDetail(), typeToResources);
        indexResource(request.getResource().getFormat(), request.getResource(), request.getConnectionDetail(), formatToResources);

        indexResource(request.getContainer(), request.getResource(), request.getConnectionDetail(), resourceToResources);
        indexResource(request.getContainer().getId(), request.getResource(), request.getConnectionDetail(), idToResources);
        indexResource(request.getContainer().getType(), request.getResource(), request.getConnectionDetail(), typeToResources);
        indexResource(request.getContainer().getFormat(), request.getResource(), request.getConnectionDetail(), formatToResources);
        return CompletableFuture.completedFuture(true);
    }

    public void setResources(final List<AddResourceRequest> resources) {
        resourceToResources.clear();
        idToResources.clear();
        typeToResources.clear();
        formatToResources.clear();
        resources.forEach(this::addResource);
    }

    private <T> void indexResource(final T index, final Resource resource, final ConnectionDetail connectionDetail, final Map<T, Map<Resource, ConnectionDetail>> map) {
        if (null != index) {
            Map<Resource, ConnectionDetail> resources = map.get(index);
            if (null == resources) {
                resources = new HashMap<>();
                map.put(index, resources);
            }
            resources.put(resource, connectionDetail);
        }
    }
}
