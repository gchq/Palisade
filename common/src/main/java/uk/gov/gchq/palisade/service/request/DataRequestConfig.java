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
import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.rule.Rule;
import uk.gov.gchq.palisade.rule.Rules;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

/**
 * This is the high level API for the object that contains all the information
 * that the data service will require from the palisade, for the data service to
 * respond to requests for access to data.
 */
public class DataRequestConfig extends Request {
    private User user = new User();
    private Context context = new Context();
    private Map<Resource, Rules> rules = new HashMap<>();

    public DataRequestConfig() {
    }

    public DataRequestConfig user(final User user) {
        this.user = user;
        return this;
    }

    public DataRequestConfig context(final Context context) {
        this.context = context;
        return this;
    }

    public DataRequestConfig rules(final Map<Resource, Rules> rules) {
        requireNonNull(rules, "rules is required");
        this.rules = rules;
        return this;
    }

    public DataRequestConfig rules(final Resource resource, final Rules rules) {
        requireNonNull(resource, "resource is required");
        requireNonNull(rules, "rules is required");
        this.rules.put(resource, rules);
        return this;
    }

    public DataRequestConfig rule(final Resource resource, final String ruleId, final Rule rule) {
        return rule(resource, null, ruleId, rule);
    }

    public DataRequestConfig rule(final Resource resource, final String message, final String ruleId, final Rule rule) {
        requireNonNull(resource, "resource is required");
        requireNonNull(ruleId, "ruleId is required");
        requireNonNull(rule, "rule is required");

        Rules<?> resourceRules = rules.get(resource);
        if (null == resourceRules) {
            resourceRules = new Rules();
        }
        if (nonNull(message)) {
            resourceRules.message(message);
        }
        resourceRules.rule(ruleId, rule);
        return this;
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

    public DataRequestConfig setRules(final Map<Resource, Rules> rules) {
        this.rules = rules;
        return this;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(final Context context) {
        this.context = context;
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
                .append("justification", context)
                .append("rules", rules)
                .toString();
    }
}
