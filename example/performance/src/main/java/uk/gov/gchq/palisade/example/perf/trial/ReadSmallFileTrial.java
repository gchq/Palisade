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

import uk.gov.gchq.palisade.client.ClientUtil;
import uk.gov.gchq.palisade.example.hrdatagenerator.types.Employee;
import uk.gov.gchq.palisade.example.perf.PerfFileSet;
import uk.gov.gchq.palisade.service.PalisadeService;

import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static uk.gov.gchq.palisade.example.perf.PerfUtils.sink;

/**
 * Reads the small file a repeated number of times. This measures the entire interaction with Palisade.
 */
public class ReadSmallFileTrial extends PalisadeTrial {
    /**
     * Number of requests to make.
     */
    private final int requests;

    /**
     * Create a small file read trial.
     *
     * @param requests number of sequential requests to Palisade to make
     * @throws IllegalArgumentException if {@code requests} less than 1
     */
    public ReadSmallFileTrial(final int requests) {
        if (requests < 1) {
            throw new IllegalArgumentException("requests less than 1");
        }
        this.requests = requests;
    }

    @Override
    public String name() {
        return "read_small_with_policy";
    }

    @Override
    public String description() {
        return String.format("reads the small file %d times", requests);
    }

    @Override
    public void accept(final PerfFileSet fileSet, final PerfFileSet noPolicySet) {
        requireNonNull(fileSet, "fileSet");
        requireNonNull(noPolicySet, "noPolicySet");

        //make multiple requests
        for (int i = 0; i < requests; i++) {
            //find Palisade entry point
            PalisadeService palisade = ClientUtil.getPalisadeClientEntryPoint();

            //setup a request and read data
            try (Stream<Employee> data = getDataStream(palisade, fileSet.getSmallFile().toString())) {
                sink(data);
            }
        }
    }
}
