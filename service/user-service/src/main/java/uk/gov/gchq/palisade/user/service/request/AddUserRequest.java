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

package uk.gov.gchq.palisade.user.service.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.exception.ForbiddenException;
import uk.gov.gchq.palisade.service.request.Request;

import static java.util.Objects.requireNonNull;

/**
 * An {@code AddUserRequest} is a {@link Request} that is passed to the {@link uk.gov.gchq.palisade.user.service.UserService}
 * to add a {@link User}.
 */
@JsonIgnoreProperties(value = {"originalRequestId"})
public class AddUserRequest extends Request {
    private User user;

    /**
     * Constructs an {@link AddUserRequest} without a user.
     */
    public AddUserRequest() {
    }

    public AddUserRequest user(final User user) {
        requireNonNull(user, "The user cannot be set to null.");
        this.user = user;
        return this;
    }

    public User getUser() {
        requireNonNull(user, "The user has not been set.");
        return user;
    }

    public void setUser(final User user) {
        user(user);
    }

    @Override
    public void setOriginalRequestId(final String originalRequestId) {
        throw new ForbiddenException("Should not call AddUserRequest.setOriginalRequestId()");
    }

    @Override
    public String getOriginalRequestId() {
        throw new ForbiddenException("Should not call AddUserRequest.getOriginalRequestId()");
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final AddUserRequest that = (AddUserRequest) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(user, that.user)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 61)
                .appendSuper(super.hashCode())
                .append(user)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("user", user)
                .toString();
    }
}
