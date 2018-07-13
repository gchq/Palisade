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

package uk.gov.gchq.palisade.policy.tuple;


import uk.gov.gchq.koryphe.tuple.Tuple;
import uk.gov.gchq.palisade.Justification;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.policy.Rule;
import uk.gov.gchq.palisade.policy.Rules;

public final class Util {
    private Util() {
    }

    public static <T> T applyRules(final T record, final User user, final Justification justification, final Rules<T> rules) {
        if (null == rules || rules.getRules().isEmpty()) {
            return record;
        }

        if (record instanceof Tuple) {
            T updatedRecord = record;
            for (final Rule<T> resourceRule : rules.getRules().values()) {
                updatedRecord = resourceRule.apply(updatedRecord, user, justification);
                if (null == updatedRecord) {
                    break;
                }
            }
            return updatedRecord;
        }

        ReflectiveTuple tuple = new ReflectiveTuple(record);
        for (final Rule resourceRule : rules.getRules().values()) {
            tuple = (ReflectiveTuple) resourceRule.apply(tuple, user, justification);
            if (null == tuple) {
                break;
            }
        }
        return null != tuple ? (T) tuple.getRecord() : null;
    }
}
