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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Map;

import static java.util.Objects.requireNonNull;

public class ServicesConfig {

    private Map<String,String> configMap;

    public ServicesConfig() {}

    public Map<String,String> getConfig() {
        requireNonNull(configMap,"The config has not been set");
        return configMap;
    }

    public void setConfig(final Map<String,String> map) {
        config(map);
    }

    public ServicesConfig config(final Map<String,String> map) {
        requireNonNull(map,"map");
        this.configMap=map;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ServicesConfig that = (ServicesConfig) o;

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
