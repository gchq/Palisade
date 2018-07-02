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

import uk.gov.gchq.palisade.Justification;
import uk.gov.gchq.palisade.ToStringBuilder;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.policy.Rules;
import uk.gov.gchq.palisade.resource.Resource;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * This is the high level API for the object that contains all the information
 * that the data service will require from the palisade, for the data service to
 * respond to requests for access to data.
 */
public class DataRequestConfig extends Request {
    private User user;
    private Justification justification;
    private Map<Resource, Rules> rules;

    public DataRequestConfig() {
        this(new User(), new Justification(), new HashMap<>());
    }

    public DataRequestConfig(final User user, final Justification justification, final Map<Resource, Rules> rules) {
        this.user = user;
        this.justification = justification;
        this.rules = rules;
    }

    public User getUser() {
        return user;
    }

    public void setUser(final User user) {
        this.user = user;
    }

    public Map<Resource, Rules> getRules() {
        return rules;
    }

    public void setRules(final Map<Resource, Rules> rules) {
        this.rules = rules;
    }

    public <T> Rules<T> getResourceRules(final Resource resource) {
        Objects.requireNonNull(resource);
        final Rules resourceRules = rules.get(resource);
        if (null != resourceRules) {
            return resourceRules;
        }
        return new Rules<>();
    }

    public Justification getJustification() {
        return justification;
    }

    public void setJustification(final Justification justification) {
        this.justification = justification;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final DataRequestConfig that = (DataRequestConfig) o;

        return new EqualsBuilder()
                .append(user, that.user)
                .append(justification, that.justification)
                .append(rules, that.rules)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(53, 37)
                .append(user)
                .append(justification)
                .append(rules)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("user", user)
                .append("justification", justification)
                .append("rules", rules)
                .toString();
    }
}
