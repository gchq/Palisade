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

package uk.gov.gchq.palisade.example.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.example.hrdatagenerator.CreateData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A class to test if the Palisade data path can handle retrieving many thousands of resources in a single request. This class
 * will create the given number of resources in the examples/resources/data directory and then try to retrieve them.
 */
public final class BulkTestExample {
    private static final Logger LOGGER = LoggerFactory.getLogger(BulkTestExample.class);

    private BulkTestExample() {
    }

    /**
     * Set by the destruct method to ensure this doesn't happen via a shutdown thread as well.
     */
    private static AtomicBoolean hasDestructionOccured = new AtomicBoolean(false);

    public static void main(final String[] args) throws Exception {
        if (args.length < 2) {
            System.out.printf("Usage: %s file\n", RestExample.class.getTypeName());
            System.out.println("\ndirectory\tdirectory to create files in (if directory exists, it will be temporarily renamed)");
            System.out.println("\nquantity\tnumber of Employee data files to create and try to retrieve");
            System.out.println();
            System.out.println("OPTIONAL:");
            System.out.println("behaviour\tdc d = don't delete created files after test, c = don't create files at beginning," +
                    " try to reuse previously created files, b = don't do either");
            System.exit(1);
        }

        boolean shouldCreate = true;
        boolean shouldDelete = true;
        String directory = args[0];

        int numFiles;
        try {
            numFiles = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid number entered");
            System.exit(1);
            return; //compiler doesn't realise above line won't return
        }

        if (args.length > 2) {
            if (args[2].equalsIgnoreCase("c")) {
                shouldCreate = false;
            } else if (args[2].equalsIgnoreCase("d")) {
                shouldDelete = false;
            } else if (args[2].equalsIgnoreCase("b")) {
                shouldCreate = false;
                shouldDelete = false;
            } else {
                throw new IllegalArgumentException(args[2] + " is invalid");
            }
        }

        //ensure we clean up if a SIGTERM occurs
        configureShutdownHook(shouldDelete, directory);

        //First create some bulk data (unless flag set)
        try {
            if (shouldCreate) {
                createBulkData(directory, numFiles);
            }

            //run test
            RestExample.main(directory);

        } finally {
            if (shouldDelete) {
                removeBulkData(directory);
            }
        }
    }

    /**
     * Ensures that the removal/restoration of the original data directory occurs if the VM is closed. This intercepts
     * things like SIGTERM (Ctrl-C) events, but not abnormal termination.
     *
     * @param shouldDelete whether the deletion should occur at all
     * @param directory    the directory path for the original data
     */
    private static void configureShutdownHook(final boolean shouldDelete, final String directory) {
        //register shutdown hook in case someone tries to terminate the VM gracefully
        if (shouldDelete) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    removeBulkData(directory);
                } catch (IOException e) {
                    LOGGER.error("Exception on shutdown ", e);
                    //don't throw exceptions from shutdown hooks!
                }
            }));
        }
    }

    /**
     * Remove the generated directory and replace the original one.
     *
     * @param directory the original directory
     * @throws IOException for any filesystem errors
     */
    private static void removeBulkData(final String directory) throws IOException {
        if (hasDestructionOccured.compareAndSet(false, true)) {
            Path dir = Paths.get(directory);
            Path newLocation = generateNewDirectoryName(dir);

            //remove existing files
            Files.list(dir)
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });

            //remove original
            Files.deleteIfExists(dir);
            LOGGER.info("Deleted {}", dir);
            //copy back
            Files.move(newLocation, dir);
            LOGGER.info("Moved {} to {}", newLocation, dir);
        } else {
            LOGGER.info("Directory already deleted");
        }
    }

    /**
     * Create the bulk data directory. This will move any existing directory to a different name by adding ".spare" to the
     * end of the name.
     *
     * @param directory the directory to create files in
     * @param numCopies the number of resources to create
     * @throws IOException for any file system error
     */
    private static void createBulkData(final String directory, final int numCopies) throws IOException {
        Path dir = Paths.get(directory);
        Path newLocation = generateNewDirectoryName(dir);
        if (Files.exists(dir) && !Files.isDirectory(dir)) {
            throw new IllegalArgumentException(directory + " is not a directory");
        }
        moveDataDir(dir, newLocation);

        //call the HR Data generator
        CreateData.main(directory, "10", "1");

        //check for existence of the file we need
        Path startFile = dir.resolve("employee_file0.avro");
        if (!Files.exists(startFile)) {
            throw new IOException("Creation of employee file failed, couldn't find file " + startFile);
        }

        //copy the files out n times
        cloneFiles(numCopies, startFile);
    }

    /**
     * Creates the path for the backup of the data directory.
     *
     * @param dir the data directory for the files
     * @return the new path
     */
    private static Path generateNewDirectoryName(final Path dir) {
        return dir.resolveSibling(dir.getFileName() + ".spare");
    }

    /**
     * Copy the given file a number of times in the same directory.
     *
     * @param numCopies    the number of copies of the file needed
     * @param originalFile the file being copied
     * @throws IOException for any IO errors
     */
    private static void cloneFiles(final int numCopies, final Path originalFile) throws IOException {
        if (numCopies < 0) {
            throw new IllegalArgumentException("Can't have fewer than 0 copies");
        }
        //now copy that out as many times as necessary
        for (int i = 1; i < numCopies; i++) {
            Path newFile = originalFile.resolveSibling("employee_file" + i + ".avro");
            Files.copy(originalFile, newFile);
            if (i % 10 == 0) {
                LOGGER.info("Wrote {}", newFile);
            }
        }
        LOGGER.info("Done");
    }

    /**
     * Move the original directory to a new place and recreate the original one.
     *
     * @param source      the directory to move
     * @param newLocation the renamed location
     * @throws IOException for any file system error
     */
    private static void moveDataDir(final Path source, final Path newLocation) throws IOException {
        //move it
        if (Files.exists(source)) {
            //new location
            LOGGER.info("Moving {} to {}", source, newLocation);
            Files.move(source, newLocation, StandardCopyOption.ATOMIC_MOVE);
        }

        //make the directory
        LOGGER.info("Create directory {}", source);
        Files.createDirectory(source);
    }
}
