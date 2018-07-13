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

import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.util.StringUtils;

import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.palisade.service.PalisadeService;
import uk.gov.gchq.palisade.service.request.RegisterDataRequest;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Phaser;
import java.util.stream.Collectors;

public class PalisadeInputFormat<K, V> extends InputFormat<K, V> {
    public static final Charset UTF8 = Charset.forName("UTF-8");

    public static final String REGISTER_REQUESTS_KEY = "uk.gov.gchq.palisade.mapreduce.registered.requests";

    private static PalisadeService palisadeService;

    @Override
    public List<InputSplit> getSplits(final JobContext context) throws IOException, InterruptedException {
        Objects.requireNonNull(context, "context");
        //get the list for this job
        List<RegisterDataRequest> reqs = getDataRequests(context);
        //sanity check
        if (reqs.isEmpty()) {
            throw new IllegalStateException("No data requests have been registered for this job");
        }

        //store local and call through method, don't access direct!
        PalisadeService serv = getPalisadeService();

        ConcurrentLinkedDeque<InputSplit> splits = new ConcurrentLinkedDeque<>();
        /*Each RegisterDataRequest may result in multiple resources, each of which should be in it's own input split.
        *These may complete at different rates in any order, so we need to know when all of them have finished to know
        * that all the resources have been added to the final list. Therefore, we use a Phaser to keep count of the number
        * of registration requests that have been completed.*/
        final Phaser counter = new Phaser(1); //register self

        for (RegisterDataRequest req : reqs) {
            counter.register();
            //send request to the Palisade service
            serv.registerDataRequest(req)
                    //when the response comes back create a input split per resource
                    .thenApplyAsync(response -> {
                        RequestId id = response.getRequestId();
                        //make a new input split for each resource in the response
                        return response.getResources().entrySet().stream()
                                //create the input split
                                .map(entry -> new PalisadeInputSplit(id, entry.getKey(), entry.getValue()))
                                        //this is a partial list of input splits for this response
                                .collect(Collectors.toList());
                    })
                            //then add them to the complete list
                    .thenAccept(splits::addAll)
                            //signal this has finished
                    .whenComplete((r, e) -> counter.arriveAndDeregister());
        }

        //wait for everything to finish
        counter.awaitAdvance(counter.arriveAndDeregister());
        return new ArrayList<>(splits);
    }

    public static synchronized void setPalisadeService(final PalisadeService service) {
        Objects.requireNonNull(service, "service");
        PalisadeInputFormat.palisadeService = service;
    }

    public static synchronized PalisadeService getPalisadeService() {
        return palisadeService;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Creates a {@link uk.gov.gchq.palisade.PalisadeRecordReader}.
     */
    @Override
    public RecordReader<K, V> createRecordReader(final InputSplit inputSplit, final TaskAttemptContext taskAttemptContext) throws IOException, InterruptedException {
        return new PalisadeRecordReader();
    }

    public static void addDataRequest(final JobContext context, final RegisterDataRequest request) throws IOException {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(request, "request");
        //fetch the existing ones
        String reqs = context.getConfiguration().get(REGISTER_REQUESTS_KEY, "");
        //serialise the request
        String serialised = new String(JSONSerialiser.serialise(request), UTF8);
        //add this to the list of requests
        reqs = reqs + ',' + serialised;
        context.getConfiguration().set(REGISTER_REQUESTS_KEY, reqs);
    }

    public static List<RegisterDataRequest> getDataRequests(final JobContext context) {
        Objects.requireNonNull(context, "context");
        //retrieve the requests added so far
        String reqs = context.getConfiguration().get(REGISTER_REQUESTS_KEY, "");
        //split these up and decode
        String[] splitReqs = StringUtils.split(",");
        return Arrays.stream(splitReqs)
                //lose the empties
                .filter(x -> !x.isEmpty())
                        //convert back to Java classes
                .map(x -> JSONSerialiser.deserialise(x, RegisterDataRequest.class))
                        //into a list
                .collect(Collectors.toList());
    }
}
