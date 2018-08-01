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

import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import uk.gov.gchq.palisade.data.serialise.Serialiser;
import uk.gov.gchq.palisade.data.service.DataService;
import uk.gov.gchq.palisade.data.service.request.ReadRequest;
import uk.gov.gchq.palisade.data.service.request.ReadResponse;
import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.service.request.ConnectionDetail;
import uk.gov.gchq.palisade.service.request.DataRequestResponse;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * The main {@link RecordReader} class for Palisade MapReduce clients. This class implements the logic for connecting to
 * a data service and retrieving the results from the returned stream. It will contact the necessary data service for
 * each resource in turn from the {@link PalisadeInputSplit} provided to it. Clients can retrieve the current resource
 * being processed by calling {@link PalisadeRecordReader#getCurrentKey()}. Therefore, in the client MapReduce code, the
 * key will become the current resource being processed and the value will become the current item from that resource.
 *
 * @param <V> the value type which will be de-serialised from the resources this input split is processing
 * @implNote This class currently requests each Resource from its data service sequentially. We avoid launching all the
 * requests to the data service(s) in parallel because Hadoop's processing of tasks in an individual map task is
 * necessarily serial. If we launch multiple requests for data in parallel, but Hadoop/the user's MapReduce job spends a
 * long time processing the first Resource(s), then the data services waiting to send the ones later in the list may
 * timeout. Thus, we only make the request to the {@link DataService} responsible for an individual resource when we
 * need it. This may change in the future and SHOULD NOT be relied upon in any implementation decisions.
 * @implNote In order to do this, we create a DataRequestResponse for each Resource and send it to the data service
 * created by the corresponding ConnectionDetail object.
 */
public class PalisadeRecordReader<V> extends RecordReader<Resource, V> {
    /**
     * The request that is being processed in this task.
     */
    private DataRequestResponse resourceDetails;

    /**
     * Iterates through the resources to be processed.
     */
    private Iterator<Map.Entry<Resource, ConnectionDetail>> resIt;

    /**
     * Value supplier.
     */
    private Iterator<V> itemIt;

    /**
     * The current Palisade resource being processed.
     */
    private Resource currentKey;

    /**
     * The current value in a resource.
     */
    private V currentValue;

    /**
     * The Palisade serialiser that will be used to decode each item from the resources being processed.
     */
    private Serialiser<Object, V> serialiser;

    /**
     * Count of number of resources already processed, used for Haodop progress monitoring.
     */
    private int processed;

    /**
     * No-arg constructor.
     */
    public PalisadeRecordReader() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(final InputSplit inputSplit, final TaskAttemptContext taskAttemptContext) throws IOException {
        Objects.requireNonNull(inputSplit, "inputSplit");
        Objects.requireNonNull(taskAttemptContext, "taskAttemptContext");
        if (!(inputSplit instanceof PalisadeInputSplit)) {
            throw new ClassCastException("input split MUST be instance of " + PalisadeInputSplit.class.getName());
        }
        PalisadeInputSplit pis = (PalisadeInputSplit) inputSplit;
        //sanity check
        DataRequestResponse reqDetails = pis.getRequestResponse();
        if (reqDetails.getResources().isEmpty()) {
            throw new IOException("no resource details in input split");
        }
        resourceDetails = reqDetails;
        resIt = reqDetails.getResources().entrySet().iterator();
        serialiser = PalisadeInputFormat.getSerialiser(taskAttemptContext);
        currentKey = null;
        currentValue = null;
        processed = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean nextKeyValue() throws IOException {
        //if we don't have a current item iterator or it's empty...
        while (itemIt == null || !itemIt.hasNext()) {
            //...try to move to the next one, if we can't then we're done
            if (!moveToNextResource()) {
                return false;
            }
        }
        //by now we've either got an item iterator with results or returned
        currentValue = itemIt.next();
        return true;
    }

    /**
     * Move to the next resource in the list of resources for this input split. This co-ordinates the logic for moving
     * to the next resource.
     *
     * @return true iff we have a successfully moved to a new data stream for the next resource
     */
    private boolean moveToNextResource() {
        //do we have a resource iterator?
        if (resIt == null) {
            return false;
        } else {
            //any resources left to process?
            if (resIt.hasNext()) {
                //set up the next resource
                setupItemStream();
                return true;
            } else {
                //end of things to be iterated
                resIt = null;
                return false;
            }
        }
    }

    /**
     * Internal method to move to the next resource in our iterator of resources. This makes the actual call to the data
     * service and waits for the request to complete before extracting the data stream iterator which clients will use.
     * If successful there will be a new item iterator set up and ready to retrieve data.
     */
    private void setupItemStream() {
        Map.Entry<Resource, ConnectionDetail> entry = resIt.next();
        final Resource resource = entry.getKey();
        final ConnectionDetail conDetails = entry.getValue();
        //stash the resource
        currentKey = resource;
        final DataService service = conDetails.createService();
        //create the singleton resource request for this resource
        final DataRequestResponse singleResourceRequest = new DataRequestResponse()
                .requestId(resourceDetails.getRequestId())
                .resource(resource, conDetails);
        //lodge request with the data service
        final CompletableFuture<ReadResponse<Object>> futureResponse = service.read(new ReadRequest().dataRequestResponse(singleResourceRequest));
        //when this future completes, we should have an iterator of things once we deserialise
        itemIt = futureResponse.thenApply(response -> response.getData().map(serialiser::deserialise).iterator())
                //force code to block at this point waiting for resource data to become available
                .join();
        processed++;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource getCurrentKey() throws IOException {
        return currentKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V getCurrentValue() throws IOException {
        return currentValue;
    }

    /**
     * {@inheritDoc} Counts completed resource as units of progress.
     */
    @Override
    public float getProgress() throws IOException {
        return (resourceDetails != null && resourceDetails.getResources().size() > 0)
                ? (float) processed / resourceDetails.getResources().size()
                : 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        resourceDetails = null;
        currentKey = null;
        currentValue = null;
        resIt = null;
        itemIt = null;
        serialiser = null;
        processed = 0;
    }
}
