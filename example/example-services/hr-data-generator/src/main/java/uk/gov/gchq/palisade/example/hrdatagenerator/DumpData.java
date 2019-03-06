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

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class DumpData {

    private static final Logger LOGGER = LoggerFactory.getLogger(DumpData.class);

    private DumpData() {
    }

    public static void main(final String[] args) {
        if (args.length < 1) {
            LOGGER.error("This method needs at least one argument. The file path of the file to dump");
        } else {
            String inputFilePath = args[0];
            long startTime = System.currentTimeMillis();

            DumpDataFile tasks = new DumpDataFile(new File(inputFilePath));

            long endTime = System.currentTimeMillis();
            LOGGER.info("Took " + (endTime - startTime) + "ms to dump");
        }
    }
}
