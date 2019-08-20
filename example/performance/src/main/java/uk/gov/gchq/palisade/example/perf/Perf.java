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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.example.perf.actions.CreateAction;
import uk.gov.gchq.palisade.example.perf.actions.HelpAction;
import uk.gov.gchq.palisade.example.perf.actions.RunAction;
import uk.gov.gchq.palisade.example.perf.actions.SetPolicyAction;
import uk.gov.gchq.palisade.example.perf.actions.UnknownAction;
import uk.gov.gchq.palisade.example.perf.actions.UsageAction;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

/**
 * Main class for the performance testing tool.
 */
public final class Perf {
    public static final Logger LOGGER = LoggerFactory.getLogger(Perf.class);

    /**
     * Map of action names to the classes that implement them. This map contains the name of the action given on the command
     * line. We can then just look up what to do and call its apply() method.
     */
    public static final Map<String, PerfAction> ACTIONS;

    static {
        //populate the map, use a tree map so everything gets sorted when printed
        ACTIONS = new TreeMap<>();
        addAction(new CreateAction());
        addAction(new UsageAction());
        addAction(new HelpAction());
        addAction(new SetPolicyAction());
        addAction(new RunAction());
    }

    /**
     * Add the given action to the map of actions for the performance tool
     *
     * @param action action instance
     */
    protected static void addAction(final PerfAction action) {
        requireNonNull(action, "action");
        requireNonNull(action.name(), "action name is null for " + action.getClass());
        ACTIONS.put(action.name(), action);
    }

    private Perf() {
    }

    public static void main(final String... args) {
        String action = "";
        String[] actionArgs = new String[0];

        //if any argument was supplied that becomes the action
        if (args.length > 0) {
            action = args[0];
            //if we have any further arguments they becomes the action arguments
            if (args.length > 1) {
                actionArgs = Arrays.copyOfRange(args, 1, args.length);
            }
        }
        //find the action class we need to run or default it to unknown
        PerfAction actionInstance = ACTIONS.getOrDefault(action, new UnknownAction());

        //call the action and exit with that return code
        try {
            Integer exitCode = actionInstance.apply(actionArgs);
            System.exit((nonNull(exitCode)) ? exitCode.intValue() : 0);

        } catch (IllegalArgumentException e) {
            //don't print stack trace for simple error
            LOGGER.error("Executing \"{}\" gave following error: {}", action, e.getMessage());
            System.exit(1);
        } catch (RuntimeException e) {
            LOGGER.error("Executing \"{}\" failed:", action, e);
            System.exit(1);
        }
    }
}
