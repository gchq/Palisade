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

/**
 * A {@link UserId} uniquely identifies a {@link User}. By default the ID will
 * be set to "UNKNOWN".
 */
public class UserId {
    /**
     * The default user ID - "UNKNOWN".
     */
    public static final String UNKNOWN_USER_ID = "UNKNOWN";
    private final String id;

    /**
     * Constructs a {@link UserId} with the default user ID of "UNKNOWN".
     */
    public UserId() {
        this(UNKNOWN_USER_ID);
    }

    /**
     * Constructs a {@link UserId} with the provided user ID.
     *
     * @param id the unique ID of the {@link User}.
     */
    public UserId(final String id) {
        if (null == id) {
            this.id = UNKNOWN_USER_ID;
        } else {
            this.id = id;
        }
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final UserId userId = (UserId) o;

        return new EqualsBuilder()
                .append(id, userId.id)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 37)
                .append(id)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .toString();
    }
}
