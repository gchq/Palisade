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
package uk.gov.gchq.palisade.service.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import static java.util.Objects.requireNonNull;

public class InitialConfig {

    @JsonProperty("config")
    private final Map<String, String> configMap = new HashMap<>();

    public InitialConfig() {
    }

    @JsonIgnore
    public Map<String, String> getConfig() {
        //never null
        return Collections.unmodifiableMap(configMap);
    }

    public InitialConfig putAll(final Map<String, String> map) {
        requireNonNull(map, "map");
        configMap.putAll(map);
        return this;
    }

    public InitialConfig put(final String key, final String value) {
        requireNonNull(key, "key");
        requireNonNull(value, "value");
        configMap.put(key, value);
        return this;
    }

    public String get(final String key) {
        requireNonNull(key, "key");
        String value = configMap.get(key);
        if (value == null) {
            throw new NoSuchElementException("no such key " + key);
        } else {
            return value;
        }
    }

    public String getOrDefault(final String key, final String defaultValue) {
        requireNonNull(key, "key");
        return configMap.getOrDefault(key, defaultValue);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        InitialConfig that = (InitialConfig) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
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
                .appendSuper(super.hashCode())
                .append(configMap)
                .toHashCode();
    }
}
