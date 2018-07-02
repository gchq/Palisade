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
import uk.gov.gchq.palisade.service.Service;
import uk.gov.gchq.palisade.service.request.ConnectionDetail;
import uk.gov.gchq.palisade.service.request.Request;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * <p>
 * The core API for the resource service.
 * </p>
 * <p>
 * The resource service is the Palisade component that determines what resources are available that meet a specific
 * (type of) request and how they should be accessed. This interface details several methods for obtaining a list of
 * resources, e.g. by type or by data format. The methods of this service all return {@link CompletableFuture}s of
 * {@link Map}s which link a valid {@link Resource} with a {@link ConnectionDetail} object. The ${@code
 * ConnectionDetail} objects contain information on how to set up a connection to retrieve a particular resource.
 * Implementations of this service do not deal with the filtering or application of security policy to the resources.
 * Therefore, a result returned from a method call on this interface doesn't guarantee that the user will be allowed to
 * access it by policy. Other components of the Palisade system will enforce the necessary policy controls to prevent
 * access to resources by users without the necessary access rights.
 * </p>
 * <p>
 * Implementation note: None of the ${@code getResourcesByXXX} methods in this class will return in error if there
 * don't happen to be any resources that do not match a request, instead they will simply return empty ${@link Map}
 * instances.
 * </p>
 */
public interface ResourceService extends Service {
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
    CompletableFuture<Map<Resource, ConnectionDetail>> getResourcesByResource(final GetResourcesByResourceRequest request);

    /**
     * Retrieve resource and connection details by resource ID. The request object allows the client to specify the
     * resource ID and obtain the connection details once the returned future has completed.
     *
     * @param request the details of which ID to request
     * @return a {@link CompletableFuture} that upon completion will contain details on how to retrieve the requested
     * resource.
     */
    CompletableFuture<Map<Resource, ConnectionDetail>> getResourcesById(final GetResourcesByIdRequest request);

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
    CompletableFuture<Map<Resource, ConnectionDetail>> getResourcesByType(final GetResourcesByTypeRequest request);

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
    CompletableFuture<Map<Resource, ConnectionDetail>> getResourcesByFormat(final GetResourcesByFormatRequest request);

    /**
     * Informs Palisade about a specific resource that it may return to users. This lets Palisade clients request access
     * to that resource and allows Palisade to provide policy controlled access to it via the other methods in this
     * interface.
     *
     * @param request details of the resource that Palisade can manage access to
     * @return a {@link CompletableFuture} that will complete as true once the resource has been added to this {@link
     * ResourceService}
     */
    CompletableFuture<Boolean> addResource(final AddResourceRequest request);

    @Override
    default CompletableFuture<?> process(final Request request) {
        if (request instanceof GetResourcesByResourceRequest) {
            return getResourcesByResource(((GetResourcesByResourceRequest) request));
        }
        if (request instanceof GetResourcesByIdRequest) {
            return getResourcesById((GetResourcesByIdRequest) request);
        }
        if (request instanceof GetResourcesByTypeRequest) {
            return getResourcesByType((GetResourcesByTypeRequest) request);
        }
        if (request instanceof GetResourcesByFormatRequest) {
            return getResourcesByFormat((GetResourcesByFormatRequest) request);
        }
        if (request instanceof AddResourceRequest) {
            addResource((AddResourceRequest) request);
            return CompletableFuture.completedFuture(null);
        }
        return Service.super.process(request);
    }
}
