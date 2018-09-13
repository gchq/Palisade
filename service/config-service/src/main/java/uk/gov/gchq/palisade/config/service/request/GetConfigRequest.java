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

import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.service.Service;
import uk.gov.gchq.palisade.service.request.Request;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class GetConfigRequest extends Request {

    private Optional<User> user;

    private Optional<Class<? extends Service>> service;

    public GetConfigRequest() {
        user = Optional.empty();
        service = Optional.empty();
    }

    public Optional<User> getUser() {
        //never null
        return user;
    }

    public void setUser(final Optional<User> user) {
        user(user);
    }

    public GetConfigRequest user(final Optional<User> user) {
        requireNonNull(user, "user");
        this.user = user;
        return this;
    }

    public Optional<Class<? extends Service>> getService() {
        //never null
        return service;
    }

    public void setService(final Optional<Class<? extends Service>> service) {
        service(service);
    }

    public GetConfigRequest service(final Optional<Class<? extends Service>> service) {
        requireNonNull(service, "service");
        this.service = service;
        return this;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 41)
                .appendSuper(super.hashCode())
                .append(user)
                .append(service)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("user", user)
                .append("service", service)
                .toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        GetConfigRequest that = (GetConfigRequest) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(user, that.user)
                .append(service, that.service)
                .isEquals();
    }
}
