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

package uk.gov.gchq.palisade.example.perf;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

/**
 * An output collector receives timing data from performance tests and stores them. It can later be called on to write
 * summary statistics to various outputs.
 */
public class PerfCollector {
    /**
     * Column headers.
     */
    public static final String[] COL_HDRS = {"Test", "# trials", "Min", "Max", "Mean", "Std.dev.", "25%", "50%", "75%", "99%", "Norm"};
    /**
     * Number of nanoseconds in a second.
     */
    public static final double NANOS_IN_SECOND = 1e9d;

    /**
     * Utility class.
     */
    public static class PerfStats {
        public double numTrials, min, max, mean, stdDev, pc25, pc50, pc75, pc99, norm;
    }

    /**
     * Details of all the times.
     */
    private Map<String, List<Long>> times = new TreeMap<>();

    /**
     * Convert nanoseconds to seconds.
     *
     * @param ns nanoseconds
     * @return seconds
     */
    public static double secondsToNano(final long ns) {
        return ns / NANOS_IN_SECOND;
    }

    /**
     * Records a single trial of a given test.
     *
     * @param testName the name of the test
     * @param ns       the timing of the test in nanoseconds
     * @throws IllegalArgumentException if {@code testName} is {@code null} or empty or {@code ns} is negative
     */
    public void logTime(final String testName, final long ns) {
        requireNonNull(testName, "testName");
        if (testName.trim().isEmpty()) {
            throw new IllegalArgumentException("testName cannot be empty");
        }
        if (ns < 0) {
            throw new IllegalArgumentException("ns is negative");
        }

        //create a list if needed and add this time
        times.computeIfAbsent(testName, k -> new ArrayList<>())
                .add(Long.valueOf(ns));
    }

    /**
     * Writes a table of summary statistics to the given {@link java.io.OutputStream}. The output stream
     * will not be closed when this method finishes. There will be a normalisation column, this will show the performance
     * of each test relative to the mean of a specific test. The map given details the names of which trials should have
     * their times normalised against which others.
     * For example, if one test had a 2 second mean its the normalised test had a 1 second mean, then the normalised performance
     * will be 2.
     *
     * @param normalMap the map of which test names to normalise against which others
     * @param out       where to write output
     */
    public void outputTo(final OutputStream out, final Map<String, Optional<String>> normalMap) {
        requireNonNull(out, "out");
        requireNonNull(normalMap, "normalMap");
        PrintStream print = new PrintStream(out);

        print.println("All times in seconds.\n");
        //build format strings
        StringBuilder header = new StringBuilder("%-30s");
        StringBuilder rows = new StringBuilder("%-30s");
        Arrays.stream(COL_HDRS)
                .skip(1) //skip "test" column
                .forEach(ignore -> {
                    header.append("%12s");
                    rows.append("%12.3f");
                });
        header.append("%n");
        rows.append("%n");

        print.printf(header.toString(), (Object[]) COL_HDRS);

        Map<String, PerfStats> results = new HashMap<>();

        //now for each test, compute statistics and output
        for (Map.Entry<String, List<Long>> entry : times.entrySet()) {
            PerfStats pfs = computePerfStats(entry.getValue());
            results.put(entry.getKey(), pfs);
        }

        //now do normalisation
        for (Map.Entry<String, PerfStats> entry : results.entrySet()) {
            //do we have an entry for this test?
            if (normalMap.containsKey(entry.getKey())) {
                //get the test name we should normalise against
                Optional<String> normalTest = normalMap.get(entry.getKey());
                normalTest.ifPresent(normalTestName -> {
                            //get the perf stats for this test and the normalised one
                            PerfStats trial = entry.getValue();
                            PerfStats normalTrial = results.get(normalTestName);
                            //if both non null then compute normalisation
                            if (nonNull(trial) && nonNull(normalTrial)) {
                                trial.norm = trial.mean / normalTrial.mean;
                            }
                        }
                );
            }
        }

        //send to output
        for (Map.Entry<String, PerfStats> entry : results.entrySet()) {
            PerfStats pfs = entry.getValue();
            print.printf(rows.toString(), entry.getKey(), pfs.numTrials, pfs.min, pfs.max, pfs.mean,
                    pfs.stdDev, pfs.pc25, pfs.pc50, pfs.pc75, pfs.pc99, pfs.norm);
        }

        print.flush();
    }

    /**
     * Compute the performance statistics for the given list of results. A {@link PerfStats} object is returned containing
     * the results. All times in the given list must be in nanoseconds. The results are converted into seconds. Percentiles
     * are calculated using the linear interpolation closest-rank method.
     *
     * @param results the list of results
     * @return performance statistics data
     */
    public static PerfStats computePerfStats(final List<Long> results) {
        requireNonNull(results, "results");
        //deal with zero length results
        if (results.isEmpty()) {
            return new PerfStats();
        }

        //convert to seconds
        double[] resultList = new double[results.size()];
        for (int i = 0; i < results.size(); i++) {
            resultList[i] = secondsToNano(results.get(i));
        }

        PerfStats stats = new PerfStats();
        stats.numTrials = resultList.length;

        //sort the list so we can get the percentiles
        Arrays.sort(resultList);

        //min and max are now trivially the first and last elements
        stats.min = resultList[0];
        stats.max = resultList[resultList.length - 1];

        //compute mean
        double total = 0;
        for (double seconds : resultList) {
            total = total + seconds;
        }
        stats.mean = total / stats.numTrials;

        //compute standard deviation
        double totalDiff = 0;
        for (double seconds : resultList) {
            double difFromMean = seconds - stats.mean;
            totalDiff = totalDiff + (difFromMean * difFromMean);
        }
        stats.stdDev = Math.sqrt(totalDiff / stats.numTrials);

        //compute percentiles
        stats.pc25 = computePercentile(resultList, 25);
        stats.pc50 = computePercentile(resultList, 50);
        stats.pc75 = computePercentile(resultList, 75);
        stats.pc99 = computePercentile(resultList, 99);

        //set default
        stats.norm = 0;
        return stats;
    }

    /**
     * Computes a given percentile from a sorted list of data. This uses the linear interpolated closest-rank method
     * to calculate percentiles. If the list is not already sorted into ascending order, then the results are undefined,.
     *
     * @param data       the sorted data
     * @param percentile the desired percentile, must be in range [0,100]
     * @return the percentile result
     * @throws IllegalArgumentException if {@code percentile} is out of range, or data is empty
     */
    public static double computePercentile(final double[] data, final double percentile) {
        requireNonNull(data, "data");
        if (data.length < 1) {
            throw new IllegalArgumentException("data array is empty");
        }
        if (percentile < 0 || percentile > 100) {
            throw new IllegalArgumentException("percentile must be between 0 and 100 inclusive");
        }

        //assume data is sorted
        double rank = ((percentile / 100d) * (data.length - 1));
        int truncatedRank = (int) rank;
        double remain = rank - truncatedRank;
        double lower = data[truncatedRank];
        double upper = data[Math.min(truncatedRank + 1, data.length - 1)];
        double result = lower + (remain * (upper - lower));
        return result;
    }
}
