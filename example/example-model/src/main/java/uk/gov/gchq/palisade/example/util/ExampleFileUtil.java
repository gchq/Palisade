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

package uk.gov.gchq.palisade.example.util;

import org.apache.commons.io.FileUtils;

import uk.gov.gchq.palisade.util.StreamUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

public final class ExampleFileUtil {
    private ExampleFileUtil() {
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

    /**
     * Convert the given path to an absolute URI. If the given path represents something on the local file system, then
     * the path will be converted to a full absolute path and converted to a {@code file:} URI, if not then it will be returned verbatim.
     *
     * @param path the path to convert
     * @return the URI of the path
     * @throws IllegalArgumentException if {@code path} is empty
     */
    public static URI convertToFileURI(final String path) {
        requireNonNull(path, "path");
        if (path.isEmpty()) {
            throw new IllegalArgumentException("path is empty");
        }
        URI uriPath;
        try {
            uriPath = new URI(path);
        } catch (URISyntaxException e) {
            try {
                uriPath = Paths.get(path).normalize().toUri();
            } catch (IllegalArgumentException | FileSystemNotFoundException f) {
                throw new RuntimeException("Can't parse the given file name: ", f);
            }
        }

        // If the URI scheme is not provided or is not correct for this filesystem attempt to correct it if possible

        if (isNull(uriPath.getScheme()) ||
                FileSystems.getDefault().provider().getScheme().equals(uriPath.getScheme())) {

            Pattern patt = Pattern.compile(FileSystems.getDefault().provider().getScheme() + ":[/]?([^/].*)$");
            Matcher matt = patt.matcher(uriPath.toString());
            Path file;

            if (matt.matches()) {
                return URI.create(FileSystems.getDefault().provider().getScheme() + "://" + matt.group(1));
            } else {
                if (isNull(uriPath.getScheme())) {
                    file = FileSystems.getDefault().getPath(uriPath.getPath());
                } else {
                    file = Paths.get(uriPath).normalize();
                }
            }
            //normalise this against the file system
            try {
                file = file.toRealPath(LinkOption.NOFOLLOW_LINKS);
            } catch (IOException e) {
                //doesn't exist
            }
            return file.toUri();
        }
        return uriPath;
    }
}
