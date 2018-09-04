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
package uk.gov.gchq.palisade.cache.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamUtil {
    /**
     * Compare two streams for equality. Each stream must be of the same length and contain the same elements (by
     * value). The streams are sorted beforehand. Therefore T must be naturally comparable.
     *
     * @param expected first stream
     * @param actual   second stream
     * @param <T>      type of list element
     * @return true if streams are equal
     */
    public static <T> boolean areEqual(final Stream<? extends T> expected, final Stream<? extends T> actual) {
        Stream<? extends T> sort_expected = expected.sorted();
        Stream<? extends T> sort_actual = actual.sorted();
        List<? extends T> lhs = sort_expected.collect(Collectors.toList());
        List<? extends T> rhs = sort_actual.collect(Collectors.toList());
        return lhs.equals(rhs);
    }
}
