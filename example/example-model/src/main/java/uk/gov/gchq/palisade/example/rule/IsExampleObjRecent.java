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

package uk.gov.gchq.palisade.example.rule;


import uk.gov.gchq.palisade.Justification;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.example.ExampleObj;
import uk.gov.gchq.palisade.rule.Rule;

public class IsExampleObjRecent implements Rule<ExampleObj> {
    private long threshold;

    public IsExampleObjRecent() {
    }

    public IsExampleObjRecent(final long threshold) {
        this.threshold = threshold;
    }

    @Override
    public ExampleObj apply(final ExampleObj record, final User user, final Justification justification) {
        if (null == record) {
            return null;
        }

        final boolean isRecent = record.getTimestamp() > threshold;
        return isRecent ? record : null;
    }
}
