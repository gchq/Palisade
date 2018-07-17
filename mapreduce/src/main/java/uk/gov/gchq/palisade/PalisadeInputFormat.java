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
import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.service.PalisadeService;
import uk.gov.gchq.palisade.service.request.ConnectionDetail;
import uk.gov.gchq.palisade.service.request.RegisterDataRequest;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.PrimitiveIterator;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Phaser;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * The main client class for using Palisade data inside of the Hadoop framework. Clients should set this class as the
 * input format for a MapReduce job inside their client code.
 *
 * @param <K> The key type being retrieved from the requested resources
 * @param <V> The value type for the map task
 */
public class PalisadeInputFormat<K extends Resource, V> extends InputFormat<K, V> {
    /**
     * Char-set for serialising data.
     */
    public static final Charset UTF8 = Charset.forName("UTF-8");

    /**
     * Hadoop configuration key for storing requests to be processed.
     */
    public static final String REGISTER_REQUESTS_KEY = "uk.gov.gchq.palisade.mapreduce.registered.requests";

    /**
     * Hadoop configuration key for setting the client hint for the maximum number of mappers.
     */

    public static final String MAXIMUM_MAP_HINT_KEY = "uk.gov.gchq.palisade.mapreduce.max.map.hint";

    /**
     * Default number of mappers to use. Hint only. Zero means unlimited.
     */
    public static final int DEFAULT_MAX_MAP_HINT = 0;

    /**
     * The Palisade service instance that serves as the central point for gathering data.
     */
    private static PalisadeService palisadeService;

    /**
     * {@inheritDoc}
     * <p>
     * This method will try to split the resource details returned from the Palisade service across multiple input
     * splits as guided by the maxmimum mappers hint. However, we can only honour this hint so far, for example, each
     * separate data request will generate at least one mapper. At the moment we don't try to balance input splits
     * across multiple data requests. This may be added in the future.
     *
     * @throws IllegalStateException if no data requests have been added with {@link PalisadeInputFormat#addDataRequest(JobContext,
     *                               RegisterDataRequest)} or if no Palisade service has been set
     * @see PalisadeInputFormat#setMaxMapTasksHint(JobContext, int)
     */
    @Override
    public List<InputSplit> getSplits(final JobContext context) throws IOException, InterruptedException {
        Objects.requireNonNull(context, "context");
        //check we have a service set
        if (palisadeService == null) {
            throw new IllegalStateException("no Palisade service has been specified");
        }
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
        //how many mappers hinted at?
        int maxMapHint = getMaxMapTasksHint(context);
        final int maxCounter = (maxMapHint == 0) ? Integer.MAX_VALUE : maxMapHint;
        //create a stream for round robining resources
        IntStream index = IntStream.iterate(0, x -> (x + 1) % maxCounter);

        for (RegisterDataRequest req : reqs) {
            //tell the phaser we have another party active
            counter.register();
            //this iterator determines which mapper gets which resource
            final PrimitiveIterator.OfInt indexIt = index.iterator();
            //send request to the Palisade service
            serv.registerDataRequest(req)
                    //when the response comes back create input splits based on max mapper hint
                    .thenApplyAsync(response ->
                            response.getResources().entrySet().stream()
                                    .collect(
                                            //group by the indexIt, which will round robin the resources to
                                            //different groupings
                                            Collectors.groupingBy(ignored -> indexIt.next(),
                                                    //create downstream collector to convert the
                                                    //List<Map.Entry<Resource,ConnectionDetail>> into a map
                                                    Collector.of(
                                                            //Supplier
                                                            HashMap<Resource, ConnectionDetail>::new,
                                                            //Accumulator
                                                            (map, entry) -> {
                                                                map.put(entry.getKey(), entry.getValue());
                                                            },
                                                            //Combiner
                                                            (leftMap, rightMap) -> {
                                                                leftMap.putAll(rightMap);
                                                                return leftMap;
                                                            }
                                                    )
                                            )
                                    )
                                            //now take the values of that map (we don't care about the keys)
                                    .values()
                                    .stream()
                                            //make each map into an input split
                                    .map(m -> new PalisadeInputSplit(response.getRequestId(), m))
                                            //reduce to a list
                                    .collect(Collectors.toList()))
                            //then add them to the master list (which is thread safe)
                    .thenAccept(splits::addAll)
                            //signal this has finished
                    .whenComplete((r, e) -> counter.arriveAndDeregister());
        }

        //wait for everything to finish
        counter.awaitAdvance(counter.arriveAndDeregister());
        return new ArrayList<>(splits);
    }

    /**
     * Specify the Palisade service instance to use for requests. This should be set by clients before the job
     * launches.
     *
     * @param service the Palisade service that requests should be sent to for all jobs
     * @throws NullPointerException if {@code service} is null
     */
    public static synchronized void setPalisadeService(final PalisadeService service) {
        //method is syncced in case Hadoop tries to access it from another thread.
        Objects.requireNonNull(service, "service");
        PalisadeInputFormat.palisadeService = service;
    }

    /**
     * Get the current Palisade service.
     *
     * @return the service
     * @apiNote This method should ALWAYS be used instead of accessing the field directly, even internally in this
     * class.
     */
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
        return new PalisadeRecordReader<>();
    }

    /**
     * Adds a data request for the given job. This is the main method by which clients can request data for a MapReduce
     * job through Palisade. This will be added to the job configuration and used once a job is submitted.
     *
     * @param context the job to be configured
     * @param request the data request to be added
     * @throws NullPointerException if anything is null
     */
    public static void addDataRequest(final JobContext context, final RegisterDataRequest request) {
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

    /**
     * Get the list of registered data requests for a job.
     *
     * @param context the job to get details for
     * @return the list data requests currently registered for a job
     * @throws NullPointerException if {@code context} is null
     */
    public static List<RegisterDataRequest> getDataRequests(final JobContext context) {
        Objects.requireNonNull(context, "context");
        //retrieve the requests added so far
        String reqs = context.getConfiguration().get(REGISTER_REQUESTS_KEY, "");
        //split these up and decode
        String[] splitReqs = StringUtils.split(reqs);
        return Arrays.stream(splitReqs)
                //lose the empties
                .filter(x -> !x.isEmpty())
                        //convert back to Java classes
                .map(x -> JSONSerialiser.deserialise(x, RegisterDataRequest.class))
                        //into a list
                .collect(Collectors.toList());
    }

    /**
     * Set a hint for the maximum number of map tasks that the given job should be split into. Note that this is a hint
     * and the system is free to ignore it. There will always be at least one map task per data request generated,
     * however since each request may require multiple ${@link uk.gov.gchq.palisade.resource.Resource}s to be processed,
     * this method allows the client to provide a hint at how widely those resouces should be spread across map tasks.
     *
     * @param context the job to set the maximum hint for
     * @param maxMaps the maximum number of mappers desired, a value of 0 implies no limit
     * @throws NullPointerException     if {@code context} is null
     * @throws IllegalArgumentException if {@code maxMaps} is negative
     */
    public static void setMaxMapTasksHint(final JobContext context, final int maxMaps) {
        Objects.requireNonNull(context);
        if (maxMaps < 0) {
            throw new IllegalArgumentException("maxMaps must be >= 0");
        }
        context.getConfiguration().setInt(MAXIMUM_MAP_HINT_KEY, maxMaps);
    }

    /**
     * Get the maximum number of mappers hint set for a job. A value of zero means no limit has been set.
     *
     * @param context the job to retrieve the details for
     * @return the maximum number of map tasks hint
     * @throws NullPointerException if {@code context} is null
     */
    public static int getMaxMapTasksHint(final JobContext context) {
        Objects.requireNonNull(context);
        return context.getConfiguration().getInt(MAXIMUM_MAP_HINT_KEY, DEFAULT_MAX_MAP_HINT);
    }
}
