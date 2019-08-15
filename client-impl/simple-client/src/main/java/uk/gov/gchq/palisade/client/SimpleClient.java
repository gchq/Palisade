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

package uk.gov.gchq.palisade.client;

import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.data.serialise.Serialiser;
import uk.gov.gchq.palisade.data.service.DataService;
import uk.gov.gchq.palisade.data.service.request.ReadRequest;
import uk.gov.gchq.palisade.data.service.request.ReadResponse;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.service.ConnectionDetail;
import uk.gov.gchq.palisade.service.PalisadeService;
import uk.gov.gchq.palisade.service.request.DataRequestResponse;
import uk.gov.gchq.palisade.service.request.RegisterDataRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class SimpleClient<T> {
    private final Serialiser<T> serialiser;

    private final PalisadeService palisadeService;

    public SimpleClient(final PalisadeService palisadeService, final Serialiser<T> serialiser) {
        requireNonNull(palisadeService, "palisade service must be provided");
        this.palisadeService = palisadeService;
        requireNonNull(serialiser, "serialiser cannot be null");
        this.serialiser = serialiser;
    }

    public DataRequestResponse makeRequest(final String fileName, final String resourceType, final String userId, final String purpose) {
        final RegisterDataRequest dataRequest = new RegisterDataRequest().resourceId(fileName).userId(new UserId().id(userId)).context(new Context().purpose(purpose));
        final DataRequestResponse dataRequestResponse = palisadeService.registerDataRequest(dataRequest).join();
        return dataRequestResponse;
    }

    public Stream<T> getObjectStreams(final DataRequestResponse response) {
        requireNonNull(response, "response");

        final List<CompletableFuture<Stream<T>>> futureResults = new ArrayList<>(response.getResources().size());
        for (final Entry<LeafResource, ConnectionDetail> entry : response.getResources().entrySet()) {
            final ConnectionDetail connectionDetail = entry.getValue();
            final DataService dataService = connectionDetail.createService();
            final RequestId uuid = response.getOriginalRequestId();

            final ReadRequest readRequest = (ReadRequest) new ReadRequest()
                    .token(response.getToken())
                    .resource(entry.getKey())
                    .originalRequestId(response.getOriginalRequestId());
            readRequest.setOriginalRequestId(uuid);

            final CompletableFuture<ReadResponse> futureResponse = dataService.read(readRequest);
            final CompletableFuture<Stream<T>> futureResult = futureResponse.thenApply(
                    dataResponse -> {
                        try {
                            return getSerialiser().deserialise(dataResponse.asInputStream());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
            futureResults.add(futureResult);
        }

        return futureResults.stream().flatMap(CompletableFuture::join);
    }

    public Stream<T> read(final String filename, final String resourceType, final String userId, final String purpose) {
        final DataRequestResponse dataRequestResponse = makeRequest(filename, resourceType, userId, purpose);
        return getObjectStreams(dataRequestResponse);
    }

    public Serialiser<T> getSerialiser() {
        return serialiser;
    }

}
