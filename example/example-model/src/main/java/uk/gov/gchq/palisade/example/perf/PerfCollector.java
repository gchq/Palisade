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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * An output collector receives timing data from performance tests and stores them. It can later be called on to write
 * summary statistics to various outputs.
 */
public class PerfCollector {

    /**
     * Details of all the times.
     */
    private Map<String, List<Long>> times = new HashMap<>();

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
     * Writes a table of summary statistics to the given {@link java.io.OutputStream}
     *
     * @param out
     */
    public void outputTo(final OutputStream out) {
        requireNonNull(out, "out");
        new PrintStream(out).println(times);
    }
}
