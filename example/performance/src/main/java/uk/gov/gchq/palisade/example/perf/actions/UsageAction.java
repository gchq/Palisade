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

package uk.gov.gchq.palisade.example.perf.actions;

import uk.gov.gchq.palisade.example.perf.Perf;
import uk.gov.gchq.palisade.example.perf.PerfAction;

import java.util.Objects;

/**
 * Print the usage information for this application.
 */
public class UsageAction implements PerfAction {

    @Override
    public String name() {
        return "usage";
    }

    @Override
    public String description() {
        return "prints usage information";
    }

    @Override
    public String help() {
        return null;
    }

    @Override
    public Integer apply(final String[] strings) {
        //build usage string
        StringBuilder out = new StringBuilder("Palisade performance tool\n" +
                "Usage information:\n" +
                "\n" +
                "\tAction\t\tDescription\n" +
                "\t======\t\t===========\n");
        Perf.ACTIONS.entrySet().stream().forEach(action -> {
            out.append("\t")
                    .append(action.getKey())
                    .append("\t\t")
                    .append(Objects.toString(action.getValue().description(), "no description supplied"))
                    .append("\n");
        });

        out.append("\n");

        System.out.println(out.toString());
        return Integer.valueOf(0);
    }
}
