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
import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.service.request.DataRequestResponse;

import java.io.IOException;
import java.util.Objects;

public class PalisadeRecordReader<K extends Resource, V> extends RecordReader<K, V> {

    private DataRequestResponse resourceDetails;

    private K currentKey;

    private V currentValue;

    private Serialiser<Object, V> serialiser;

    private int processed;

    public PalisadeRecordReader() {
    }

    //TODO: how does the record reader know what serialiser to use? Do we send that with the job configuration? How
    //do we know the classname? I guess we send that too...hmm

    @Override
    public void initialize(final InputSplit inputSplit, final TaskAttemptContext taskAttemptContext) throws IOException, InterruptedException {
        Objects.requireNonNull(inputSplit, "inputSplit");
        Objects.requireNonNull(taskAttemptContext, "taskAttemptContext");
        if (inputSplit instanceof PalisadeInputSplit) {
            throw new ClassCastException("input split MUST be instance of " + PalisadeInputSplit.class.getName());
        }
        PalisadeInputSplit pis = (PalisadeInputSplit) inputSplit;
        resourceDetails = pis.getRequestResponse();
        //sanity check
        if (resourceDetails == null) {
            throw new IOException(new NullPointerException("no resource details in input split"));
        }

    }

    /*

             TODO: this should be optimised so we don't make multiple calls to the same dataService.
            for (final Entry<Resource, ConnectionDetail> entry : dataRequestResponse.getResources().entrySet()) {
                final ConnectionDetail connectionDetail = entry.getValue();
                final DataService dataService = connectionDetail.createService();

                final CompletableFuture<ReadResponse<Object>> futureResponse = dataService.read(new ReadRequest(dataRequestResponse));
                final CompletableFuture<Stream<T>> futureResult = futureResponse.thenApply(
                        response -> response.getData().map(((Serialiser<Object, T>) serialiser)::deserialise)
                );
                futureResults.add(futureResult);
            }

            return futureResults.stream().flatMap(CompletableFuture::join);
     */
    
    @Override
    public boolean nextKeyValue() throws IOException, InterruptedException {
        return false;
    }

    @Override
    public K getCurrentKey() throws IOException, InterruptedException {
        return currentKey;
    }

    @Override
    public V getCurrentValue() throws IOException, InterruptedException {
        return currentValue;
    }

    /**
     * {@inheritDoc} Counts completed resource as units of progress.
     */
    @Override
    public float getProgress() throws IOException, InterruptedException {
        return (resourceDetails != null && resourceDetails.getResources().size() > 0)
                ? processed / resourceDetails.getResources().size()
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
        serialiser = null;
        processed = 0;

    }
}
