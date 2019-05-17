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

package uk.gov.gchq.palisade.example.perf.trial;

import uk.gov.gchq.palisade.example.hrdatagenerator.types.Employee;
import uk.gov.gchq.palisade.example.perf.PerfFileSet;
import uk.gov.gchq.palisade.example.perf.PerfUtils;
import uk.gov.gchq.palisade.service.PalisadeService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Reads a file repeatedly through Palisade, but only times how long it takes to read the data.
 */
public class DataOnlySmallTrial extends PalisadeTrial {
    /**
     * Number of requests to make.
     */
    private final int requests;

    /**
     * Streams that will be opened before trial runs.
     */
    private List<Stream<Employee>> streams;

    /**
     * Create a small file read trial.
     *
     * @param requests number of sequential requests to Palisade to make
     * @throws IllegalArgumentException if {@code requests} less than 1
     */
    public DataOnlySmallTrial(final int requests) {
        if (requests < 1) {
            throw new IllegalArgumentException("requests less than 1");
        }
        this.requests = requests;
    }

    @Override
    public String name() {
        return String.format("data_only_small_%d_times", requests);
    }

    @Override
    public String description() {
        return String.format("reads the small file %d times, but ONLY accounts for the data reading not setup", requests);
    }

    /**
     * Overridden to create necessary requests in Palisade.
     */
    @Override
    public void setup(final PerfFileSet fileSet, final PerfFileSet noPolicySet) {
        //get palisade entry point
        PalisadeService palisadeService = getPalisadeClientServices();

        //set up the number of requests needed
        streams = new ArrayList<>(requests);
        for (int i = 0; i < requests; i++) {
            streams.add(getDataStream(palisadeService, fileSet.getSmallFile().toString()));
        }
    }

    @Override
    public void tearDown(final PerfFileSet fileSet, final PerfFileSet noPolicySet) {
        streams.clear();
    }

    @Override
    public void accept(final PerfFileSet fileSet, final PerfFileSet noPolicySet) {
        requireNonNull(fileSet, "fileSet");
        requireNonNull(noPolicySet, "noPolicySet");

        //make multiple read attempts
        streams.stream().forEach(PerfUtils::sink);
    }
}
