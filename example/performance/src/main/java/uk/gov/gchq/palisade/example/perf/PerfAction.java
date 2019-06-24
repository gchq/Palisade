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

import java.util.function.Function;

/**
 * Abstract class for performance tool actions. The {@link Function#apply(Object)} method should be overridden
 * to perform some action for the performance tool.
 */
public interface PerfAction extends Function<String[], Integer> {

    /**
     * Returns the name for this action
     *
     * @return action name
     */
    String name();

    /**
     * Provides a one line description of this action to be used in program usage information.
     *
     * @return the usage line
     */
    String description();

    /**
     * Print detailed help for this action.
     *
     * @return detailed help string
     */
    String help();
}
