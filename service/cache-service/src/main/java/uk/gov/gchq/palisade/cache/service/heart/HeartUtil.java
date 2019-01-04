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

package uk.gov.gchq.palisade.cache.service.heart;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;

public final class HeartUtil {
    /**
     * The sentinel placed in cache keys to signal this is a heartbeat entry.
     */
    public static final String HEARTBEAT_SENTINEL = "__heartbeat:";

    /**
     * The minimum amount of time between heartbeats.
     */
    public static final Duration MIN_HEARTBEAT_DURATION = Duration.ofSeconds(1);

    /**
     * The default heart rate.
     */
    public static final Duration DEFAULT_HEARTBEAT_DURATION = Duration.ofSeconds(10);

    /**
     * Prevent creation.
     */
    private HeartUtil() {}

    /**
     * Create the cache key for this instance.
     *
     * @return the given name
     */
    public static String makeKey(final String instance) {
        return HEARTBEAT_SENTINEL + instance;
    }

    /**
     * Create a default name for this instance.
     *
     * @return a default name
     */
    public static String createDefaultName() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException("unable to get local IP address", e);
        }
    }
}