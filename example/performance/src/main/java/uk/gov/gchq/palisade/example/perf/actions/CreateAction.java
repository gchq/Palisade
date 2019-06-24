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

import uk.gov.gchq.palisade.Util;
import uk.gov.gchq.palisade.example.hrdatagenerator.CreateDataFile;
import uk.gov.gchq.palisade.example.perf.Perf;
import uk.gov.gchq.palisade.example.perf.PerfAction;
import uk.gov.gchq.palisade.example.perf.PerfUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Creates the files needed by other parts of the performance testing application.
 */
public class CreateAction implements PerfAction {

    @Override
    public String name() {
        return "create";
    }

    @Override
    public String description() {
        return "creates files for performance testing";
    }

    @Override
    public String help() {
        return "Create the employee Avro data files using the HR data generator." +
                "\nThis command should be invoked as:\n\t" +
                name() +
                "\tSMALL LARGE PATH" +
                "\nwhere SMALL and LARGE are record number counts for the small and" +
                "\nlarge employee Avro data files and PATH is the relative path from" +
                "\nthe current directory to create the files in. It will also create" +
                "\ncopies of the generated files so that the performance tool can read" +
                "\nfrom them without security policies being attached to them.";
    }

    @Override
    public Integer apply(final String[] args) {
        //args check first
        validate(args);

        //get the sizes and paths
        int small = Integer.parseInt(args[0]);
        int large = Integer.parseInt(args[1]);
        Path output = Paths.get(args[2]);
        ExecutorService tasks = Executors.newFixedThreadPool(2, Util.createDaemonThreadFactory());

        //make a result writers
        CreateDataFile smallWriter = new CreateDataFile(small, 0, PerfUtils.getSmallFile(output).toFile());
        CreateDataFile largeWriter = new CreateDataFile(large, 1, PerfUtils.getLargeFile(output).toFile());

        //submit tasks
        Perf.LOGGER.info("Going to create {} records in file {} and {} records in file {} in sub-directory", small, PerfUtils.SMALL_FILE_NAME, large, PerfUtils.LARGE_FILE_NAME);
        Future<Boolean> smallFuture = tasks.submit(smallWriter);
        Future<Boolean> largeFuture = tasks.submit(largeWriter);
        Perf.LOGGER.info("Creation tasks submitted...");

        //wait for completion
        try {
            boolean smallComplete = smallFuture.get().booleanValue();
            Perf.LOGGER.info("Small file written successfully {}", smallComplete);
            boolean largeComplete = largeFuture.get().booleanValue();
            Perf.LOGGER.info("Large file written successfully {}", largeComplete);

            //copy the files to no policy variants
            Perf.LOGGER.info("Copying small file");
            Files.copy(PerfUtils.getSmallFile(output), PerfUtils.getNoPolicyName(PerfUtils.getSmallFile(output)), StandardCopyOption.REPLACE_EXISTING);
            Perf.LOGGER.info("Copying large file");
            Files.copy(PerfUtils.getLargeFile(output), PerfUtils.getNoPolicyName(PerfUtils.getLargeFile(output)), StandardCopyOption.REPLACE_EXISTING);

            //indicate success in exit code
            return Integer.valueOf((smallComplete && largeComplete) ? 0 : 1);
        } catch (InterruptedException | ExecutionException | IOException e) {
            e.printStackTrace();
            return Integer.valueOf(1);
        } finally {
            //ensure executor shutdown
            tasks.shutdownNow();
        }
    }

    /**
     * Validate the arguments for length and type
     *
     * @param args the action arguments
     * @throws IllegalArgumentException if any error is found in the arguments
     */
    private void validate(final String[] args) {
        if (args.length != 3) {
            throw new IllegalArgumentException("expected exactly 3 arguments, see \"help " + name() + "\"");
        }
        int small = 0;
        int large = 0;
        //first two should be integers
        try {
            small = Integer.parseInt(args[0]);
            if (small < 1) {
                throw new IllegalArgumentException("small record count cannot be less than 1");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("small record count not a valid number");
        }

        try {
            large = Integer.parseInt(args[1]);
            if (large < 1) {
                throw new IllegalArgumentException("large record count cannot be less than 1");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("large record count not a valid number");
        }

        //check second integer is >= first
        if (large < small) {
            throw new IllegalArgumentException("large record count must be >= small record count");
        }

        //third should be valid path
        Path relativeOutPath = Paths.get(args[2]);
        if (!Files.exists(relativeOutPath) || !Files.isDirectory(relativeOutPath)) {
            throw new IllegalArgumentException("path specified is not a valid directory");
        }

        //check files don't already exist
        Path smallFile = PerfUtils.getSmallFile(relativeOutPath);
        Path largeFile = PerfUtils.getLargeFile(relativeOutPath);
        if (Files.exists(smallFile)) {
            throw new IllegalArgumentException("file already exists " + smallFile);
        }

        if (Files.exists(largeFile)) {
            throw new IllegalArgumentException("file already exists " + largeFile);
        }
    }
}
