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
package uk.gov.gchq.palisade.mapreduce;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.util.StringUtils;

import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.ToStringBuilder;
import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.service.ConnectionDetail;
import uk.gov.gchq.palisade.service.request.DataRequestResponse;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * The input split for {@link PalisadeInputFormat}. This class contains all the information for describing the resources
 * for one split and the necessary connection methods for finding the data services responsible for finding those
 * resources.
 */
public class PalisadeInputSplit extends InputSplit implements Writable {
    /**
     * The response object that contains all the details
     */
    private DataRequestResponse requestResponse;

    /**
     * No-arg constructor required by Hadoop to de-serialise.
     */
    public PalisadeInputSplit() {
        requestResponse = new DataRequestResponse();
    }

    /**
     * Create a new input split. The given details are wrapped inside a new {@link DataRequestResponse} object.
     *
     * @param requestId the request ID for this split
     * @param resources the resources to be processed by this split
     * @throws NullPointerException if anything is null
     */
    public PalisadeInputSplit(final RequestId requestId, final Map<LeafResource, ConnectionDetail> resources) {
        Objects.requireNonNull(requestId, "requestId");
        Objects.requireNonNull(resources, "resources");
        requestResponse = new DataRequestResponse().requestId(requestId).resources(resources);
    }

    /**
     * Create a new input split. The request response is stored inside this input split.
     *
     * @param requestResponse the response object
     * @throws NullPointerException if {@code requestResponse} is null
     */
    public PalisadeInputSplit(final DataRequestResponse requestResponse) {
        Objects.requireNonNull(requestResponse);
        this.requestResponse = requestResponse;
    }

    /**
     * Get the response for this input split. This response object may contain many resources for a single request.
     *
     * @return the response object
     */
    public DataRequestResponse getRequestResponse() {
        return requestResponse;
    }

    /**
     * {@inheritDoc}
     * <p>
     * We measure length according to the number of resources that this split will process.
     *
     * @return the number of resources contained in this split
     */
    @Override
    public long getLength() throws IOException, InterruptedException {
        return getRequestResponse().getResources().size();
    }

    /**
     * {@inheritDoc}
     * <p>
     * We don't implement about data locality, so this always returns an empty array.
     *
     * @return always returns an empty string array
     */
    @Override
    public String[] getLocations() throws IOException, InterruptedException {
        return StringUtils.emptyStringArray;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final DataOutput dataOutput) throws IOException {
        Objects.requireNonNull(dataOutput, "dataOutput");
        //serialise this class to JSON and write out
        byte[] serial = JSONSerialiser.serialise(requestResponse);

        dataOutput.writeInt(serial.length);
        dataOutput.write(serial);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readFields(final DataInput dataInput) throws IOException {
        Objects.requireNonNull(dataInput, "dataInput");
        int length = dataInput.readInt();
        //validate length
        if (length < 0) {
            throw new IOException("illegal negative length on deserialisation");
        }
        //make buffer and read
        byte[] buffer = new byte[length];
        dataInput.readFully(buffer);
        //deserialise
        DataRequestResponse deserialisedResponse = JSONSerialiser.deserialise(buffer, DataRequestResponse.class);
        Objects.requireNonNull(deserialisedResponse, "deserialised request response was null");
        //all clear
        this.requestResponse = deserialisedResponse;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final PalisadeInputSplit other = (PalisadeInputSplit) o;

        return new EqualsBuilder()
                .append(requestResponse, other.requestResponse)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 29)
                .append(requestResponse)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("requestResponse", requestResponse)
                .toString();
    }
}
