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

import com.google.common.collect.Sets;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import uk.gov.gchq.palisade.util.FieldGetter;
import uk.gov.gchq.palisade.util.FieldSetter;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;

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
public class User implements Cloneable {
    public static final String NAMESPACE = "User";
    public static final String USER_ID = "userId";
    public static final String ROLES = "roles";
    public static final String AUTHS = "auths";

    private static final Map<String, FieldGetter<User>> FIELD_GETTERS = createFieldGetters();
    private static final Map<String, FieldSetter<User>> FIELD_SETTERS = createFieldSetters();

    private UserId userId;
    private Set<String> roles = new HashSet<>();
    private Set<String> auths = new HashSet<>();

    public Object getField(final String reference) {
        return Util.getField(this, FIELD_GETTERS, reference);
    }

    public void setField(final String reference, final Object value) {
        Util.setField(this, FIELD_SETTERS, reference, value);
    }

    /**
     * Sets the userId to a {@link UserId} with the given userId string.
     *
     * @param userId the unique user ID string.
     * @return this User instance.
     */
    public User userId(final String userId) {
        requireNonNull(userId, "The user id cannot be set to null.");
        return userId(new UserId().id(userId));
    }

    /**
     * Sets the userId.
     *
     * @param userId the userId
     * @return this User instance.
     */
    public User userId(final UserId userId) {
        requireNonNull(userId, "The user id cannot be set to null.");
        this.userId = userId;
        return this;
    }

    public UserId getUserId() {
        requireNonNull(userId, "The user id has not been set.");
        return userId;
    }

    public void setUserId(final UserId userId) {
        userId(userId);
    }

    /**
     * Adds the user auths.
     *
     * @param auths the user auths to add
     * @return this User instance.
     */
    public User auths(final String... auths) {
        requireNonNull(auths, "Cannot add null auths.");
        Collections.addAll(this.auths, auths);
        return this;
    }

    public User auths(final Set<String> auths) {
        requireNonNull(auths, "Cannot add null auths.");
        this.auths.addAll(auths);
        return this;
    }

    public void setAuths(final Set<String> auths) {
        auths(auths);
    }

    public Set<String> getAuths() {
        // auths cannot be null
        return auths;
    }

    /**
     * Adds the user roles.
     *
     * @param roles the user roles to add
     * @return this User instance.
     */
    public User roles(final String... roles) {
        requireNonNull(auths, "Cannot add null roles.");
        Collections.addAll(this.roles, roles);
        return this;
    }

    public void setRoles(final Set<String> roles) {
        roles(roles);
    }

    public User roles(final Set<String> roles) {
        requireNonNull(auths, "Cannot add null roles.");
        this.roles.addAll(roles);
        return this;
    }

    public Set<String> getRoles() {
        // roles cannot be null
        return roles;
    }

    public User clone() {
        User clone;
        try {
            clone = (User) super.clone();
        } catch (final CloneNotSupportedException e) {
            clone = new User();
        }
        clone.userId = userId.clone();
        clone.roles = Sets.newHashSet(roles);
        clone.auths = Sets.newHashSet(auths);
        return clone;
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

    private static Map<String, FieldGetter<User>> createFieldGetters() {
        Map<String, FieldGetter<User>> map = new HashMap<>();
        map.put(USER_ID, (user, subfield) -> user.userId.getField(subfield));
        map.put(ROLES, (user, subfield) -> user.getRoles());
        map.put(AUTHS, (user, subfield) -> user.getAuths());
        return Collections.unmodifiableMap(map);
    }

    private static Map<String, FieldSetter<User>> createFieldSetters() {
        Map<String, FieldSetter<User>> map = new HashMap<>();
        map.put(USER_ID, (user, subfield, value) -> user.userId.setField(subfield, value));
        map.put(ROLES, (user, subfield, value) -> user.setRoles(((Set<String>) value)));
        map.put(AUTHS, (user, subfield, value) -> user.setAuths(((Set<String>) value)));
        return Collections.unmodifiableMap(map);
    }
}
