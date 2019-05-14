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

package uk.gov.gchq.palisade.example.perf.trial;

import uk.gov.gchq.palisade.example.perf.PerfFileSet;
import uk.gov.gchq.palisade.example.perf.PerfTrial;

public class ReadNative extends PerfTrial {

    @Override
    public String name() {
        return "native";
    }

    @Override
    public String description() {
        return "performs a native read of a file";
    }

    @Override
    public void accept(final PerfFileSet fileSet, final PerfFileSet noPolicySet) {

    }
}
