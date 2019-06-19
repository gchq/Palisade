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

import static java.util.Objects.isNull;

/**
 * Runs the detailed help for an action
 */
public class HelpAction implements PerfAction {

    @Override
    public String name() {
        return "help";
    }

    @Override
    public String description() {
        return "provides detailed help information on another action e.g. \"help create\"";
    }

    @Override
    public String help() {
        return "The help command provides detailed information about another action." +
                "\nYou can invoke it by using \"help\" followed by the action" +
                "\nyou want more detailed help on. This should include an" +
                "\nexplanation of any arguments for the action.";
    }

    @Override
    public Integer apply(final String[] actionArgs) {
        if (actionArgs.length < 1) {
            throw new IllegalArgumentException("please specify the action to print help for, e.g. \"create\"");
        }
        //look up command
        PerfAction action = Perf.ACTIONS.get(actionArgs[0]);
        if (isNull(action)) {
            throw new IllegalArgumentException("action " + actionArgs[0] + " is not recognised");
        }
        //call help
        String help = action.help();

        System.out.println();
        System.out.println(Objects.toString(help, "This action has no further help information."));
        return Integer.valueOf(0);
    }
}
