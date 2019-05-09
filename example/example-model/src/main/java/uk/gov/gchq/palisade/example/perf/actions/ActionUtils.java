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

import java.nio.file.Path;

public final class ActionUtils {

    public static final String SMALL_FILE_NAME = "employee_small.avro";
    public static final String LARGE_FILE_NAME = "employee_large.avro";
    public static final String LARGE_DIRECTORY = "large";

    private ActionUtils() {
    }

    /**
     * Make {@link Path} for small file.
     *
     * @param outputDirectory relative output directory
     * @return complete path
     */
    public static Path getSmallFile(final Path outputDirectory) {
        return outputDirectory.resolve(SMALL_FILE_NAME);
    }

    /**
     * Make {@link Path} for large file.
     *
     * @param outputDirectory relative output directory
     * @return complete path
     */
    public static Path getLargeFile(final Path outputDirectory) {
        return outputDirectory.resolve(LARGE_DIRECTORY).resolve(LARGE_FILE_NAME);
    }

    /**
     * Create a file name with "-nopolicy" attached.
     *
     * @param file original path
     * @return adapted path
     */
    public static Path getNoPolicyName(final Path file) {
        return file.resolveSibling(file.getFileName().getFileName().toString().replace(".", "-nopolicy."));
    }
}
