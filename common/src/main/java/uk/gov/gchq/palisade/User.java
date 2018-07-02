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

package uk.gov.gchq.palisade;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * A {@link User} contains the details about a user of Palisade. It contains
 * their unique user ID {@link UserId}, their roles and their auths.
 * </p>
 * <p>
 * The user roles are based on the role or function of the user's job. For example it could be used for deciding what actions users are allowed to perform, such as READ/WRITE.
 * </p>
 * <p>
 * The user auths are used specifically to decide what visibilities users can see.
 * </p>
 */
public class User {
    private UserId userId = new UserId();
    private Set<String> roles = new HashSet<>();
    private Set<String> auths = new HashSet<>();

    public Set<String> getAuths() {
        return auths;
    }

    public Set<String> getRoles() {
        return roles;
    }

    /**
     * Sets the userId to a {@link UserId} with the given userId string.
     *
     * @param userId the unique user ID string.
     * @return this User instance.
     */
    public User userId(final String userId) {
        return userId(new UserId(userId));
    }

    /**
     * Sets the userId.
     *
     * @param userId the userId
     * @return this User instance.
     */
    public User userId(final UserId userId) {
        this.userId = userId;
        return this;
    }

    /**
     * Adds the user auths.
     *
     * @param auths the user auths to add
     * @return this User instance.
     */
    public User auths(final String... auths) {
        Collections.addAll(this.auths, auths);
        return this;
    }

    /**
     * Adds the user roles.
     *
     * @param roles the user roles to add
     * @return this User instance.
     */
    public User roles(final String... roles) {
        Collections.addAll(this.roles, roles);
        return this;
    }

    public UserId getUserId() {
        return userId;
    }

    public void setUserId(final UserId userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final User user = (User) o;

        return new EqualsBuilder()
                .append(userId, user.userId)
                .append(roles, user.roles)
                .append(auths, user.auths)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(11, 19)
                .append(userId)
                .append(roles)
                .append(auths)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("userId", userId)
                .append("roles", roles)
                .append("auths", auths)
                .toString();
    }
}
