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

package uk.gov.gchq.palisade.example.hrdatagenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.Util;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class CreateData {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateData.class);

    private CreateData() {
    }

    public static void main(final String... args) {
        if (args.length < 3) {
            LOGGER.error("This method needs at least three arguments. The directory path to save the files in, the number of employee's to generate and the number of files to split those employees between. An optional 4th argument is the number of threads to use which will default to 1.");
        } else {
            String outputFilePath = args[0];
            long numberOfEmployees = Long.parseLong(args[1]);
            int numberOfFiles = Integer.parseInt(args[2]);
            int numberOfThreads = numberOfFiles;
            if (args.length > 3) {
                numberOfThreads = Integer.parseInt(args[3]);
            }
            long startTime = System.currentTimeMillis();
            ExecutorService executors = Executors.newFixedThreadPool(numberOfThreads, Util.createDaemonThreadFactory());
            CreateDataFile[] tasks = new CreateDataFile[numberOfFiles];
            long employeesPerFile = numberOfEmployees / numberOfFiles;
            for (int i = 0; i < numberOfFiles; i++) {
                tasks[i] = new CreateDataFile(employeesPerFile, i, new File(outputFilePath + "/employee_file" + i + ".avro"));
            }
            try {
                List<Future<Boolean>> responses = executors.invokeAll(Arrays.asList(tasks));
                for (Future<Boolean> response : responses) {
                    response.get();
                }
            } catch (final Exception e) {
                LOGGER.error(e.getLocalizedMessage(), e);
            }
            long endTime = System.currentTimeMillis();
            LOGGER.info("Took " + (endTime - startTime) + "ms to create " + numberOfEmployees + " employees");
        }
    }
}
