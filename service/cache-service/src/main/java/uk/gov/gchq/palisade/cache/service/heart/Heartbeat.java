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

import uk.gov.gchq.palisade.Util;
import uk.gov.gchq.palisade.cache.service.CacheService;
import uk.gov.gchq.palisade.cache.service.request.AddCacheRequest;
import uk.gov.gchq.palisade.service.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

/**
 * A heartbeat is used within Palisade to send notification that a particular instance of a service exists and is alive.
 * For example, when there are multiple instances of a particular service A, service B might wish to get a list of all
 * current instances of service A to find out where it can send a request. This is particularly useful for service
 * discovery and load balancing.
 * <p>
 * This relies on the time to live feature provided by a {@link CacheService} to register a running instance.
 * <p>
 * Most parameters cannot be changed whilst the heartbeats are being sent.
 * <p>
 * No parameter may be {@code null} unless stated.
 *
 * @see Stethoscope
 */
public class Heartbeat {
    /**
     * The dummy value.
     */
    private static final byte[] DUMMY_DATA = new byte[1];

    /**
     * The amount of time between heartbeats.
     */
    private Duration heartRate;

    /**
     * The cache service we are using.
     */
    private CacheService cache;

    /**
     * The type of service that is being registered.
     */
    private Class<? extends Service> serviceClass;

    /**
     * The name to provide to the cache service for this instance.
     */
    private String instanceName;

    /**
     * Create daemonised heartbeat thread.
     */
    private final ScheduledExecutorService heart = Executors.newSingleThreadScheduledExecutor(Util.createDaemonThreadFactory());

    /**
     * The heartbeat handle.
     */
    private ScheduledFuture<?> futureBeat = null;

    /**
     * Create a new instance with the default instance name and heart rate.
     */
    public Heartbeat() {
        this.heartRate = HeartUtil.DEFAULT_HEARTBEAT_DURATION;
        this.instanceName = HeartUtil.createDefaultName();
    }

    /**
     * Set the duration for this heartbeat. This must be at least {@link HeartUtil#MIN_HEARTBEAT_DURATION} else an exception
     * is thrown. You cannot change this parameter whilst the heartbeat is running.
     *
     * @param heartRate the new heart rate
     * @return this object
     * @throws IllegalArgumentException if {@code heartRate} is below the minimum threshold
     * @throws IllegalStateException    if called while {@link Heartbeat#isBeating()} returns {@code true}
     */
    public Heartbeat heartRate(final Duration heartRate) {
        requireNonNull(heartRate, "heartRate");
        if (HeartUtil.MIN_HEARTBEAT_DURATION.compareTo(heartRate) > 0) {
            throw new IllegalArgumentException("Minimum duration is " + HeartUtil.MIN_HEARTBEAT_DURATION.toMillis() + " milliseconds");
        }
        checkBeating();
        this.heartRate = heartRate;
        return this;
    }

    /**
     * Check if heart is currently beating and throw an exception.
     *
     * @throws IllegalStateException if the heart is currently beating
     */
    private void checkBeating() {
        if (isBeating()) {
            throw new IllegalStateException("can't change parameters while running");
        }
    }

    /**
     * Set the duration for this heartbeat. This must be at least {@link HeartUtil#MIN_HEARTBEAT_DURATION} else an exception is thrown. You cannot change this parameter whilst the heartbeat is running.
     *
     * @param heartRate the new heart rate
     * @throws IllegalArgumentException if {@code heartRate} is below the minimum threshold
     * @throws IllegalStateException    if called while {@link Heartbeat#isBeating()} returns {@code true}
     */
    public void setHeartRate(final Duration heartRate) {
        heartRate(heartRate);
    }

    /**
     * Get the heart rate.
     *
     * @return the current heart rate
     */
    public Duration getHeartBeat() {
        //never null
        return heartRate;
    }

    /**
     * Set the cache service this heartbeat will use as its co-ordination mechanism.
     *
     * @param cacheService the cache service to send messages to
     * @return this object
     * @throws IllegalStateException if called while {@link Heartbeat#isBeating()} returns {@code true}
     */
    public Heartbeat cacheService(final CacheService cacheService) {
        requireNonNull(cacheService, "cacheService");
        checkBeating();
        this.cache = cacheService;
        return this;
    }

    /**
     * Set the cache service this heartbeat will use as its co-ordination mechanism.
     *
     * @param cacheService the cache service to send messages to
     * @throws IllegalStateException if called while {@link Heartbeat#isBeating()} returns {@code true}
     */
    public void setCacheService(final CacheService cacheService) {
        cacheService(cacheService);
    }

    /**
     * Get the current cache service.
     *
     * @return the cache service
     */
    public CacheService getCacheService() {
        requireNonNull(cache, "cache service must be set");
        return cache;
    }

    /**
     * Sets the service type for this heartbeat.
     *
     * @param serviceClass the type of service that this instance represents
     * @return this object
     * @throws IllegalStateException if called while {@link Heartbeat#isBeating()} returns {@code true}
     */
    public Heartbeat serviceClass(final Class<? extends Service> serviceClass) {
        requireNonNull(serviceClass, "serviceClass");
        checkBeating();
        this.serviceClass = serviceClass;
        return this;
    }

    /**
     * Sets the service type for this heartbeat.
     *
     * @param serviceClass the type of service that this instance represents
     * @throws IllegalStateException if called while {@link Heartbeat#isBeating()} returns {@code true}
     */
    public void setServiceClass(final Class<? extends Service> serviceClass) {
        serviceClass(serviceClass);
    }

    /**
     * Get the current service class type.
     *
     * @return the service class
     */
    public Class<? extends Service> getServiceClass() {
        requireNonNull(serviceClass, "serviceClass must be set");
        return serviceClass;
    }

    /**
     * Set the name for this instance. This should be a recognisable name like an IP address or a hostname or URL by which other
     * entities can uniquely identify this instance. If the {@code name} is {@code null} then a default instance name will be
     * generated which will normally be the local IP address of the host.
     *
     * @param name the instance name, if {@code null} then a default name will be generated
     * @return this object
     * @throws IllegalArgumentException if {@code name} is empty
     * @throws IllegalStateException    if called while {@link Heartbeat#isBeating()} returns {@code true}
     */
    public Heartbeat instanceName(final String name) {
        checkBeating();
        if (isNull(name)) {
            //use default name
            String defName = HeartUtil.createDefaultName();
            if (isNull(defName)) {
                throw new IllegalStateException("illegal default name created. Probable bug");
            }
            this.instanceName = defName;
        } else {
            if (name.isEmpty()) {
                throw new IllegalArgumentException("name cannot be empty");
            }
            this.instanceName = name;
        }
        return this;
    }

    /**
     * Set the name for this instance. This should be a recognisable name like an IP address or a hostname or URL by which other
     * entities can uniquely identify this instance. If the {@code name} is {@code null} then a default instance name will be
     * generated which will normally be the local IP address of the host.
     *
     * @param name the instance name, if {@code null} then a default name will be generated
     * @throws IllegalArgumentException if {@code name} is empty
     * @throws IllegalStateException    if called while {@link Heartbeat#isBeating()} returns {@code true}
     */
    public void setInstanceName(final String name) {
        instanceName(name);
    }

    /**
     * The current instance name.
     *
     * @return instance name
     */
    public String getInstanceName() {
        //never null
        return instanceName;
    }

    /**
     * Checks the configuration before starting heartbeats.
     *
     * @throws IllegalStateException if something is wrong
     */
    private void validateState() {
        try {
            getCacheService();
            getServiceClass();
        } catch (NullPointerException e) {
            throw new IllegalStateException("configuration missing", e);
        }

        if (isBeating()) {
            throw new IllegalStateException("already started");
        }
    }

    /**
     * Start sending heartbeats to the cache service. All needed parameters must be set before this method can be complete
     * successfully. Exceptions will be thrown for unset items. Once started, most fields in this class cannot be changed until
     * {@link Heartbeat#stop()} is called.
     *
     * @throws IllegalStateException if a required field has not been set, or if heart beats are already active
     */
    public synchronized void start() {
        //validate parameters
        validateState();

        //create the request that we can reuse
        final AddCacheRequest<byte[]> cacheRequest = new AddCacheRequest<>()
                .service(getServiceClass())
                .value(DUMMY_DATA)
                .timeToLive(Optional.of(getHeartBeat()
                        //multiply the heart rate by the TTL ratio to make a single heart beat last longer in cache than
                        //the beat duration, thus an instance is allowed to "miss" a some beats before it will be thought
                        //of as terminated
                        .multipliedBy(HeartUtil.TIME_TO_LIVE_RATIO)))
                .locallyCacheable(false)
                .key(HeartUtil.makeKey(getInstanceName()));

        final CacheService localCache = getCacheService();

        //the actual heartbeat
        Runnable beat = () -> localCache.add(cacheRequest);

        //start the beat
        this.futureBeat = heart.scheduleWithFixedDelay(beat, 0, getHeartBeat().toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * Stops sending heartbeats to the cache service.
     */
    public synchronized void stop() {
        if (!isBeating()) {
            return;
        }
        futureBeat.cancel(true);
        futureBeat = null;
    }

    /**
     * Checks if the heartbeat notifications are currently being sent.
     *
     * @return true if beats are being sent
     */
    public synchronized boolean isBeating() {
        return nonNull(futureBeat);
    }
}
