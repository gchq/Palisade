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
package uk.gov.gchq.palisade.config.service.impl;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import uk.gov.gchq.palisade.config.service.request.GetConfigRequest;
import uk.gov.gchq.palisade.service.request.InitialConfig;

import static java.util.Objects.requireNonNull;

/**
 * Request to place some configuration data into a {@link uk.gov.gchq.palisade.config.service.InitialConfigurationService}.
 */
public class PutConfigRequest extends GetConfigRequest {
    /**
     * The configuration being added.
     */
    private InitialConfig config;

    /**
     * Gets the configuration in this request.
     *
     * @return the configuration
     */
    public InitialConfig getConfig() {
        requireNonNull(config, "config must be set");
        return config;
    }

    /**
     * Set the configuration.
     *
     * @param config the configuration
     * @return this object
     */
    public PutConfigRequest config(final InitialConfig config) {
        requireNonNull(config, "config");
        this.config = config;
        return this;
    }

    /**
     * Set the configuration.
     *
     * @param config the configuration
     */
    public void setConfig(final InitialConfig config) {
        config(config);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("config", config)
                .toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PutConfigRequest that = (PutConfigRequest) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(getConfig(), that.getConfig())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(getConfig())
                .toHashCode();
    }
}
