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

package uk.gov.gchq.palisade.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Utility methods for opening {@link InputStream}s.
 */
public final class StreamUtil {
    private StreamUtil() {
        // Private constructor to prevent instantiation.
    }

    /**
     * Open the file found at the the specified path under the location of the given
     * class.
     *
     * @param clazz the class location
     * @param path  the path in the class location
     * @return an input stream representating the requested file
     * @throws IllegalArgumentException if there was an error opening the stream
     */
    public static InputStream openStream(final Class clazz, final String path) throws IllegalArgumentException {
        Objects.requireNonNull(path, "Path is required");
        if (new File(path).exists()) {
            try {
                return Files.newInputStream(Paths.get(path));
            } catch (final IOException e) {
                throw new IllegalArgumentException("Unable to load file: " + path, e);
            }
        } else {
            final String checkedPath = formatPathForOpenStream(path);
            final InputStream resourceAsStream = clazz.getResourceAsStream(checkedPath);
            if (null == resourceAsStream) {
                throw new IllegalArgumentException("Unable to load file: " + path);
            }
            return resourceAsStream;
        }
    }

    /**
     * Format a path to ensure that it begins with a '/' character.
     *
     * @param path the path to format
     * @return a correctly formatted path string
     */
    public static String formatPathForOpenStream(final String path) {
        if (null == path || path.isEmpty()) {
            throw new IllegalArgumentException("path is required");
        }
        return path.startsWith("/") ? path : "/" + path;
    }
}
