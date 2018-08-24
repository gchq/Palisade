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

import uk.gov.gchq.palisade.resource.ChildResource;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.resource.service.request.AddResourceRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByIdRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByResourceRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesBySerialisedFormatRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByTypeRequest;
import uk.gov.gchq.palisade.service.request.ConnectionDetail;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.requireNonNull;

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
    private static final Map<Resource, Map<LeafResource, ConnectionDetail>> RES_TO_RES = new ConcurrentHashMap<>();
    private static final Map<String, Map<LeafResource, ConnectionDetail>> ID_TO_RES = new ConcurrentHashMap<>();
    private static final Map<String, Map<LeafResource, ConnectionDetail>> TYPE_TO_RES = new ConcurrentHashMap<>();
    private static final Map<String, Map<LeafResource, ConnectionDetail>> FORMAT_TO_RES = new ConcurrentHashMap<>();

    private final Map<Resource, Map<LeafResource, ConnectionDetail>> resourceToResources;
    private final Map<String, Map<LeafResource, ConnectionDetail>> idToResources;
    private final Map<String, Map<LeafResource, ConnectionDetail>> typeToResources;
    private final Map<String, Map<LeafResource, ConnectionDetail>> serialisedFormatToResources;

    public HashMapResourceService() {
        this(true);
    }

    public HashMapResourceService(final boolean useStatic) {
        if (useStatic) {
            resourceToResources = RES_TO_RES;
            idToResources = ID_TO_RES;
            typeToResources = TYPE_TO_RES;
            serialisedFormatToResources = FORMAT_TO_RES;
        } else {
            resourceToResources = new ConcurrentHashMap<>();
            idToResources = new ConcurrentHashMap<>();
            typeToResources = new ConcurrentHashMap<>();
            serialisedFormatToResources = new ConcurrentHashMap<>();
        }
    }

    @Override
    public CompletableFuture<Map<LeafResource, ConnectionDetail>> getResourcesByResource(final GetResourcesByResourceRequest request) {
        Map<LeafResource, ConnectionDetail> result = resourceToResources.get(request.getResource());
        if (null == result) {
            result = Collections.emptyMap();
        }
        return CompletableFuture.completedFuture(result);
    }

    @Override
    public CompletableFuture<Map<LeafResource, ConnectionDetail>> getResourcesById(final GetResourcesByIdRequest request) {
        Map<LeafResource, ConnectionDetail> result = idToResources.get(request.getResourceId());
        if (null == result) {
            result = Collections.emptyMap();
        }
        return CompletableFuture.completedFuture(result);
    }

    @Override
    public CompletableFuture<Map<LeafResource, ConnectionDetail>> getResourcesByType(final GetResourcesByTypeRequest request) {
        Map<LeafResource, ConnectionDetail> result = typeToResources.get(request.getType());
        if (null == result) {
            result = Collections.emptyMap();
        }
        return CompletableFuture.completedFuture(result);
    }

    @Override
    public CompletableFuture<Map<LeafResource, ConnectionDetail>> getResourcesBySerialisedFormat(final GetResourcesBySerialisedFormatRequest request) {
        Map<LeafResource, ConnectionDetail> result = serialisedFormatToResources.get(request.getSerialisedFormat());
        if (null == result) {

            result = Collections.emptyMap();
        }
        return CompletableFuture.completedFuture(result);
    }

    @Override
    public CompletableFuture<Boolean> addResource(final AddResourceRequest request) {
        LeafResource resource = request.getResource();
        ConnectionDetail connectionDetail = request.getConnectionDetail();

        indexResource(resource, resource, connectionDetail, resourceToResources);
        indexResource(resource.getId(), resource, connectionDetail, idToResources);
        indexResource(resource.getType(), resource, connectionDetail, typeToResources);
        indexResource(resource.getSerialisedFormat(), resource, connectionDetail, serialisedFormatToResources);

        recursiveAddParentResourceToIndex(resource.getParent(), resource, connectionDetail);

        return CompletableFuture.completedFuture(true);
    }

    private void recursiveAddParentResourceToIndex(final Resource parent, final LeafResource resource, final ConnectionDetail connectionDetail) {
        if (parent instanceof ChildResource) {
            recursiveAddParentResourceToIndex(((ChildResource) parent).getParent(), resource, connectionDetail);
        }
        indexResource(parent, resource, connectionDetail, resourceToResources);
        indexResource(parent.getId(), resource, connectionDetail, idToResources);
    }

    public HashMapResourceService resources(final List<AddResourceRequest> resources) {
        requireNonNull(resources, "The resources cannot be null.");
        resourceToResources.clear();
        idToResources.clear();
        typeToResources.clear();
        serialisedFormatToResources.clear();
        resources.forEach(this::addResource);
        return this;
    }

    public void setResources(final List<AddResourceRequest> resources) {
        resources(resources);
    }

    private <T> void indexResource(final T index, final LeafResource resource, final ConnectionDetail connectionDetail, final Map<T, Map<LeafResource, ConnectionDetail>> map) {
        if (null != index) {
            Map<LeafResource, ConnectionDetail> resources = map.computeIfAbsent(index, k -> new HashMap<>());
            resources.put(resource, connectionDetail);
        }
    }
}
