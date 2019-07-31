/*
 * Copyright 2019 Crown Copyright
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

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.DoneableConfigMap;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class K8sBackingStore implements BackingStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(K8sBackingStore.class);

    private final Config config = new ConfigBuilder().build();
    private final KubernetesClient client = new DefaultKubernetesClient(config);

    private static final String NAMESPACE = "default";
    private String namespace = Optional.ofNullable(client.getNamespace()).orElse(NAMESPACE);

    public K8sBackingStore() {
        LOGGER.info("Current namespace is: {}", this.namespace);

        try {
            this.client.namespaces().list().getItems()
                    .forEach(ns -> LOGGER.info("Found namespace {} with status: {}", ns.getMetadata().getName(), ns.getStatus()));
        } catch (KubernetesClientException e) {
            LOGGER.info("Not running in kubernetes or RBAC not entitled");
        }
    }

    /**
     * Clean up the resource
     */

    public void close() {
        client.close();
    }

    @Override
    public String convertKeyToCompatible(final String key) throws
            IllegalArgumentException {
        //only allow lower case alphanumberic character or -
        if (key.trim().length() == 0) {
            throw new IllegalArgumentException();
        }
        return key.toLowerCase().replaceAll("[^a-z0-9\\-]", "");
    }


    /**
     * Store the given data in the backing store. The byte array <code>value</code> is assumed to encode an object of
     * the type represented by the class <code>valueClass</code>. The <code>key</code> must not be empty or
     * <code>null</code>. If a time to live duration is required on this entry, then the optional specified should not
     * be empty. Durations must not be negative.
     * <p>
     * It is the responsibility of the backing store to ensure that the <code>valueClass</code> is stored along with the
     * object byte array. The standard way to do this is to store the type name of the class. See {@link
     * Class#getTypeName()}.
     *
     * @param key        the cache key
     * @param valueClass the object type represented in the byte array
     * @param value      the encoded object
     * @param timeToLive an optional time to live, maybe be empty
     * @return true if and only if the the cache entry was made successfully
     * @throws IllegalArgumentException if the duration is negative
     * @throws IllegalArgumentException if <code>key</code> is empty (once whitespace is trimmed)
     */
    @Override
    public boolean add(final String key, final Class<?> valueClass,
                       final byte[] value, final Optional<Duration> timeToLive) {

        if (key == null) {
            throw new IllegalArgumentException("key");
        }
        requireNonNull(valueClass, "valueClass");
        requireNonNull(value, "value");
        if (value.length == 0) {
            throw new IllegalArgumentException("value is empty");
        }

        String dns1123Compatible = convertKeyToCompatible(key);
        Resource<ConfigMap, DoneableConfigMap> configMapResource = client.configMaps().inNamespace(namespace).withName(dns1123Compatible);

        if (timeToLive.isPresent()) {
            final long epochMilli = Instant.now().plusSeconds(timeToLive.get().getSeconds()).plusNanos(timeToLive.get().getNano()).toEpochMilli();
            ConfigMap configMap = configMapResource.createOrReplace(new ConfigMapBuilder()
                    .withNewMetadata().withName(dns1123Compatible).endMetadata()
                    .addToData("valueClass", valueClass.getName())
                    .addToData("value", new String(value))
                    .addToData("expiryTimestamp", String.valueOf(epochMilli))
                    .build());
            log("Upserted ConfigMap at " + configMap.getMetadata().getSelfLink() + " data " + configMap.getData());

        } else {
            ConfigMap configMap = configMapResource.createOrReplace(new ConfigMapBuilder()
                    .withNewMetadata().withName(dns1123Compatible).endMetadata()
                    .addToData("valueClass", valueClass.getName())
                    .addToData("value", new String(value))
                    .build());
            log("Upserted ConfigMap at " + configMap.getMetadata().getSelfLink() + " data " + configMap.getData());
        }
        return true;
    }


    /**
     * Attempt to get the given key from the backing store. Looks up the given key and attempts to get it. If the
     * requested key couldn't be found then the value of the returned {@link SimpleCacheObject} will be empty. A new
     * cache object should be returned for each call to this method. If the key can't be found, then the expected class
     * should be <code>Object.class</code> inside the returned object. The metadata object in the cache object returned
     * from a backing store should be empty.
     *
     * @param key the key to lookup
     * @return a new cache object
     * @throws IllegalArgumentException if <code>key</code> is empty (once whitespace is trimmed)
     */
    @Override
    public SimpleCacheObject get(final String key) {
        if (key == null) {
            throw new IllegalArgumentException("key");
        }


        String dns1123Compatible = convertKeyToCompatible(key);
        Resource<ConfigMap, DoneableConfigMap> configMapResource = client.configMaps().inNamespace(namespace).withName(dns1123Compatible);
        ConfigMap configMap = configMapResource.get();

        if (configMap == null || configMap.getData().isEmpty()) {
            return new SimpleCacheObject(Object.class, Optional.empty());
        } else {
            try {
                if (configMap.getData().containsKey("expiryTimestamp")) {
                    if (Instant.now().isAfter(Instant.ofEpochMilli(Long.parseLong(configMap.getData().get("expiryTimestamp"))))) {
                        remove(key);
                        log("Timedout on key " + key);

                        return new SimpleCacheObject(Object.class, Optional.empty());
                    }
                }
                String className = Class.forName(configMap.getData().get("valueClass")).toString();
                byte[] data = configMap.getData().get("value").getBytes(StandardCharsets.UTF_8);

                log("get for key " + key + " with className " + className + " data " + new String(data));

                return new SimpleCacheObject(Class.forName(configMap.getData().get("valueClass")), Optional.of(configMap.getData().get("value").getBytes(StandardCharsets.UTF_8)));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Get request failed due to the class of the value not being found.", e);
            }
        }
    }


    /**
     * Remove the given key from the backing store. If the key is present it will be removed, otherwise nothing will happen.
     * The implementation must ensure that any supporting information that was originally added with the value such as the class name
     * must also be removed.
     *
     * @param key the key to remove
     * @return true if the key and value pair was removed, false otherwise
     */
    @Override
    public boolean remove(final String key) {
        if (key == null) {
            throw new IllegalArgumentException("key");
        }

        String dns1123Compatible = convertKeyToCompatible(key);
        try {
            Resource<ConfigMap, DoneableConfigMap> configMapResource = client.configMaps().inNamespace(namespace).withName(dns1123Compatible);
            ConfigMap configMap = configMapResource.get();

            if (configMap == null || configMap.getData().isEmpty()) {
                return false;
            }
            log("remove " + key);
            return client.configMaps().inNamespace(namespace).withName(dns1123Compatible).delete();
        } catch (KubernetesClientException e) {
            throw new RuntimeException("Get request failed due to the class of the value not being found.", e);
        }
    }

    /**
     * Get a stream of all keys with a given prefix. This method acts as a way to query the backing store to find which
     * keys it has. The returned list will have all the keys that start with the given string in it.
     *
     * @param prefix the prefix to look for
     * @return a stream of valid keys
     */
    @Override
    public Stream<String> list(final String prefix) {
        if (prefix == null) {
            throw new NullPointerException();
        }
        String dns1123Compatible = convertKeyToCompatible(prefix);

        return client.configMaps().inNamespace(namespace).list().getItems()
                .stream()
                .filter(configMap -> configMap.getMetadata().getName().startsWith(dns1123Compatible))
                .filter(configMap -> (!configMap.getData().containsKey("expiryTimestamp")) ||
                        (!Instant.now().isAfter(Instant.ofEpochMilli(Long.parseLong(configMap.getData().get("expiryTimestamp"))))))
                .map(configMap -> configMap.getMetadata().getName())
                .distinct();
    }


    private static void log(final String action, final Object obj) {
        LOGGER.info("{}: {}", action, obj);
    }

    private static void log(final String action) {
        LOGGER.info(action);
    }
}
