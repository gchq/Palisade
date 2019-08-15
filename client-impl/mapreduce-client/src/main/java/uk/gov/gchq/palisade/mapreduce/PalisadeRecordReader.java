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

import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.data.serialise.Serialiser;
import uk.gov.gchq.palisade.data.service.DataService;
import uk.gov.gchq.palisade.data.service.request.ReadRequest;
import uk.gov.gchq.palisade.data.service.request.ReadResponse;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.service.ConnectionDetail;
import uk.gov.gchq.palisade.service.request.DataRequestResponse;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

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
public class PalisadeRecordReader<V> extends RecordReader<LeafResource, V> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PalisadeRecordReader.class);

    /**
     * The task attempt that this record reader is serving.
     */
    private TaskAttemptContext context;

    /**
     * The request that is being processed in this task.
     */
    private DataRequestResponse dataRequestResponse;

    /**
     * Iterates through the resources to be processed.
     */
    private Iterator<Map.Entry<LeafResource, ConnectionDetail>> resIt;

    /**
     * Value supplier.
     */
    private Iterator<V> itemIt;

    /**
     * The current Palisade resource being processed.
     */
    private LeafResource currentKey;

    /**
     * The current value in a resource.
     */
    private V currentValue;

    /**
     * The Palisade serialiser that will be used to decode each item from the resources being processed.
     */
    private Serialiser<V> serialiser;

    /**
     * Count of number of resources already processed, used for Haodop progress monitoring.
     */
    private int processed;

    /**
     * Resource that was last attempted before an error occurred.
     */
    private LeafResource errResource;

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
        dataRequestResponse = reqDetails;
        resIt = reqDetails.getResources().entrySet().iterator();
        serialiser = PalisadeInputFormat.getSerialiser(taskAttemptContext);
        context = taskAttemptContext;
        currentKey = null;
        currentValue = null;
        errResource = null;
        processed = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean nextKeyValue() throws IOException {
        //if we don't have a current item iterator or it's empty...
        while (itemIt == null || !itemIt.hasNext()) {
            try {
                //...try to move to the next one, if we can't then we're done
                if (!moveToNextResource()) {
                    return false;
                }
            } catch (final CompletionException e) {
                //something went wrong while fetching the next resource, what we do now depends on the user choice of how
                //they want to handle errors, either way we need to log the error
                LOGGER.warn("Failed to connect to resource {} due to {}", errResource, e.getCause());
                LOGGER.warn("Failure exception is", e);
                errResource = null;
                //notify via counter
                context.getCounter(PalisadeRecordReader.class.getSimpleName(), "Failed resources").increment(1);
                //decide how to act
                switch (PalisadeInputFormat.getResourceErrorBehaviour(context)) {
                    case FAIL_ON_READ_FAILURE:
                        throw e;
                    case CONTINUE_ON_READ_FAILURE:
                    default:
                        //do nothing
                }
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
     * @throws java.util.concurrent.CompletionException if the next source of data suffered a failure on establishing
     *                                                  data stream
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
     *
     * @throws java.util.concurrent.CompletionException if the next source of data suffered a failure on establishing
     *                                                  data stream
     */
    private void setupItemStream() {
        Map.Entry<LeafResource, ConnectionDetail> entry = resIt.next();
        final LeafResource resource = entry.getKey();
        final ConnectionDetail conDetails = entry.getValue();
        final DataService service = conDetails.createService();
        //lodge request with the data service
        ReadRequest readRequest = (ReadRequest) new ReadRequest()
                .token(dataRequestResponse.getToken())
                .resource(resource)
                .originalRequestId(dataRequestResponse.getOriginalRequestId());
        readRequest.setOriginalRequestId(dataRequestResponse.getOriginalRequestId());

        final CompletableFuture<ReadResponse> futureResponse = service.read(readRequest);
        errResource = resource;
        //when this future completes, we should have an iterator of things once we deserialise
        itemIt = futureResponse.thenApply(response -> {
            try {
                return serialiser.deserialise(response.asInputStream()).iterator();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        })
                //force code to block at this point waiting for resource data to become available
                .join();
        //stash the resource
        currentKey = resource;
        processed++;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LeafResource getCurrentKey() throws IOException {
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
        return (dataRequestResponse != null && dataRequestResponse.getResources().size() > 0)
                ? (float) processed / dataRequestResponse.getResources().size()
                : 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        context = null;
        dataRequestResponse = null;
        currentKey = null;
        currentValue = null;
        resIt = null;
        itemIt = null;
        serialiser = null;
        errResource = null;
        processed = 0;
    }
}
