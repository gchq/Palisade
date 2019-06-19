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
 * Test that reads the small data file from Palisade with no policy and times entire Palisade interaction.
 */
public class ReadSmallNoPolicyTrial extends PalisadeTrial {

    @Override
    public String name() {
        return "read_small_no_policy";
    }

    @Override
    public String description() {
        return "reads the small data file with no policy set";
    }

    @Override
    public void accept(final PerfFileSet fileSet, final PerfFileSet noPolicySet) {
        requireNonNull(fileSet, "fileSet");
        requireNonNull(noPolicySet, "noPolicySet");

        //find Palisade entry point
        PalisadeService palisade = ClientUtil.getPalisadeClientEntryPoint();

        //setup a request and read data
        try (Stream<Employee> data = getDataStream(palisade, noPolicySet.getSmallFile().toString())) {
            sink(data);
        }
    }
}
