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

import static java.util.Objects.requireNonNull;

/**
 * Simple class that contains details on the files being manipulated.
 */
public class PerfFileSet {
    /**
     * Small file of data.
     */
    private final URI smallFile;
    /**
     * Large file of data.
     */
    private final URI largeFile;

    public PerfFileSet(final URI smallFile, final URI largeFile) {
        requireNonNull(smallFile, "smallFile");
        requireNonNull(largeFile, "largeFile");
        this.smallFile = smallFile;
        this.largeFile = largeFile;
    }

    /**
     * Get the URI to the small data file.
     *
     * @return file URI
     */
    public URI getSmallFile() {
        return smallFile;
    }

    /**
     * Get the URI to the large data file.
     *
     * @return file URI
     */
    public URI getLargeFile() {
        return largeFile;
    }
}
