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
import org.odpi.openmetadata.frameworks.connectors.properties.AssetRelatedAsset;
import org.odpi.openmetadata.frameworks.connectors.properties.AssetRelatedAssets;
import org.odpi.openmetadata.frameworks.connectors.properties.AssetUniverse;
import org.odpi.openmetadata.frameworks.connectors.properties.RelatedAssetProperties;

import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.service.ResourceService;
import uk.gov.gchq.palisade.resource.service.request.AddResourceRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByIdRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByResourceRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesBySerialisedFormatRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByTypeRequest;
import uk.gov.gchq.palisade.service.ConnectionDetail;
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
    protected String egeriaServer;
    protected String egeriaServerURL;

    private transient AssetConsumer assetConsumer;

    public EgeriaResourceService() {
    }

    public EgeriaResourceService(final String egeriaServer, final String egeriaServerURL) {
        try {
            assetConsumer = new AssetConsumer(egeriaServer, egeriaServerURL);
        } catch (InvalidParameterException e) {
            e.printStackTrace();
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
    @Override
    public CompletableFuture<Map<LeafResource, ConnectionDetail>> getResourcesById(final GetResourcesByIdRequest request) {
        Map<LeafResource, ConnectionDetail> connections = new HashMap<>();
        return CompletableFuture.supplyAsync(() -> {
            if (request.getResourceId().endsWith("/")) {
                List<AssetUniverse> assetUni = new ArrayList();
                List<String> assetUniverse = new ArrayList();
                int maxPage = 5;
                try {
                    for (maxPage = 5; assetConsumer.findAssets(request.getUserId().getId(), ".*file.*", maxPage - 5, maxPage).size() % maxPage == 0; maxPage += 5) {
                        assetUniverse.addAll(assetConsumer.findAssets(request.getUserId().getId(), ".*file.*", maxPage - 5, maxPage));
                    }
                    assetUniverse.addAll(assetConsumer.findAssets(request.getUserId().getId(), ".*file.*", maxPage - 5, maxPage));
                    Set<String> set = new LinkedHashSet<>();
                    set.addAll(assetUniverse);
                    assetUniverse.clear();
                    assetUniverse.addAll(set);
                    assetUniverse.forEach((guid) -> {
                        try {
                            assetUni.add(assetConsumer.getAssetProperties(request.getUserId().getId(), guid));
                        } catch (InvalidParameterException e) {
                            e.printStackTrace();
                        } catch (PropertyServerException e) {
                            e.printStackTrace();
                        } catch (UserNotAuthorizedException e) {
                            e.printStackTrace();
                        }
                    });
                } catch (InvalidParameterException e) {
                    e.printStackTrace();
                } catch (PropertyServerException e) {
                    e.printStackTrace();
                } catch (UserNotAuthorizedException e) {
                    e.printStackTrace();
                }


            } else if (request.getResourceId().endsWith(".csv") || request.getResourceId().endsWith(".AVRO")) {
                try {
                    List<String> newAssetsList = assetConsumer.getAssetsByName(request.getUserId().getId(), request.getResourceId(), 0, 10);
                    for (String guid : newAssetsList) {
                        AssetUniverse assets = assetConsumer.getAssetProperties(request.getUserId().getId(), guid);
                        System.out.println(assets.getAssetTypeName());
                        if ("FileFolder".equals(assets.getAssetTypeName())) {
                            AssetRelatedAssets relatedAssets = assets.getRelatedAssets();
                            while (relatedAssets.hasNext()) {
                                AssetRelatedAsset asset = relatedAssets.next();
                                RelatedAssetProperties assetConnections = asset.getRelatedAssetProperties();
                                //TODO need to create a ConnectionDetail from the assetConnection.next()
                                ConnectionDetail connectionDetail = null;
//                        //TODO need to create a LeafResource from the RelatedAsset
                                LeafResource leafResource = null;
                                connections.put(leafResource, connectionDetail);
                            }
                            //TODO if there are more related files then loop and build resources using all guids in folder

                        } else if ("CSVFile".equals(assets.getAssetTypeName())) {
                            String id = assets.getQualifiedName().substring(assets.getQualifiedName().lastIndexOf(":") + 2);
                            String serialisedFormat = assets.getQualifiedName().substring(assets.getQualifiedName().lastIndexOf(".") + 1);
                            String type = assets.getQualifiedName().substring(assets.getQualifiedName().lastIndexOf("/") + 1, assets.getQualifiedName().indexOf("."));
                            FileResource file = new FileResource().id(id).serialisedFormat(serialisedFormat).type(type);
                        }


//                    Connector a = assetConsumer.getConnectorForAsset(request.getUserId().getId(), guid);

//                    AssetRelatedAssets relatedAssets = assets.getRelatedAssets(); //assuming that there will be a method to get child assets
//                    while (relatedAssets.hasNext()) { //forEach not supported in relatedAssets
//                        AssetRelatedAsset asset = relatedAssets.next();
//                        //the asset bean is related to the Resource in some way !!!
//                        RelatedAssetProperties assetConnections = asset.getRelatedAssetProperties();
//
//                        //TODO need to create a ConnectionDetail from the assetConnection.next()
//                        ConnectionDetail connectionDetail = null;
//                        //TODO need to create a LeafResource from the RelatedAsset
//                        LeafResource leafResource = null;
//
//                        connections.put(leafResource, connectionDetail);
//
//
////                    //TODO need to create a ConnectionDetail from the assetConnection.next()
////                    ConnectionDetail connectionDetail = null;
////                    //TODO need to create a LeafResource from the RelatedAsset
////                    LeafResource leafResource = null;
////
////                    connections.put(leafResource, connectionDetail);
//                    }
//                AssetUniverse assetUniverse = assetConsumer.getAssetProperties(request.getUserId().getId(), request.getResourceId());
//                AssetConnections assetConnections = assetUniverse.getConnections();
//                AssetRelatedAssets relatedAssets = assetUniverse.getRelatedAssets(); //assuming that there will be a method to get child assets
                    }
                } catch (InvalidParameterException e) {
                    e.printStackTrace();
                } catch (PropertyServerException e) {
                    e.printStackTrace();
                } catch (UserNotAuthorizedException e) {
                    e.printStackTrace();
                } finally {
                    return connections;
                }
            }
            return null;
        });

    }


    /**
     * Retrieve resource and connection details by resource ID. The request object allows the client to specify the
     * resource ID and obtain the connection details once the returned future has completed.
     *
     * @param request the details of which ID to request
     * @return a {@link CompletableFuture} that upon completion will contain details on how to retrieve the requested
     * resource.
     */
    public CompletableFuture<Map<LeafResource, ConnectionDetail>> getResourcesById1(final GetResourcesByIdRequest request) {
        Map<LeafResource, ConnectionDetail> connections = new HashMap<>();
        List<String> assetUniverse = new ArrayList();
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (request.getResourceId().endsWith("/") || (!(request.getResourceId().endsWith(".csv")))) {
                    int maxPage = 5;
                    for (maxPage = 5; assetConsumer.findAssets(request.getUserId().getId(), ".*file.*", maxPage - 5, maxPage).size() % maxPage == 0; maxPage += 5) {
                        assetUniverse.addAll(assetConsumer.findAssets(request.getUserId().getId(), ".*file.*", maxPage - 5, maxPage));
                    }
                    assetUniverse.addAll(assetConsumer.findAssets(request.getUserId().getId(), ".*file.*", maxPage - 5, maxPage));
                    Set<String> set = new LinkedHashSet<>();
                    set.addAll(assetUniverse);
                    assetUniverse.clear();
                    assetUniverse.addAll(set);
                } else {
                    assetUniverse.addAll(assetConsumer.getAssetsByName(request.getUserId().getId(), request.getResourceId(), 0, 10));
                }
                assetUniverse.forEach((guid) -> {
                    try {
                        AssetUniverse asset = assetConsumer.getAssetProperties(request.getUserId().getId(), guid);
                        System.out.println(asset.getAssetTypeName());
                        if ("CSVFile".equals(asset.getAssetTypeName())) {
                            String id = asset.getQualifiedName().substring(asset.getQualifiedName().lastIndexOf(":") + 2);
                            String serialisedFormat = asset.getQualifiedName().substring(asset.getQualifiedName().lastIndexOf(".") + 1);
                            String type = asset.getQualifiedName().substring(asset.getQualifiedName().lastIndexOf("/") + 1, asset.getQualifiedName().indexOf("."));
                            FileResource file = new FileResource().id(id).serialisedFormat(serialisedFormat).type(type);
//                          TODO need to create a ConnectionDetail from the assetConnection.next()
                            ConnectionDetail connectionDetail = null;
//                          TODO need to create a LeafResource from the RelatedAsset
                            connections.put(file, connectionDetail);
                        }
                    } catch (InvalidParameterException e) {
                        e.printStackTrace();
                    } catch (PropertyServerException e) {
                        e.printStackTrace();
                    } catch (UserNotAuthorizedException e) {
                        e.printStackTrace();
                    }
                });
            } catch (InvalidParameterException e) {
                e.printStackTrace();
            } catch (PropertyServerException e) {
                e.printStackTrace();
            } catch (UserNotAuthorizedException e) {
                e.printStackTrace();
            }
            System.out.println("here");
//            final RegisterDataRequest dataRequest = new RegisterDataRequest().resourceId(fileArray.get(1).toString()).userId(new UserId().id(request.getUserId().toString())).context(new Context().purpose("SALARY"));
//            final DataRequestResponse dataRequestResponse = PalisadeService.registerDataRequest(dataRequest).join();
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
