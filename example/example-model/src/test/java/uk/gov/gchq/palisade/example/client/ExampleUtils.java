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

package uk.gov.gchq.palisade.example.client;

import org.apache.commons.io.FileUtils;

import uk.gov.gchq.palisade.util.StreamUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static java.util.Objects.requireNonNull;

/**
 * Utility methods for example clients.
 */
public final class ExampleUtils {

    private ExampleUtils() {
    }

    /**
     * Copies the example file containing serialised {@link uk.gov.gchq.palisade.example.ExampleObj} records to the given
     * path. This uses {@link StreamUtil#openStream(Class, String)} to read the file either as a resource relative to the
     * given class or from the file system.
     *
     * @param file        the file to load
     * @param destination where to copy it to
     * @param resource    the {@link Class} to try and resolve against
     */
    public static void createDataPath(final String file, final String destination, final Class resource) {
        requireNonNull(file, "file");
        requireNonNull(destination, "destination");
        requireNonNull(resource, "resource");
        final File targetFile = new File(destination);
        try (final InputStream data = StreamUtil.openStream(resource, file)) {
            requireNonNull(data, "couldn't load file: " + file);
            FileUtils.copyInputStreamToFile(data, targetFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
