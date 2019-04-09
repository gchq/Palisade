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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.rule.Rule;
import uk.gov.gchq.palisade.rule.Rules;
import uk.gov.gchq.palisade.util.FieldGetter;
import uk.gov.gchq.palisade.util.FieldSetter;

import java.net.URL;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;


/**
 * Common utility methods.
 */
public final class Util {
    private static final Logger LOGGER = LoggerFactory.getLogger(Util.class);

    /**
     * Minimum retry time to wait between attempts.
     */
    public static final Duration MIN_DURATION_PAUSE = Duration.ofSeconds(1);

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

    /**
     * Creates a new map from a collection of map entries. A new map instance is created.
     *
     * @param entries the entries to add to the new map
     * @param <K>     the map key type
     * @param <V>     the map value type
     * @return a new map
     */
    public static <K, V> Map<K, V> newHashMap(final Collection<Entry<K, V>> entries) {
        requireNonNull(entries);
        final Map<K, V> map = new HashMap<>(entries.size());
        entries.forEach(e -> map.put(e.getKey(), e.getValue()));
        return map;
    }

    /**
     * Applies a collection of rules to a record stream.
     *
     * @param records record stream
     * @param user    user the records are being processed for
     * @param context the additional context
     * @param rules   rules collection
     * @param <T>     record type
     * @return filtered stream
     */
    public static <T> Stream<T> applyRulesToStream(final Stream<T> records, final User user, final Context context, final Rules<T> rules) {
        Objects.requireNonNull(records);
        if (isNull(rules) || isNull(rules.getRules()) || rules.getRules().isEmpty()) {
            return records;
        }

        return records.map(record -> applyRulesToRecord(record, user, context, rules)).filter(record -> null != record);
    }

    /**
     * Applies a collection of rules to a record.
     *
     * @param record  record to filter
     * @param user    user the record is being processed for
     * @param context the additional context
     * @param rules   rules collection
     * @param <T>     record type
     * @return filtered record
     */
    public static <T> T applyRulesToRecord(final T record, final User user, final Context context, final Rules<T> rules) {
        if (null == rules || rules.getRules().isEmpty()) {
            return record;
        }
        T updatedRecord = record;
        for (final Rule<T> resourceRule : rules.getRules().values()) {
            updatedRecord = resourceRule.apply(updatedRecord, user, context);
            if (null == updatedRecord) {
                break;
            }
        }
        return updatedRecord;
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

    /**
     * Create a {@link ThreadFactory} that creates daemon threads that don't prevent JVM exit.
     *
     * @return a daemon thread factory
     */
    public static ThreadFactory createDaemonThreadFactory() {
        //set up a thread to watch this
        final ThreadFactory defaultFactory = Executors.defaultThreadFactory();
        //ensure thread is daemon
        return runnable -> {
            Thread t = defaultFactory.newThread(runnable);
            t.setDaemon(true);
            return t;
        };
    }

    /**
     * Carries out the given operation a set number of times in case of failure. The given function will be called and the
     * result returned if successful. If the function throws an exception, then the function is tried again after a pause.
     * If {@code retryCount} attempts fail, then a {@link RuntimeException} is thrown with the most recent cause.
     *
     * @param function   the function to run
     * @param retryCount the maximum number of attempts
     * @param pause      the pause time between failure
     * @param <R>        the return type
     * @return object of type {@code R} from {@code function}
     * @throws IllegalArgumentException if {@code retryCount} less than 1 or {@code pause} is negative or less than the
     *                                  minimum required time
     * @throws RuntimeException         if all attempts to execute {@code function} fail
     */
    public static <R> R retryThunker(final Callable<R> function, final int retryCount, final Duration pause) {
        requireNonNull(function, "function");
        requireNonNull(pause, "pause");
        if (retryCount < 1) {
            throw new IllegalArgumentException("retryCount must be >=1");
        }
        if (pause.isNegative()) {
            throw new IllegalArgumentException("pause time cannot be negative");
        }
        if (MIN_DURATION_PAUSE.compareTo(pause) > 0) {
            throw new IllegalArgumentException("pause time must be at least " + MIN_DURATION_PAUSE.toMillis() + " ms");
        }


        RuntimeException lastCause = null;
        int count = 0;
        long wait = pause.toMillis();

        //loop with a count
        while (count < retryCount) {
            try {
                return function.call();
            } catch (Throwable t) {
                //wrap if checked
                if (t instanceof RuntimeException) {
                    lastCause = (RuntimeException) t;
                } else {
                    lastCause = new RuntimeException(t);
                }
            }

            //failed so increment count and retry
            count++;
            try {
                if (count < retryCount) {
                    Thread.sleep(wait);
                }
            } catch (InterruptedException e) {
                //ignore
            }
        }

        //wrap the exception if it is checked
        if (isNull(lastCause)) {
            //shouldn't happen
            throw new RuntimeException("root cause unknown");
        }
        throw lastCause;
    }

    /**
     * Captures an exception thrown while running the given function and writes the cause to the log. This is especially
     * useful during connection refused errors as they do not give the host information in their stack trace.
     *
     * @param func    the function to protect
     * @param address the address being contacted
     * @param <V>     the return type of the function
     * @return dependent on {@code func}
     * @throws Exception any exception thrown by {@code func}
     */
    public static <V> V logConnectionFailed(final Callable<V> func, final URL address) throws Exception {
        try {
            return func.call();
        } catch (Exception e) {
            LOGGER.error("Call to failed to {} due to {}", address.toString(), e.getMessage());
            throw e;
        }
    }
}
