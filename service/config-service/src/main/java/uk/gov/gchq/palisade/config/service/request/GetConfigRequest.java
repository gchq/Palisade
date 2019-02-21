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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import uk.gov.gchq.palisade.service.Service;
import uk.gov.gchq.palisade.service.request.Request;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * A request class that is sent by services and clients wanting their initial configuration data. The class has an
 * {@link Optional} service field that is used to specify which {@link Service} to request the configuration for. If
 * this is left empty, then the Palisade client configuration is requested, i.e. the "anonymous" configuration. Note
 * that there may be additional authentication/authorisation constraints placed upon requestees. This means that for
 * example, a client may not be able to request configuration details for a particular service.
 */
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE,setterVisibility = JsonAutoDetect.Visibility.NONE,fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class GetConfigRequest extends Request {

    /**
     * The service for the configuration being requested.
     */
    private Optional<String> serviceClassName;

    /**
     * Create an empty request.
     */
    public GetConfigRequest() {
        serviceClassName = Optional.empty();
    }

    /**
     * Get the service requesting configuration.
     *
     * @return the optional serviceClassName
     */
    public Optional<Class<? extends Service>> getService() {
        //never null
        return serviceClassName.map(name -> {
            try {
                return Class.forName(name).asSubclass(Service.class);
            } catch (ClassNotFoundException e) {
                throw new NoClassDefFoundError(e.getLocalizedMessage());
            }
        });
    }

    /**
     * Get the service class as a string to avoid needing to create the {@link Class} object.
     */
    public Optional<String> getServiceAsString() {
        return serviceClassName;
    }

    /**
     * Set the {@link Service} that is requesting configuration.
     *
     * @param service an optional {@link Service}
     */
    public void setService(final Optional<Class<? extends Service>> service) {
        service(service);
    }

    /**
     * Set the {@link Service} that is requesting configuration.
     *
     * @param serviceClassName an optional {@link Service}
     * @return this object
     */
    public GetConfigRequest service(final Optional<Class<? extends Service>> serviceClassName) {
        requireNonNull(serviceClassName, "serviceClassName");
        this.serviceClassName = serviceClassName.map(Class::getTypeName);
        return this;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 41)
                .appendSuper(super.hashCode())
                .append(serviceClassName)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("serviceClassName", serviceClassName)
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

        GetConfigRequest that = (GetConfigRequest) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(serviceClassName, that.serviceClassName)
                .isEquals();
    }
}
