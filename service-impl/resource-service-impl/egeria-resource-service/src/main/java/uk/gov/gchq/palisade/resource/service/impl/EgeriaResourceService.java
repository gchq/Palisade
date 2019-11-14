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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.odpi.openmetadata.accessservices.assetconsumer.client.AssetConsumer;
import org.odpi.openmetadata.frameworks.connectors.ffdc.InvalidParameterException;
import org.odpi.openmetadata.frameworks.connectors.ffdc.PropertyServerException;
import org.odpi.openmetadata.frameworks.connectors.ffdc.UserNotAuthorizedException;
import org.odpi.openmetadata.frameworks.connectors.properties.AssetUniverse;
import org.odpi.openmetadata.frameworks.connectors.properties.beans.CommentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.service.ResourceService;
import uk.gov.gchq.palisade.resource.service.request.AddResourceRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByIdRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByResourceRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesBySerialisedFormatRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByTypeRequest;
import uk.gov.gchq.palisade.service.ConnectionDetail;
import uk.gov.gchq.palisade.service.request.Request;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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

    @JsonIgnore
    private ResourceService proxyOf;

    public EgeriaResourceService() {
        proxyOf = new HadoopResourceService();
    }

    @JsonCreator
    public EgeriaResourceService(@JsonProperty("egeriServer") final String egeriaServer, @JsonProperty("egeriaServerURL") final String egeriaServerURL, @JsonProperty("hadoopResourceService") final HadoopResourceService hadoopResourceService) throws InvalidParameterException, IOException {
        assetConsumer = new AssetConsumer(egeriaServer, egeriaServerURL);
        if (hadoopResourceService != null) {
            proxyOf = hadoopResourceService;
        }
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
        List<AssetUniverse> fileArray = new ArrayList();
        System.out.println(request.getResourceId());

        try {
            List<String> assetUniverse = new ArrayList();
            assetUniverse.addAll(assetConsumer.getAssetsByName(request.getUserId().getId(), request.getResourceId(), 0, 10));
            assetUniverse.forEach((guid) -> {
                try {
                    AssetUniverse asset = assetConsumer.getAssetProperties(request.getUserId().getId(), guid);
                    if (!asset.getAssetTypeName().equals("FileFolder")) {
                        assetConsumer.addCommentToAsset(request.getUserId().getId(), guid, CommentType.STANDARD_COMMENT, "User " + request.getUserId().getId() + " is accessing this file", true);
                        fileArray.add(asset);
                    }
                } catch (InvalidParameterException e) {
                    LOGGER.debug("InvalidParameterException: " + e);
                } catch (PropertyServerException e) {
                    LOGGER.debug("PropertyServerException: " + e);
                } catch (UserNotAuthorizedException e) {
                    LOGGER.debug("UserNotAuthorizedException: " + e);
                }
            });
        } catch (InvalidParameterException e) {
            LOGGER.debug("InvalidParameterException: " + e);
        } catch (PropertyServerException e) {
            System.out.println(e);
            LOGGER.debug("PropertyServerException: " + e);
        } catch (UserNotAuthorizedException e) {
            LOGGER.debug("UserNotAuthorizedException: " + e);
        }
        if (!fileArray.isEmpty()) {
            return proxyOf.getResourcesById(request);
        } else {
            return null;
        }
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
    public CompletableFuture<Boolean> addResource(
            final AddResourceRequest request) {
        return null;
    }

    @Override
    public CompletableFuture<?> process(final Request request) {
        return null;
    }
}
