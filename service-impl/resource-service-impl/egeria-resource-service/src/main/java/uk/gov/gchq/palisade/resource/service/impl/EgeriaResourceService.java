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
import org.odpi.openmetadata.frameworks.connectors.ffdc.InvalidParameterException;
import org.odpi.openmetadata.frameworks.connectors.properties.AdditionalProperties;
import org.odpi.openmetadata.frameworks.connectors.properties.AssetUniverse;
import org.odpi.openmetadata.frameworks.connectors.properties.beans.CommentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.data.service.impl.ProxyRestDataService;
import uk.gov.gchq.palisade.exception.NoConfigException;
import uk.gov.gchq.palisade.resource.ChildResource;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.impl.DirectoryResource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.impl.SystemResource;
import uk.gov.gchq.palisade.resource.service.ResourceService;
import uk.gov.gchq.palisade.resource.service.request.AddResourceRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByIdRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByResourceRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesBySerialisedFormatRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByTypeRequest;
import uk.gov.gchq.palisade.rest.ProxyRestConnectionDetail;
import uk.gov.gchq.palisade.rest.ProxyRestService;
import uk.gov.gchq.palisade.service.ConnectionDetail;
import uk.gov.gchq.palisade.service.ServiceState;

import java.io.File;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

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
    private static final String SERVER_KEY = "egeriaServer";
    private static final String SERVER_URL_KEY = "egeriaServerURL";
    private final int pageSize = 10;

    public EgeriaResourceService() {
    }

    @JsonCreator
    public EgeriaResourceService(@JsonProperty("egeriaServer") final String egeriaServer, @JsonProperty("egeriaServerURL") final String egeriaServerURL) throws RuntimeException {
        createAssetConsumer(egeriaServer, egeriaServerURL);
    }

    private void createAssetConsumer(final String egeriaServer, final String egeriaServerURL) throws RuntimeException {
        try {
            assetConsumer = new AssetConsumer(egeriaServer, egeriaServerURL);
            this.egeriaServer = egeriaServer;
            this.egeriaServerURL = egeriaServerURL;
        } catch (InvalidParameterException e) {
            LOGGER.error("Cast EgeriaException ({}) to RuntimeException", e.getClass().toString());
            throw new RuntimeException(e.getMessage());
        }
    }

    Stream<String> pageAssets(final Function<Integer, List<String>> pager) {
        List<String> assets = new ArrayList<>();
        int startFrom = 0;
        List<String> page;
        while (!(page = pager.apply(startFrom)).isEmpty()) {
            assets.addAll(page);
            startFrom += pageSize;
        }
        return assets.stream();
    }

    Stream<AssetUniverse> asAssetUniverse(final Stream<String> assets, final UserId userId) {
        return assets.map(guid -> {
                try {
                    return assetConsumer.getAssetProperties(userId.getId(), guid);
                } catch (Throwable e) {
                    LOGGER.error("Exception while getting properties for asset {}:", guid);
                    LOGGER.error(e.getMessage());
                    return null;
                }
            })
        .filter(Objects::nonNull);
    }

    Stream<AssetUniverse> tagWithMetadata(final Stream<AssetUniverse> assets, final UserId userId, final String comment) {
        return assets.peek(asset -> {
                try {
                    assetConsumer.addCommentToAsset(userId.getId(), asset.getGUID(), CommentType.STANDARD_COMMENT, comment, true);
                    LOGGER.debug("Added comment {} to asset {}", comment, asset.getGUID());
                } catch (Throwable e) {
                    LOGGER.warn("Exception while adding comment to asset {}:", asset.getGUID());
                    LOGGER.warn(e.getMessage());
                }
            });
    }

    Stream<SimpleEntry<AssetUniverse, ConnectionDetail>> withConnectionDetails(final Stream<AssetUniverse> assets, final UserId userId) {
        return assets.map(asset -> {
                ConnectionDetail connectionDetail = null;
                try {
                    AdditionalProperties props = asset.getAdditionalProperties();
                    Class<? extends ProxyRestService> serviceClass = (Class<? extends ProxyRestService>) Class.forName(props.getProperty("proxyRestServiceClass"));
                    String url = props.getProperty("proxyRestServiceUrl");
                    connectionDetail = new ProxyRestConnectionDetail().serviceClass(serviceClass).url(url);
                } catch (Throwable e) {
                    LOGGER.warn("Exception while getting connection details for asset {}:", asset.getGUID());
                    LOGGER.warn(e.getMessage());
                }
                return new SimpleEntry<>(asset, connectionDetail);
            });
    }

    static ChildResource resolveParents(final ChildResource resource) {
        String path = resource.getId();
        if (path.endsWith(File.separator)) {
            path = path.substring(0, path.length() - 1);
        }
        String rootPath = path.substring(0, path.indexOf(File.separator));
        String parentPath = path.substring(0, path.lastIndexOf(File.separator));
        if (parentPath.equals(rootPath)) {
            SystemResource root = new SystemResource().id(parentPath);
            resource.setParent(root);
        } else {
            DirectoryResource parent = new DirectoryResource().id(parentPath);
            resolveParents(parent);
            resource.setParent(parent);
        }
        return resource;
    }

    CompletableFuture<Map<LeafResource, ConnectionDetail>> asResourceMap(final Stream<SimpleEntry<AssetUniverse, ConnectionDetail>> assetConnections) {
        return CompletableFuture.supplyAsync(() -> assetConnections
                .filter(entry -> !entry.getKey().getAssetTypeName().equals("FileFolder"))
                .collect(Collectors.toMap(
                        entry -> {
                            AssetUniverse asset = entry.getKey();
                            String id = asset.getQualifiedName().replace("//", "///");
                            String serialisedFormat = asset.getQualifiedName().substring(asset.getQualifiedName().lastIndexOf(".") + 1);
                            String type = asset.getQualifiedName().substring(asset.getQualifiedName().lastIndexOf(File.separator) + 1, asset.getQualifiedName().indexOf(".")).toLowerCase();
                            FileResource resource = new FileResource().id(id).serialisedFormat(serialisedFormat).type(type);
                            return (FileResource) resolveParents(resource);
                        },
                        entry -> {
                            ConnectionDetail connectionDetail = entry.getValue();
                            if (Objects.isNull(connectionDetail)) {
                                Class<? extends ProxyRestService> serviceClass = ProxyRestDataService.class;
                                String url = "http://data-service:8080/data";
                                connectionDetail = new ProxyRestConnectionDetail().serviceClass(serviceClass).url(url);
                                LOGGER.warn("ConnectionDetail for asset {} was null, using default: proxyRestServiceClass={}, proxyRestServiceUrl={}",
                                        entry.getKey().getGUID(), serviceClass.getName(), url);
                            }
                            return connectionDetail;
                        }
                    ))
        );
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
        throw new UnsupportedOperationException();
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
        Function<Integer, List<String>> pager = (start) -> {
            List<String> page = null;
            try {
                page = this.assetConsumer.findAssets(request.getUserId().getId(), request.getResourceId().replace("///", "//"), start, this.pageSize);
            } catch (Throwable e) {
                LOGGER.debug("Exception while paging (is the paging response expected to be empty?):");
                LOGGER.debug(e.getMessage());
            }
            return Objects.nonNull(page) ? page : Collections.emptyList();
        };
        final UserId userId = request.getUserId();

        // Get all asset guids
        final Stream<String> assets = pageAssets(pager);

        // Filter out inaccessible assets
        final Stream<AssetUniverse> availableAssets = asAssetUniverse(assets, userId);

        // Audit use of assets with Egeria metadata tagging
        final Stream<AssetUniverse> auditedAssets = tagWithMetadata(availableAssets, userId, request.toString());

        // Get connectors for assets
        final Stream<SimpleEntry<AssetUniverse, ConnectionDetail>> assetConnectors = withConnectionDetails(auditedAssets, userId);

        // Return LeafResource-ConnectionDetail map for data service
        return asResourceMap(assetConnectors);
    }

    /**
     * Obtain a list of resources that match a specific resource type. This method allows a client to obtain potentially
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
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }

    @Override
    public void applyConfigFrom(final ServiceState config) throws NoConfigException {
        requireNonNull(config, "config");
        String egeriaServer = config.getOrDefault(SERVER_KEY, "cocoMDS1");
        String egeriaServerURL = config.getOrDefault(SERVER_URL_KEY, "http://localhost/egeria");
        createAssetConsumer(egeriaServer, egeriaServerURL);
    }

    @Override
    public void recordCurrentConfigTo(final ServiceState config) {
        requireNonNull(config, "config");
        config.put(ResourceService.class.getTypeName(), getClass().getTypeName());
        config.put(SERVER_KEY, egeriaServer);
        config.put(SERVER_URL_KEY, egeriaServerURL);
    }
}
