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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.odpi.openmetadata.accessservices.assetconsumer.client.AssetConsumer;
import org.odpi.openmetadata.frameworks.connectors.Connector;
import org.odpi.openmetadata.frameworks.connectors.ffdc.InvalidParameterException;
import org.odpi.openmetadata.frameworks.connectors.properties.AssetUniverse;
import org.odpi.openmetadata.frameworks.connectors.properties.beans.CommentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.data.service.impl.ProxyRestDataService;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.service.ResourceService;
import uk.gov.gchq.palisade.resource.service.request.AddResourceRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByIdRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByResourceRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesBySerialisedFormatRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByTypeRequest;
import uk.gov.gchq.palisade.rest.ProxyRestConnectionDetail;
import uk.gov.gchq.palisade.service.ConnectionDetail;
import uk.gov.gchq.palisade.service.request.Request;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * A implementation of the ResourceService for Hadoop.
 * <p>
 * This service is for the retrieval of Resources only. Resources cannot be added via this service, they should be added
 * through the actual real filing system.
 *
 * @see ResourceService
 */
public class EgeriaResourceService implements ResourceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EgeriaResourceService.class);
    private transient AssetConsumer assetConsumer;
    private String egeriaServer;
    private String egeriaServerURL;

    @JsonCreator
    public EgeriaResourceService(@JsonProperty("egeriaServer") final String egeriaServer, @JsonProperty("egeriaServerURL") final String egeriaServerURL) throws InvalidParameterException {
        assetConsumer = new AssetConsumer(egeriaServer, egeriaServerURL);
        this.egeriaServer = egeriaServer;
        this.egeriaServerURL = egeriaServerURL;
    }

    /**
     * Get a list of resources based on a specific resource. This allows for the retrieval of the appropriate {@link
     * ConnectionDetail}s for a given resource. It may also be used to retrieve the details all the resources that are
     * notionally children of another resource. For example, in a standard hierarchical filing system the files in a
     * directory could be considered child resources and calling this method on the directory resource would fetch the
     * details on the contained files.
     *
     * @param request the details of the resource to request
     * @return a {@link CompletableFuture} that upon completion will contain a map of how to retrieve the available
     * resources
     */
    @Override
    public CompletableFuture<Map<LeafResource, ConnectionDetail>> getResourcesByResource(final GetResourcesByResourceRequest request) {
        return null;
    }

    /**
     * Retrieve resource and connection details by resource ID. The request object allows the client to specify the
     * resource ID and obtain the connection details once the returned future has completed.
     *
     * @param request the details of which ID to request
     * @return a {@link CompletableFuture} that upon completion will contain details on how to retrieve the requested
     * resource.
     */
    public CompletableFuture<Map<LeafResource, ConnectionDetail>> getResourcesById(final GetResourcesByIdRequest request) {
        Map<AssetUniverse, Connector> fileArray = new HashMap<>();
        System.out.println(request.getResourceId());

        // Get all asset guids
        List<String> assetUniverse = new ArrayList<>();
        int pageSize = 10;
        final BiFunction<Integer, Integer, List<String>> pager = (start, size) -> {
            try {
                return assetConsumer.getAssetsByName(request.getUserId().getId(), request.getResourceId(), start, size);
            } catch (Throwable e) {
                return Collections.emptyList();
            }
        };
        for (int startFrom = 0; !pager.apply(startFrom, 1).isEmpty(); startFrom += pageSize) {
            assetUniverse.addAll(pager.apply(startFrom, pageSize));
        }

        // Audit use of assets and get connectors
        assetUniverse.forEach((guid) -> {
            try {
                AssetUniverse asset = assetConsumer.getAssetProperties(request.getUserId().getId(), guid);
                if (!asset.getAssetTypeName().equals("FileFolder")) {
                    Connector connector = assetConsumer.getConnectorForAsset(request.getUserId().getId(), guid);
                    String comment = String.format("User %s requested %s with request type %s", request.getUserId().getId(), request.getResourceId(), request.getClass().getName());
                    assetConsumer.addCommentToAsset(request.getUserId().getId(), guid, CommentType.STANDARD_COMMENT, comment, true);
                    fileArray.put(asset, connector);
                }
            } catch (Throwable e) {
                LOGGER.debug(e.toString());
            }
        });

        // Return LeafResource-ConnectionDetail map
        return CompletableFuture.supplyAsync(() -> fileArray.entrySet()
            .stream()
            .filter(entry -> !entry.getKey().getAssetTypeName().equals("FileFolder"))
            .collect(Collectors.toMap(
                entry -> {
                    AssetUniverse asset = entry.getKey();
                    String id = asset.getQualifiedName().substring(asset.getQualifiedName().lastIndexOf(":") + 2);
                    String serialisedFormat = asset.getQualifiedName().substring(asset.getQualifiedName().lastIndexOf(".") + 1);
                    String type = asset.getQualifiedName().substring(asset.getQualifiedName().lastIndexOf("/") + 1, asset.getQualifiedName().indexOf("."));
                    return new FileResource().id(id).serialisedFormat(serialisedFormat).type(type);
                },
                entry -> {
                    Connector connector = entry.getValue();
                    // TODO: Use proper information from Egeria rather than hardcoded
                    return new ProxyRestConnectionDetail().serviceClass(ProxyRestDataService.class).url("localhost/data");
                }
            ))
        );
    }


    /**
     * Obtain a list of resources that match a specifc resource type. This method allows a client to obtain potentially
     * large collections of resources by requesting all the resources of one particular type. For example, a client may
     * request all "employee contact card" records. Please note the warning in the class documentation above, that just
     * because a resource is available does not guarantee that the requesting client has the right to access it.
     *
     * @param request request object detailing the type of resource to retrieve.
     * @return {@link CompletableFuture} that upon completion will contain the connection details for all resources
     * matching a type
     */
    @Override
    public CompletableFuture<Map<LeafResource, ConnectionDetail>> getResourcesByType(
            final GetResourcesByTypeRequest request) {
        return null;
    }

    /**
     * Find all resources that match a particular data format. Resources of a particular data format may not share a
     * type, e.g. not all CSV format records will contain employee contact details. This method allows clients to
     * retrieve all the resources Palisade knows about that conform to one particular format. Note that this method can
     * potentially return large ${@code Map}s with many mappings.
     *
     * @param request the request detailing the specific format for retrieval
     * @return a {@link CompletableFuture} that upon completion will contain the details on how to retrieve the
     * resources
     */
    @Override
    public CompletableFuture<Map<LeafResource, ConnectionDetail>> getResourcesBySerialisedFormat(
            final GetResourcesBySerialisedFormatRequest request) {
        return null;
    }

    /**
     * Informs Palisade about a specific resource that it may return to users. This lets Palisade clients request access
     * to that resource and allows Palisade to provide policy controlled access to it via the other methods in this
     * interface.
     *
     * @param request details of the resource that Palisade can manage access to
     * @return a {@link CompletableFuture} that will complete as true once the resource has been added to this {@link
     * ResourceService}
     */
    @Override
    public CompletableFuture<Boolean> addResource(final AddResourceRequest request) {
        return null;
    }

    @Override
    public CompletableFuture<?> process(final Request request) {
        return null;
    }
}
