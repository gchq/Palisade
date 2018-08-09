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

import uk.gov.gchq.palisade.rule.Rule;
import uk.gov.gchq.palisade.rule.Rules;
import uk.gov.gchq.palisade.util.FieldGetter;
import uk.gov.gchq.palisade.util.FieldSetter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;


/**
 * Common utility methods.
 */
public final class Util {
    private Util() {
    }

    /**
     * Converts varargs strings into an array.
     *
     * @param input the input varargs to convert to an array
     * @return an array containing the input values
     */
    public static String[] arr(final String... input) {
        return input;
    }

    /**
     * Converts varargs strings into an array. Named as 'select' for readability.
     *
     * @param input the input varargs to convert to an array
     * @return an array containing the input values
     */
    public static String[] select(final String... input) {
        return input;
    }

    /**
     * Converts varargs strings into an array. Named as 'project' for readability.
     *
     * @param input the input varargs to convert to an array
     * @return an array containing the input values
     */
    public static String[] project(final String... input) {
        return input;
    }

    /**
     * Converts varargs integers into an array.
     *
     * @param input the input varargs to convert to an array
     * @return an array containing the input values
     */
    public static Integer[] arr(final Integer... input) {
        return input;
    }

    /**
     * Converts varargs integers into an array. Named as 'select' for readability.
     *
     * @param input the input varargs to convert to an array
     * @return an array containing the input values
     */
    public static Integer[] select(final Integer... input) {
        return input;
    }

    /**
     * Converts varargs integers into an array. Named as 'project' for readability.
     *
     * @param input the input varargs to convert to an array
     * @return an array containing the input values
     */
    public static Integer[] project(final Integer... input) {
        return input;
    }

    public static <K, V> Map<K, V> newHashMap(final Collection<Entry<K, V>> entries) {
        final Map<K, V> map = new HashMap<>(entries.size());
        entries.forEach(e -> map.put(e.getKey(), e.getValue()));
        return map;
    }

    public static <T> Stream<T> applyRulesToStream(final Stream<T> records, final User user, final Justification justification, final Rules<T> rules) {
        Objects.requireNonNull(records);
        if (null == rules || rules.getRules().isEmpty()) {
            return records;
        }

        return records.map(record -> applyRulesToRecord(record, user, justification, rules)).filter(record -> null != record);
    }

    public static <T> T applyRulesToRecord(final T record, final User user, final Justification justification, final Rules<T> rules) {
        if (null == rules || rules.getRules().isEmpty()) {
            return record;
        }

        T outputRecord = record;
        for (final Rule resourceRule : rules.getRules().values()) {
            outputRecord = (T) resourceRule.apply(outputRecord, user, justification);
            if (null == outputRecord) {
                break;
            }
        }
        return outputRecord;
    }

    public static <T> Object getField(
            final T instance,
            final Map<String, FieldGetter<T>> getters,
            final String reference) {
        return getField(instance, getters, reference, null);
    }

    public static <T> Object getField(
            final T instance,
            final Map<String, FieldGetter<T>> getters,
            final String reference,
            final Function<String, Object> notFound) {
        final int fieldEnd = reference.indexOf(".");
        final String field;
        String subfield = null;
        if (fieldEnd > -1) {
            field = reference.substring(0, fieldEnd);
            if (fieldEnd + 1 < reference.length()) {
                subfield = reference.substring(fieldEnd + 1);
            }
        } else {
            field = reference;
        }

        final FieldGetter<T> getter = getters.get(field);
        final Object result;
        if (null != getter) {
            result = getter.apply(instance, subfield);
        } else if (null != notFound) {
            result = notFound.apply(field);
        } else {
            result = null;
        }
        return result;
    }

    public static <T> void setField(final T instance,
                                    final Map<String, FieldSetter<T>> setters,
                                    final String reference,
                                    final Object value) {
        setField(instance, setters, reference, value, null);
    }

    public static <T> void setField(final T instance,
                                    final Map<String, FieldSetter<T>> setters,
                                    final String reference,
                                    final Object value,
                                    final Consumer<String> notFound) {
        final int fieldEnd = reference.indexOf(".");
        final String field;
        String subfield = null;
        if (fieldEnd > -1) {
            field = reference.substring(0, fieldEnd);
            if (fieldEnd + 1 < reference.length()) {
                subfield = reference.substring(fieldEnd + 1);
            }
        } else {
            field = reference;
        }

        final FieldSetter<T> setter = setters.get(field);
        if (null != setter) {
            setter.accept(instance, subfield, value);
        } else if (null != notFound) {
            notFound.accept(field);
        }
    }
}
