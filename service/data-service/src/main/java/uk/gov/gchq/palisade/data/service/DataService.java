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

package uk.gov.gchq.palisade.data.service;

import uk.gov.gchq.palisade.data.service.request.ReadRequest;
import uk.gov.gchq.palisade.data.service.request.ReadResponse;
import uk.gov.gchq.palisade.service.Service;
import uk.gov.gchq.palisade.service.request.Request;

import java.util.concurrent.CompletableFuture;

/**
 * The core API for the data service.
 *
 * The responsibility of the data service is to take the read request from the
 * client, request the trusted details about the request from the palisade
 * service (what policies to apply, user details, etc). The data service is then
 * loops over the list of resources passing the list of rules that need to be
 * applied, taken from the palisade service response (DataRequestConfig) and the
 * resource to be read to the {@link uk.gov.gchq.palisade.data.service.reader.DataReader}.
 * The {@link uk.gov.gchq.palisade.data.service.reader.DataReader} will then
 * connect to the resource and apply the rules before streaming the data back to
 * the {@link DataService} which forwards the data back to the client.
 *
 *
 */
public interface DataService extends Service {

    /**
     * This method is used by the client code to request the data from the list of
     * resources or a subset of the resources that the palisade service responded
     * with in the {@link uk.gov.gchq.palisade.service.request.DataRequestResponse}.
     *
     * This method will only work if the client has already registered the data
     * request with the palisade service.
     *
     * @param request The {@link ReadRequest} that came from registering the
     *                request with the palisade service. The request can be
     *                altered to contain only a subset of the resources to be
     *                read by this data service instance.
     * @param <RAW_DATA_TYPE> The Java class of the format the data will be returned as.
     * @return a {@link CompletableFuture} {@link ReadResponse} containing the stream of data in
     *         the expected format and/or a message (error/warning/info) to be
     *         returned to the client.
     */
    <RAW_DATA_TYPE> CompletableFuture<ReadResponse<RAW_DATA_TYPE>> read(final ReadRequest request);

    @Override
    default CompletableFuture<?> process(final Request request) {
        if (request instanceof ReadRequest) {
            return read((ReadRequest) request);
        }
        return Service.super.process(request);
    }
}
