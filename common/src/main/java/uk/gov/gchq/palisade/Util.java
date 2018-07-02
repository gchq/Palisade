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

package uk.gov.gchq.palisade;

import uk.gov.gchq.palisade.policy.Rule;
import uk.gov.gchq.palisade.policy.Rules;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Stream;

public final class Util {
    private Util() {
    }

    public static <K, V> Map<K, V> newHashMap(final Collection<Entry<K, V>> entries) {
        final Map<K, V> map = new HashMap<>(entries.size());
        entries.forEach(e -> map.put(e.getKey(), e.getValue()));
        return map;
    }

    public static <T> Stream<T> applyRules(final Stream<T> records, final User user, final Justification justification, final Rules<T> rules) {
        Objects.requireNonNull(records);
        if (null == rules || rules.getRules().isEmpty()) {
            return records;
        }

        return records.map(record -> applyRules(record, user, justification, rules)).filter(record -> null != record);
    }

    public static <T> T applyRules(final T record, final User user, final Justification justification, final Rules<T> rules) {
        if (null == rules || rules.getRules().isEmpty()) {
            return record;
        }
        T updatedRecord = record;
        for (final Rule<T> resourceRule : rules.getRules().values()) {
            updatedRecord = resourceRule.apply(updatedRecord, user, justification);
            if (null == updatedRecord) {
                break;
            }
        }
        return updatedRecord;
    }
}
