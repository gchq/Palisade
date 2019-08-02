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

import java.net.URL;
import java.security.CodeSource;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
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

        return records.map(record -> applyRulesToItem(record, user, context, rules)).filter(record -> null != record);
    }

    /**
     * Applies a collection of rules to an item (record/resource).
     *
     * @param item    resource or record to filter
     * @param user    user the record is being processed for
     * @param context the additional context
     * @param rules   rules collection
     * @param <T>     record type
     * @return filtered item
     */
    public static <T> T applyRulesToItem(final T item, final User user, final Context context, final Rules<T> rules) {
        if (null == rules || rules.getRules().isEmpty()) {
            return item;
        }
        T updateItem = item;
        for (final Rule<T> rule : rules.getRules().values()) {
            updateItem = rule.apply(updateItem, user, context);
            if (null == updateItem) {
                break;
            }
        }
        return updateItem;
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

    /**
     * Debug utility for finding which JAR file (if any) a class was loaded from. This is useful in debugging classpath
     * conflicts.
     *
     * @param clazz the fully qualified class name to search for
     * @return where the class was loaded from, or {@code null}
     */
    public static String locateJarFile(final String clazz) {
        requireNonNull(clazz, "clazz");
        try {
            Class c = Class.forName(clazz);
            CodeSource codeSource = c.getProtectionDomain().getCodeSource();

            if (codeSource != null) {
                LOGGER.info("{} was loaded from {}", clazz, codeSource.getLocation());

            } else {
                LOGGER.info("Can't determine where {} was loaded from", clazz);
            }
            return (codeSource != null) ? codeSource.getLocation().toString() : null;
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
