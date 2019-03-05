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

package uk.gov.gchq.palisade.service;

import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * Metrics provider convenience methods.
 */
public final class MetricsProviderUtil {

    /**
     * Pattern verifier for the filters.
     */
    private static final Pattern STAR_PATTERN = Pattern.compile("^(?:(?:\\*)?[^*]+)|(?:[^*]+(?:\\*)?)$");

    private MetricsProviderUtil() {
    }

    /**
     * Checks the filter pattern given is valid. This must not be {@code null} or empty, and must have at most one '*'
     * character either at the end or beginning of the filter. If a '*' is present, at least one other character must
     * also be present
     *
     * @param filter the string to check
     * @throws NullPointerException     if {@code filter} is {@code null}
     * @throws IllegalArgumentException if one of the above conditions is violated
     */
    public static void validateFilter(final String filter) {
        requireNonNull(filter, "filter");
        //must not be empty
        String trimmed = filter.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("filter is empty");
        }
        //must only have one asterisk either at end or beginning
        if (!STAR_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("filter is not valid filter pattern!");
        }
    }

    /**
     * Tests if the given filter will match against a key.
     *
     * @param filter the possibly wildcarded filter to match against
     * @param key    the key to test
     * @return true if the filter matches the key
     */
    public static boolean filterMatches(final String filter, final String key) {
        requireNonNull(filter, "filter");
        requireNonNull(key, "key");
        return ((filter.startsWith("*") && key.endsWith(filter.substring(1))) ||
                (filter.endsWith("*") && key.startsWith(filter.substring(0, filter.length() - 1))) ||
                filter.equals(key)
        );
    }
}
