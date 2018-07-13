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
package uk.gov.gchq.palisade;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.util.StringUtils;

import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.service.request.ConnectionDetail;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

public class PalisadeInputSplit extends InputSplit implements Writable {

    private RequestId requestId;
    private Resource resource;
    private ConnectionDetail connectionDetail;

    /**
     * No-arg constructor required by Hadoop to de-serialise.
     */
    public PalisadeInputSplit() {
    }

    public PalisadeInputSplit(final RequestId requestId, final Resource resource, final ConnectionDetail connectionDetail) {
        Objects.requireNonNull(requestId, "requestId");
        Objects.requireNonNull(resource, "resource");
        Objects.requireNonNull(connectionDetail, "connectionDetail");
        this.requestId = requestId;
        this.resource = resource;
        this.connectionDetail = connectionDetail;
    }

    public RequestId getRequestId() {
        return requestId;
    }

    public Resource getResource() {
        return resource;
    }

    public ConnectionDetail getConnectionDetail() {
        return connectionDetail;
    }

    /**
     * {@inheritDoc}
     *
     * @return always returns 0
     */
    @Override
    @JsonIgnore
    public long getLength() throws IOException, InterruptedException {
        return 0;
    }

    /**
     * {@inheritDoc}
     *
     * @return always returns an empty string array
     */
    @Override
    @JsonIgnore
    public String[] getLocations() throws IOException, InterruptedException {
        return StringUtils.emptyStringArray;
    }

    @Override
    public void write(final DataOutput dataOutput) throws IOException {
        Objects.requireNonNull(dataOutput, "dataOutput");
        //serialise this class to JSON and write out
        byte[] serial = JSONSerialiser.serialise(this);
        dataOutput.writeInt(serial.length);
        dataOutput.write(serial);
    }

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
        PalisadeInputSplit other = JSONSerialiser.deserialise(buffer, PalisadeInputSplit.class);
        Objects.requireNonNull(other.getRequestId(), "requestId");
        Objects.requireNonNull(other.getResource(), "resource");
        Objects.requireNonNull(other.getConnectionDetail(), "connectionDetail");
        //all clear
        this.requestId = other.getRequestId();
        this.resource = other.getResource();
        this.connectionDetail = other.getConnectionDetail();
    }
}
