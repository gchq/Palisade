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

package uk.gov.gchq.palisade.example.function;

import uk.gov.gchq.palisade.example.ExampleObj;

import java.util.function.Predicate;

public class IsTimestampMoreThan implements Predicate<ExampleObj> {
    private long min;

    public IsTimestampMoreThan() {
    }

    public IsTimestampMoreThan(final long min) {
        this.min = min;
    }

    @Override
    public boolean test(final ExampleObj exampleObj) {
        return exampleObj.getTimestamp() > min;
    }

    public long getMin() {
        return min;
    }

    public void setMin(final long min) {
        this.min = min;
    }
}
