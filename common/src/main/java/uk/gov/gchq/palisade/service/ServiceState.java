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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

/**
 * This is the configuration class that clients and services can use to initialise themselves. Instances of this class
 * can be obtained from the configuration service.
 * <p>
 * All methods in this class throw {@link NullPointerException} unless otherwise stated for <code>null</code>
 * parameters.
 * <p>
 * This class may contain {@code null} values.
 */
public class ServiceState {

    /**
     * The map of configuration data.
     */
    @JsonProperty("config")
    private final Map<String, String> configMap = new HashMap<>();

    /**
     * Create empty configuration.
     */
    public ServiceState() {
    }

    /**
     * Gets an unmodifiable copy of the configuration.
     *
     * @return an unmodifiable map
     */
    @JsonIgnore
    public Map<String, String> getConfig() {
        //never null
        return Collections.unmodifiableMap(configMap);
    }

    /**
     * Copies all of the entries in the given map into this configuration object.
     *
     * @param map the map to copy
     * @return this object
     */
    public ServiceState putAll(final Map<String, String> map) {
        requireNonNull(map, "map");
        //filter out any null keys
        map.entrySet()
                .stream()
                .filter(e -> nonNull(e.getKey()))
                .forEach(e -> configMap.put(e.getKey(), e.getValue()));
        return this;
    }

    /**
     * Enters the given key and value pair into the configuration.
     *
     * @param key   the configuration key
     * @param value the configuration value, maybe {@code null}
     * @return this object
     */
    public ServiceState put(final String key, final String value) {
        requireNonNull(key, "key");
        configMap.put(key, value);
        return this;
    }

    /**
     * Gets a specific configuration value.
     *
     * @param key the key to lookup
     * @return the value associated with {@code key}
     * @throws NoSuchElementException if {@code key} couldn't be found
     */
    public String get(final String key) throws NoSuchElementException {
        requireNonNull(key, "key");
        if (configMap.containsKey(key)) {
            return configMap.get(key);
        } else {
            throw new NoSuchElementException("no such key " + key);
        }
    }

    /**
     * Gets a specific configuration value or returns the default provided
     *
     * @param key          the key to lookup
     * @param defaultValue the default value. May be {@code null}
     * @return the value if present or {@code defaultValue}
     */
    public String getOrDefault(final String key, final String defaultValue) {
        requireNonNull(key, "key");
        return configMap.getOrDefault(key, defaultValue);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ServiceState that = (ServiceState) o;

        return new EqualsBuilder()
                .append(configMap, that.configMap)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("configMap", configMap)
                .toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 43)
                .append(configMap)
                .toHashCode();
    }
}
