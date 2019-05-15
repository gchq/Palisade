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

import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Sets up a data request through Palisade, but doesn't read any data back.
 */
public class SetupRequestTrial extends PalisadeTrial {
    /**
     * Number of requests to make.
     */
    private final int requests;

    /**
     * Create a request trial.
     *
     * @param requests number of sequential requests to Palisade to make
     * @throws IllegalArgumentException if {@code requests} less than 1
     */
    public SetupRequestTrial(final int requests) {
        if (requests < 1) {
            throw new IllegalArgumentException("requests less than 1");
        }
        this.requests = requests;
    }

    @Override
    public String name() {
        return String.format("make_%d_request", requests);
    }

    @Override
    public String description() {
        return String.format("Makes %d requests without reading data", requests);
    }

    @Override
    public void accept(final PerfFileSet fileSet, final PerfFileSet noPolicySet) {
        requireNonNull(fileSet, "fileSet");
        requireNonNull(noPolicySet, "noPolicySet");
        for (int i = 0; i < requests; i++) {
            try (Stream<Employee> data = getDataStream(getPalisadeClientServices(), fileSet.getSmallFile().toString())) {
                //do nothing
            }
        }
    }
}
