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

import org.odpi.openmetadata.accessservices.assetconsumer.client.AssetConsumer;
import org.odpi.openmetadata.frameworks.connectors.ffdc.InvalidParameterException;
import org.odpi.openmetadata.frameworks.connectors.ffdc.PropertyServerException;
import org.odpi.openmetadata.frameworks.connectors.ffdc.UserNotAuthorizedException;
import org.odpi.openmetadata.frameworks.connectors.properties.AssetUniverse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.service.ResourceService;
import uk.gov.gchq.palisade.resource.service.request.AddResourceRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByIdRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByResourceRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesBySerialisedFormatRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByTypeRequest;
import uk.gov.gchq.palisade.service.ConnectionDetail;
import uk.gov.gchq.palisade.service.EgeriaConnection;
import uk.gov.gchq.palisade.service.request.Request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    public static final String ERROR_RESOLVING_ID = "Error occurred while resolving resource by id";
    private static final Logger LOGGER = LoggerFactory.getLogger(EgeriaResourceService.class);
    protected String egeriaServer;
    protected String egeriaServerURL;
    private transient AssetConsumer assetConsumer;

    public EgeriaResourceService() {
    }

    public EgeriaResourceService(final String egeriaServer, final String egeriaServerURL) {
        try {
            this.egeriaServer = egeriaServer;
            this.egeriaServerURL = egeriaServerURL;
            assetConsumer = new AssetConsumer(egeriaServer, egeriaServerURL);
        } catch (InvalidParameterException e) {
            LOGGER.debug("InvalidParameterException: " + e);
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
        Map<LeafResource, ConnectionDetail> connections = new HashMap<>();
        List<String> assetUniverse = new ArrayList();
        return CompletableFuture.supplyAsync(() -> {
            try {
                /**
                 * If the user passes in an absolute path then the code will return one asset
                 * If the user passes in a folder, the code will return all assets, including folders, in all folders including nested
                 **/
                if (request.getResourceId().endsWith("/") || (!(request.getResourceId().endsWith(".csv")))) {
                    //loops through, increments page number by 5
                    //currently if the returned number is not devisable by 5 it will exit the loop and get the last set of results, this is a temp solution awaiting egeria
                    int maxPage = 5;
                    for (maxPage = 5; assetConsumer.findAssets(request.getUserId().getId(), ".*file.*", maxPage - 5, maxPage).size() % maxPage == 0; maxPage += 5) {
                        assetUniverse.addAll(assetConsumer.findAssets(request.getUserId().getId(), ".*file.*", maxPage - 5, maxPage));
                    }
                    assetUniverse.addAll(assetConsumer.findAssets(request.getUserId().getId(), ".*file.*", maxPage - 5, maxPage));
                    //prevents duplicates
                    Set<String> set = new LinkedHashSet<>();
                    set.addAll(assetUniverse);
                    assetUniverse.clear();
                    assetUniverse.addAll(set);
                } else {
                    //if user passes in absolute path, it will call this api and return one file
                    assetUniverse.addAll(assetConsumer.getAssetsByName(request.getUserId().getId(), request.getResourceId(), 0, 10));
                }
                assetUniverse.forEach((guid) -> {
                    try {
                        AssetUniverse asset = assetConsumer.getAssetProperties(request.getUserId().getId(), guid);
                        System.out.println(asset.getAssetTypeName());
                        //gets all files that aren't FileFolder, i.e csv/avro
                        if (!asset.getAssetTypeName().equals("FileFolder")) {
                            //Strips the Qualified name to form the FileResource Type
                            String id = asset.getQualifiedName().substring(asset.getQualifiedName().lastIndexOf(":") + 2);
                            String serialisedFormat = asset.getQualifiedName().substring(asset.getQualifiedName().lastIndexOf(".") + 1);
                            String type = asset.getQualifiedName().substring(asset.getQualifiedName().lastIndexOf("/") + 1, asset.getQualifiedName().indexOf("."));
                            FileResource file = new FileResource().id(id).serialisedFormat(serialisedFormat).type(type);
//                          TODO need to create a ConnectionDetail from the assetConnection.next()
                            EgeriaConnection egeriaConnection = new EgeriaConnection(egeriaServerURL, egeriaServer, request.getUserId().toString());
                            connections.put(file, egeriaConnection);
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
                LOGGER.debug("PropertyServerException: " + e);
            } catch (UserNotAuthorizedException e) {
                LOGGER.debug("UserNotAuthorizedException: " + e);
            }
            return connections;
        });

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
