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

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Utility methods for the performance tests.
 */
public final class PerfUtils {

    public static final String SMALL_FILE_NAME = "employee_small.avro";
    public static final String LARGE_FILE_NAME = "employee_large.avro";
    public static final String LARGE_DIRECTORY = "large";

    private PerfUtils() {
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
     * Make {@link URI} for large file.
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

    /**
     * Convert a scheme and path back to a URI.
     *
     * @param scheme the URI scheme
     * @param path   path name
     * @return URI corrected path
     */
    public static URI toURI(final String scheme, final Path path) {
        requireNonNull(scheme, "scheme");
        requireNonNull(path, "path");

        try {
            return new URI(scheme, path.toUri().getRawSchemeSpecificPart(), null);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sinks all the data in a stream.
     *
     * @param data the stream to sink data from
     * @param <T>  type of stream
     */
    public static <T> void sink(final Stream<T> data) {
        requireNonNull(data, "data");
        long recordCount = data.count();
        if (recordCount < 1) {
            System.err.println("\nNo records returned! Did you forget to run the \"policy\" action to set the security policy?");
        }
    }
}
