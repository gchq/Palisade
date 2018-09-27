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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * An example backing store implementation for {@link BackingStore} that supports basic file persistence. We use Base 64
 * encoding to store the byte arrays safely.
 * <p>
 * This is only an example reference implementation and cannot be used in a distributed scenario. Further, it doesn't
 * support any kind of authentication, encryption at rest or transport or any other security mechanism. Therefore, it is
 * NOT suitable to be used in any kind of production scenario. It is only provided as an example of how such a backing
 * store can be written.
 * <p>
 * All methods and constructors throw {@link NullPointerException} is any parameter is <code>null</code>.
 */
public class PropertiesBackingStore implements BackingStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesBackingStore.class);
    /**
     * Base 64 encoder.
     */
    private static final Base64.Encoder B64_ENCODER = Base64.getEncoder();
    /**
     * Base64 decoder.
     */
    private static final Base64.Decoder B64_DECODER = Base64.getDecoder();
    /**
     * The suffix for storing a key's class name.
     */
    private static final String CLASS_SUFFIX = ":class";
    /**
     * The suffix for storing a key's expiry time.
     */
    private static final String EXPIRY_SUFFIX = ":expiry";
    /**
     * Backing store.
     */
    private final Properties props = new Properties();

    /**
     * The file location of this backing store.
     */
    private final String location;

    /**
     * Flag to stop modifications triggering the watcher mechanism.
     */
    private AtomicBoolean beingPersisted = new AtomicBoolean(false);

    /**
     * Create a new backing store instance.
     *
     * @param propertiesPath path to load properties from
     */
    @JsonCreator
    public PropertiesBackingStore(@JsonProperty("location") final String propertiesPath) {
        requireNonNull(propertiesPath, "propertiesPath");
        this.location = propertiesPath;
        //set up watch to allow us to detect outside changes to file
        createWatch(Paths.get(location));
        try {
            load();
        } catch (NoSuchFileException e) {
            LOGGER.warn("Can't find specified properties file {}, defaulting to empty file.", propertiesPath);
        } catch (IOException e) {
            LOGGER.error("Couldn't load properties file {}", propertiesPath, e);
        }
    }

    /**
     * The file path to where this backing store is storing the cache.
     *
     * @return the file path
     */
    public String getLocation() {
        return location;
    }

    /**
     * Load the properties to the backing file.
     *
     * @return always returns {@code null}
     * @throws IOException if anything failed during the load
     */
    private synchronized void load() throws IOException {
        if (beingPersisted.get()) {
            LOGGER.debug("File change notification triggered by property change, ignoring it.");
            return;
        }
        Path configPath = Paths.get(location);
        props.clear();
        props.load(Files.newInputStream(configPath));
        LOGGER.debug("Loaded from {}", location);
    }

    /**
     * Static class that performs a watch on a file and then performs some action when that file changes. We use a
     * {@link WeakReference} to maintain a link to prevent the outer object being garbage collected. That way, if the
     * caller no longer exists, we can clean up quickly.
     */
    private static class WatchFile implements Callable<Object> {
        private final WeakReference<PropertiesBackingStore> action;
        private final Path watchPath;

        WatchFile(final PropertiesBackingStore action, final Path watchPath) {
            requireNonNull(action, "action");
            requireNonNull(watchPath, "watchPath");
            this.action = new WeakReference<PropertiesBackingStore>(action);
            this.watchPath = watchPath;
        }

        @Override
        public Object call() throws Exception {
            WatchKey watched = null;
            try (final WatchService watcher = watchPath.getFileSystem().newWatchService()) {
                //register this
                watched = watchPath.getParent().register(watcher,
                        StandardWatchEventKinds.ENTRY_MODIFY,
                        StandardWatchEventKinds.ENTRY_CREATE);
                LOGGER.debug("Watching for changes on {}", watchPath);

                while (true) {
                    //check exit condition
                    if (action.get() == null) {
                        //reference is collected, exit loop
                        break;
                    }
                    WatchKey key = watcher.poll(10, TimeUnit.SECONDS);
                    //did anything get returned
                    if (key == null) {
                        continue;
                    }
                    if (!(key.watchable() instanceof Path)) {
                        continue;
                    }
                    Path eventPath = (Path) key.watchable();
                    // process events
                    for (WatchEvent<?> event : key.pollEvents()) {
                        final Path changed = (Path) event.context();
                        //recompute complete path
                        final Path completePath = eventPath.resolve(changed);
                        //see if the file we care about changed
                        if (completePath.equals(watchPath)) {
                            LOGGER.info("Properties backing file changed {}", watchPath);
                            //trigger the action, might have been gc'd since the first check
                            PropertiesBackingStore store = action.get();
                            if (store != null) {
                                store.load();
                            }
                            break;
                        }
                    }

                    //notify event handled
                    if (!key.reset()) {
                        break;
                    }
                }
            } catch (ClosedWatchServiceException | IOException | InterruptedException e) {
                LOGGER.error("Error while watching {}", watchPath, e);
            } finally {
                if (watched != null) {
                    watched.cancel();
                }
            }
            LOGGER.debug("No longer watching {}", watchPath);
            return null;
        }
    }

    /**
     * Sets up a watch on the named file. This is used to watch the property backing store for changes from external
     * sources.
     *
     * @param configPath the path to watch
     */
    private void createWatch(final Path configPath) {
        requireNonNull(configPath, "configPath");
        //set up a thread to watch this
        final ThreadFactory defaultFactory = Executors.defaultThreadFactory();
        //ensure thread is daemon
        ThreadFactory daemonise = runnable -> {
            Thread t = defaultFactory.newThread(runnable);
            t.setDaemon(true);
            return t;
        };
        ExecutorService singleThread = Executors.newSingleThreadExecutor(daemonise);
        singleThread.submit(new WatchFile(this, configPath));
    }

    /**
     * Performs a purge of expired keys and persists the properties to a file.
     *
     * @param forcePersist if true then data will be persisted to disk, otherwise persistence will only be done if a key
     *                     expires
     */
    private synchronized void update(final boolean forcePersist) {
        boolean keyRemoved = doKeyExpiry();
        try {
            if (forcePersist || keyRemoved) {
                persist();
            }
        } catch (IOException e) {
            LOGGER.error("Couldn't save properties file {}", location, e);
        }
    }

    /**
     * Saves the properties to the backing file.
     *
     * @throws IOException if anything failed during save
     */
    private void persist() throws IOException {
        beingPersisted.set(true);
        props.store(Files.newOutputStream(Paths.get(location)), "Palisade cache service store.");
        LOGGER.debug("Persisted to {}", location);
        beingPersisted.set(false);
    }

    /**
     * Iterates through the entries in the backing store and removes entries that should have expired.
     *
     * @return true if any key was removed
     */
    private boolean doKeyExpiry() {
        final LocalDateTime now = LocalDateTime.now();
        //to avoid mid stream modifications we make a list of things to remove
        List<String> toRemove = new ArrayList<>();
        AtomicBoolean keyRemoved = new AtomicBoolean(false);
        props.keySet().stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .filter(key -> key.endsWith(EXPIRY_SUFFIX))
                .forEach(key -> {
                    try {
                        String time = props.getProperty(key);
                        //calculate the time
                        LocalDateTime expiry = LocalDateTime.parse(time, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                        //if this is before now, then remove it
                        if (expiry.isBefore(now)) {
                            toRemove.add(deriveBaseFromDateKey(key));
                            keyRemoved.set(true);
                        }
                    } catch (DateTimeParseException e) {
                        LOGGER.error("Invalid expiry for key {}", key, e);
                    }
                });
        //remove
        toRemove.forEach(this::removeKey);
        return keyRemoved.get();
    }

    /**
     * Remove the given key and associated class and expiry times from the properties.
     *
     * @param key the base key to remove
     */
    private void removeKey(final String key) {
        requireNonNull(key, "key");
        LOGGER.debug("Removing expired key {}", key);
        props.remove(key);
        props.remove(makeDateKey(key));
        props.remove(makeClassKey(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add(final String key, final Class<?> valueClass, final byte[] value, final Optional<Duration> timeToLive) {
        //error checks
        String cacheKey = BackingStore.validateAddParameters(key, valueClass, value, timeToLive);
        LOGGER.debug("Adding cache key {} of {}", cacheKey, valueClass);
        //this isn't meant to be thread safe, but we can at least make the cache add atomic
        synchronized (this) {
            props.setProperty(cacheKey, B64_ENCODER.encodeToString(value));
            props.setProperty(makeClassKey(cacheKey), valueClass.getCanonicalName());
            //convert duration to a final time
            timeToLive.ifPresent(d -> {
                LocalDateTime expiryTime = LocalDateTime.now().plus(d);
                props.setProperty(makeDateKey(cacheKey), expiryTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            });
        }
        //run cleanup and sync
        update(true);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SimpleCacheObject get(final String key) {
        String cacheKey = BackingStore.keyCheck(key);
        //enforce and any expiries and persist
        update(false);
        synchronized (this) {
            String b64Value = props.getProperty(cacheKey);
            LOGGER.debug("Looking up key {} from cache", cacheKey);
            if (b64Value != null) {
                //key found
                try {
                    byte[] value = B64_DECODER.decode(b64Value);
                    Class<?> valueClass = Class.forName(props.getProperty(makeClassKey(cacheKey)));
                    return new SimpleCacheObject(valueClass, Optional.of(value));
                } catch (Exception e) {
                    LOGGER.warn("Couldn't retrieve key {}", key, e);
                    return new SimpleCacheObject(Object.class, Optional.empty());
                }
            } else {
                //key not found
                return new SimpleCacheObject(Object.class, Optional.empty());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Stream<String> list(final String prefix) {
        requireNonNull(prefix, "prefix");
        update(false);
        Set<?> clonedSet;
        synchronized (this) {
            clonedSet = new HashSet<>(props.keySet());
        }
        return clonedSet
                .stream()
                        //filter any non string properties
                .filter(String.class::isInstance)
                        //perform cast now we know it's safe
                .map(String.class::cast)
                .filter(x -> !x.endsWith(EXPIRY_SUFFIX))
                .filter(x -> !x.endsWith(CLASS_SUFFIX))
                .filter(x -> x.startsWith(
                                prefix)
                );
    }

    /**
     * Create the unique key for storing the class of a key.
     *
     * @param key the original key
     * @return a string for the properties file
     */
    private static String makeClassKey(final String key) {
        return key + CLASS_SUFFIX;
    }

    /**
     * Create the unique key for storing the expiry time of a key.
     *
     * @param key the original key
     * @return a string for the properties file
     */
    private static String makeDateKey(final String key) {
        return key + EXPIRY_SUFFIX;
    }

    /**
     * Derive the base key from an expiry time key.
     *
     * @param key the expiry time key
     * @return the base key
     */
    private static String deriveBaseFromDateKey(final String key) {
        return key.substring(0, key.length() - EXPIRY_SUFFIX.length());
    }
}
