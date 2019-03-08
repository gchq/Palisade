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

import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.ToStringBuilder;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.rule.Rules;

import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * This is the high level API for the object that contains all the information
 * that the data service will require from the palisade, for the data service to
 * respond to requests for access to data.
 */
public class DataRequestConfig extends Request {
    private User user;
    private Context context;
    private Map<LeafResource, Rules> rules;

    public DataRequestConfig() {
    }

    public DataRequestConfig user(final User user) {
        requireNonNull(user, "User cannot be set to null.");
        this.user = user;
        return this;
    }

    public void setUser(final User user) {
        user(user);
    }

    public User getUser() {
        requireNonNull(user, "The user has not been set.");
        return user;
    }

    public DataRequestConfig context(final Context context) {
        requireNonNull(context, "Context cannot be set to null.");
        this.context = context;
        return this;
    }

    public void setContext(final Context context) {
        context(context);
    }

    public Context getContext() {
        requireNonNull(context, "The context has not been set.");
        return context;
    }

    public DataRequestConfig rules(final Map<LeafResource, Rules> rules) {
        requireNonNull(rules, "The rules are required.");
        this.rules = rules;
        return this;
    }

    public void setRules(final Map<LeafResource, Rules> rules) {
        rules(rules);
    }

    public Map<LeafResource, Rules> getRules() {
        requireNonNull(rules, "The Rules have not been set.");
        return rules;
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
                .appendSuper(super.equals(o))
                .append(user, that.user)
                .append(context, that.context)
                .append(rules, that.rules)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(53, 37)
                .appendSuper(super.hashCode())
                .append(user)
                .append(context)
                .append(rules)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("user", user)
                .append("context", context)
                .append("rules", rules)
                .toString();
    }
}
