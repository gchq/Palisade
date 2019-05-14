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

import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.client.ClientConfiguredServices;
import uk.gov.gchq.palisade.example.client.ExampleSimpleClient;
import uk.gov.gchq.palisade.example.common.ExampleUsers;
import uk.gov.gchq.palisade.example.common.Purpose;
import uk.gov.gchq.palisade.example.hrdatagenerator.types.Employee;
import uk.gov.gchq.palisade.example.perf.PerfFileSet;
import uk.gov.gchq.palisade.service.PalisadeService;

import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Test that reads the large data file from Palisade with no policy.
 */
public class ReadLargeNoPolicyTrial extends PalisadeTrial {

    @Override
    public String name() {
        return "large_no_policy";
    }

    @Override
    public String description() {
        return "reads the large data file with no policy set";
    }

    @Override
    public void accept(final PerfFileSet fileSet, final PerfFileSet noPolicySet) {
        requireNonNull(fileSet, "fileSet");
        requireNonNull(noPolicySet, "noPolicySet");

        //find Palisade entry point
        ClientConfiguredServices configuredServices = getPalisadeClientServices();

        //register a request for a file
        PalisadeService palisade = configuredServices.getPalisadeService();

        ExampleSimpleClient client = new ExampleSimpleClient(palisade);

        User alice = ExampleUsers.getAlice();

        try (Stream<Employee> results = client.read(noPolicySet.getLargeFile().toString(), alice.getUserId().getId(), Purpose.SALARY.name())) {
            results.count();
        }
    }
}
