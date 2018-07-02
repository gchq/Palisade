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

package uk.gov.gchq.palisade.data.service.request;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import uk.gov.gchq.palisade.ToStringBuilder;
import uk.gov.gchq.palisade.service.request.DataRequestResponse;
import uk.gov.gchq.palisade.service.request.Request;

import java.util.concurrent.CompletableFuture;

/**
 * This class is used to send a request to the
 * {@link uk.gov.gchq.palisade.data.service.DataService} to read a set of resources.
 * This class currently just takes in a {@link DataRequestResponse} that should
 * have been returned when the client registered the data access request with the
 * {@link uk.gov.gchq.palisade.service.PalisadeService}. The
 * {@link DataRequestResponse} can be modified to only contain a subset of the
 * list of resources to be read.
 */
public class ReadRequest extends Request {
    private DataRequestResponse dataRequestResponse;

    // no-args constructor required
    public ReadRequest() {
    }

    /**
     * Default constructor
     *
     * @param dataRequestResponse {@link DataRequestResponse} that should
     *                            have been returned when the client registered
     *                            the data access request with the
     *                            {@link uk.gov.gchq.palisade.service.PalisadeService}.
     *                            The {@link DataRequestResponse} can be modified
     *                            to only contain a subset of the list of resources to be read.
     */
    public ReadRequest(final DataRequestResponse dataRequestResponse) {
        this.dataRequestResponse = dataRequestResponse;
    }

    /**
     * A static method for creating the {@link ReadRequest} based on the
     * {@link CompletableFuture} {@link DataRequestResponse} to enable the chaining of
     * asynchronous requests.
     *
     * @param futureRequestResponse a CompletableFuture DataRequestResponse
     *                              returned when the client registers the data
     *                              access request with the {@link uk.gov.gchq.palisade.service.PalisadeService}.
     * @return a {@link CompletableFuture} {@link ReadRequest}
     */
    public static CompletableFuture<ReadRequest> create(
            final CompletableFuture<DataRequestResponse> futureRequestResponse) {
        return futureRequestResponse.thenApply(t -> new ReadRequest(futureRequestResponse.join()));
    }

    public DataRequestResponse getDataRequestResponse() {
        return dataRequestResponse;
    }

    public void setDataRequestResponse(final DataRequestResponse dataRequestResponse) {
        this.dataRequestResponse = dataRequestResponse;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ReadRequest that = (ReadRequest) o;

        return new EqualsBuilder()
                .append(dataRequestResponse, that.dataRequestResponse)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(47, 37)
                .append(dataRequestResponse)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("dataRequestResponse", dataRequestResponse)
                .toString();
    }
}
