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

package uk.gov.gchq.palisade.rule;

import uk.gov.gchq.palisade.Justification;
import uk.gov.gchq.palisade.User;

/**
 * A {@link PredicateRule} is an extension to {@link Rule} that either keeps
 * or fully redacts records.
 *
 * @param <T> The type of the record. In normal cases the raw data will be deserialised
 *            by the record reader before being passed to the {@link PredicateRule#test(Object, User, Justification)}.
 */
public interface PredicateRule<T> extends Rule<T> {
    boolean test(final T record, final User user, final Justification justification);

    @Override
    default T apply(final T record, final User user, final Justification justification) {
        return test(record, user, justification) ? record : null;
    }
}
