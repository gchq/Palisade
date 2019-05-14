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

import java.util.function.BiConsumer;

/**
 * Abstract superclass for all performance trials.
 */
public interface PerfTrial extends BiConsumer<PerfFileSet, PerfFileSet> {
    /**
     * Returns the name for this performance test.
     *
     * @return test name
     */
    String name();

    /**
     * Provides a one line description of this performance test.
     *
     * @return the usage line
     */
    String description();

    /**
     * Perform any setup functioanlity.
     */
    default void setup() {
    }

    /**
     * Perform any tear down and clean functionality.
     */
    default void tearDown() {
    }
}
