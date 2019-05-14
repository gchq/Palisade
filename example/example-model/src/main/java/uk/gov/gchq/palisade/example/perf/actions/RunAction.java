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
import uk.gov.gchq.palisade.example.perf.PerfCollector;
import uk.gov.gchq.palisade.example.perf.PerfTrial;
import uk.gov.gchq.palisade.example.perf.TrialType;
import uk.gov.gchq.palisade.example.perf.trial.SleepTrial;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import static java.util.Objects.requireNonNull;

/**
 * Runs a series of performance tests under various circumstances and then reports the metrics to a collector.
 */
public class RunAction extends PerfAction {
    /**
     * Amount of time to wait between each trial.
     */
    public static final Duration TEST_DELAY = Duration.ofMillis(250);

    /**
     * The map of test names to test instances.
     */
    private Map<String, PerfTrial> testsToRun = new TreeMap<>();

    public RunAction() {
        //create all the performance tests and add them in here
        addTrial(new SleepTrial());
        addTrial(new SleepTrial());
    }

    /**
     * Adds the given trial to the list of tests we can run.
     *
     * @param sleepTrial trial to add to list
     */
    private void addTrial(final PerfTrial sleepTrial) {
        requireNonNull(sleepTrial.name(), "trial has returned a null name: " + sleepTrial.getClass());
        testsToRun.put(sleepTrial.name(), sleepTrial);
    }

    @Override
    public String name() {
        return "run";
    }

    @Override
    public String description() {
        return "runs a series of performance tests";
    }

    @Override
    public String help() {
        StringBuilder help = new StringBuilder("Run a series of performance tests and report the results." +
                "\nThis command should be invoked as:\n\t" +
                name() +
                "\tDRY_RUN LIVE_TEST [TESTS SKIP]" +
                "\nwhere DRY_RUN and LIVE_TEST are the number of dry runs of tests to perform before hand and the number" +
                "\nof live trials respectively. The TESTS SKIP is a comma separated list of tests to skip." +
                "\n\nThe list of valid tests is:\n");

        testsToRun.entrySet().stream()
                .forEach(e -> {
                    help.append("\t")
                            .append(e.getValue().name())
                            .append("\t\t")
                            .append(Objects.toString(e.getValue().description(), "no description"))
                            .append("\n");
                });

        return help.toString();
    }

    @Override
    public Integer apply(final String[] args) {
        //first validate the arguments
        validate(args);

        //how many dry runs do we need?
        int dryRuns = Integer.parseInt(args[0]);

        //how many live trials do we need?
        int liveTrials = Integer.parseInt(args[1]);

        //get list of tests to skip
        String[] skipTests = new String[0];
        if (args.length > 2) {
            skipTests = args[2].split(",");
        }

        //must be sorted for binary search to succeed
        Arrays.sort(skipTests);

        //create the output collector
        PerfCollector collector = new PerfCollector();

        //do we need to do any dry runs?
        if (dryRuns > 0) {
            Perf.LOGGER.info("Starting dry runs");
            performTrialBatch(dryRuns, collector, TrialType.DRY_RUN, skipTests);
        }

        //do the live trials
        Perf.LOGGER.info("Starting live tests");
        performTrialBatch(liveTrials, collector, TrialType.LIVE, skipTests);

        //write the performance test outputs
        System.out.println();
        collector.outputTo(System.out);

        return Integer.valueOf(0);
    }

    /**
     * Perform a single batch of tests. This runs all tests the given number of times.
     *
     * @param trialCount  the number of trials of each test to run
     * @param collector   the output collector
     * @param type        the test type being run
     * @param testsToSkip test names to skip
     * @throws IllegalArgumentException if any of {@code testsToSkip} are invalid
     * @throws IllegalArgumentException {@code trialCount} is < 1
     */
    public void performTrialBatch(final int trialCount, final PerfCollector collector, final TrialType type, final String... testsToSkip) {
        requireNonNull(collector, "collector");
        requireNonNull(testsToSkip, "testsToSkip");
        if (trialCount < 1) {
            throw new IllegalArgumentException("live trials cannot be less than 1");
        }
        //check tests to skip
        validateTestNames(testsToSkip);

        //iterate over each test to run and execute the given number of trials
        for (Map.Entry<String, PerfTrial> e : testsToRun.entrySet()) {
            //do we need to skip this one?
            if (Arrays.binarySearch(testsToSkip, e.getKey()) > -1) {
                Perf.LOGGER.info("Skpping test {}", e.getKey());
                continue;
            }

            //perform a run of the named test
            performSingleTrial(trialCount, e.getValue(), collector, type);
        }
    }

    /**
     * Perform a single trial a given number of times.
     *
     * @param trialCount the count
     * @param trial      the trial to run
     * @param collector  the output collector
     * @param type       the type of test being run
     * @throws IllegalArgumentException {@code trialCount} is < 1
     */
    public void performSingleTrial(final int trialCount, final PerfTrial trial, final PerfCollector collector, final TrialType type) {
        requireNonNull(trial, "trial");
        requireNonNull(collector, "collector");
        if (trialCount < 1) {
            throw new IllegalArgumentException("trial count cannot be less than 1");
        }

        System.out.printf("Starting test %s:", trial.name());

        for (int i = 0; i < trialCount; i++) {
            delay(TEST_DELAY.toMillis());

            runTrial(trial, collector, type);
            System.out.print(".." + (i + 1));
            System.out.flush();
        }

        System.out.printf("..done%n");
    }

    /**
     * Perform a single run of a single trial.
     *
     * @param trial     the trial to run
     * @param collector the output collector
     * @param type      test type being run
     */
    public static void runTrial(final PerfTrial trial, final PerfCollector collector, final TrialType type) {
        requireNonNull(trial, "trial");
        requireNonNull(collector, "collector");

        //perform trial
        try {
            long time = System.nanoTime();
            trial.run();
            time = System.nanoTime() - time;

            //if this is a live trial then log it
            if (type == TrialType.LIVE) {
                collector.logTime(trial.name(), time);
            }
        } catch (Exception e) {
            Perf.LOGGER.warn("Performance test \"{}\" failed because {}", trial.name(), e.getMessage());
        }
    }

    /**
     * Check the action arguments are valid.
     *
     * @param args the action arguments
     * @throws IllegalArgumentException if any error is found in the arguments
     */
    private void validate(final String[] args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("expected at least 2 arguments, see \"help " + name() + "\"");
        }

        //first two should be integers
        try {
            int dry = Integer.parseInt(args[0]);
            if (dry < 0) {
                throw new IllegalArgumentException("dry run count cannot be less than 0");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("dry run count not a valid number");
        }

        try {
            int actual = Integer.parseInt(args[1]);
            if (actual < 1) {
                throw new IllegalArgumentException("live count cannot be less than 1");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("live count not a valid number");
        }

        //each argument in third should be a valid test name
        if (args.length > 2) {
            String[] testsToSkip = args[2].split(",");

            validateTestNames(testsToSkip);
        }
    }

    /**
     * Checks all the names in the array are valid test names.
     *
     * @param testsToSkip the names to check
     * @throws IllegalArgumentException if an invalid name is found
     */
    private void validateTestNames(final String[] testsToSkip) {
        boolean allValid = Arrays.stream(testsToSkip)
                .allMatch(testsToRun::containsKey);

        if (!allValid) {
            throw new IllegalArgumentException("invalid test name given to skip, see \"help " + name() + "\"");
        }
    }

    /**
     * Sleep method for separating runs.
     *
     * @param ms time to wait in milliseconds
     */
    private static void delay(final long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
            //doesn't matter
        }
    }
}
