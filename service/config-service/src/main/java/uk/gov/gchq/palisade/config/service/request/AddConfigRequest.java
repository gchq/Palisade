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
package uk.gov.gchq.palisade.config.service.request;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonIgnore;

import uk.gov.gchq.palisade.exception.ForbiddenException;
import uk.gov.gchq.palisade.service.ServiceState;

import static java.util.Objects.requireNonNull;

/**
 * A request class that is used by system internal services to add configurations for clients/services into Palisades.
 * This should NOT be used by clients.
 * <p>
 * This class has an {@link java.util.Optional} service field that is used to specify which {@link
 * uk.gov.gchq.palisade.service.Service} to request the configuration for. If this is left empty, then the Palisade
 * client configuration is set, i.e. the "anonymous" configuration. Note that there may be additional
 * authentication/authorisation constraints placed upon requestees. This means that for example, a client may not be
 * able to set configuration details for a particular service.
 */
public class AddConfigRequest extends GetConfigRequest {

    private ServiceState config;

    /**
     * Create an empty request.
     */
    public AddConfigRequest() {
    }

    /**
     * The current configuration is returned.
     *
     * @return the configuration
     */
    public ServiceState getConfig() {
        requireNonNull(config, "config cannot be left null");
        return config;
    }

    /**
     * Set the configuration to be added to the service.
     *
     * @param config the configuration
     * @return this object
     */
    public AddConfigRequest config(final ServiceState config) {
        requireNonNull(config, "config");
        this.config = config;
        return this;
    }

    /**
     * Set the configuration to be added to the service.
     *
     * @param config the configuration
     */
    public void setConfig(final ServiceState config) {
        config(config);
    }

    @JsonIgnore
    @Override
    public void setOriginalRequestId(final String originalRequestId) {
        throw new ForbiddenException("Should not call AddConfigRequest.setOriginalRequestId()");
    }

    @JsonIgnore
    @Override
    public String getOriginalRequestId() {
        throw new ForbiddenException("Should not call AddConfigRequest.getOriginalRequestId()");
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

        AddConfigRequest that = (AddConfigRequest) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(getConfig(), that.getConfig())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 23)
                .appendSuper(super.hashCode())
                .append(getConfig())
                .toHashCode();
    }
}
