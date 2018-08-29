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

package uk.gov.gchq.palisade.data.service.reader.request;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.ToStringBuilder;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.rule.Rules;
import uk.gov.gchq.palisade.service.request.Request;

import static java.util.Objects.requireNonNull;

/**
 * This class is used to request that the {@link uk.gov.gchq.palisade.data.service.reader.DataReader}
 * read a resource and apply the necessary rules.
 */
public class DataReaderRequest extends Request {
    private LeafResource resource;
    private User user;
    private Context context;
    private Rules rules;

    // no-args constructor required
    public DataReaderRequest() {
    }

    /**
     * @param resource the resource to be accessed
     * @return the {@link DataReaderRequest}
     */
    public DataReaderRequest resource(final LeafResource resource) {
        requireNonNull(resource, "The resource cannot be set to null.");
        this.resource = resource;
        return this;
    }

    /**
     * @param user the user that requested the data
     * @return the {@link DataReaderRequest}
     */
    public DataReaderRequest user(final User user) {
        requireNonNull(user, "The user cannot be set to null.");
        this.user = user;
        return this;
    }

    /**
     * @param context the Context that the user provided for why they want the data
     * @return the {@link DataReaderRequest}
     */
    public DataReaderRequest context(final Context context) {
        requireNonNull(context, "The context cannot be set to null.");
        this.context = context;
        return this;
    }

    /**
     * @param rules the list of rules to be applied to the data to ensure policy compliance
     * @return the {@link DataReaderRequest}
     */
    public DataReaderRequest rules(final Rules rules) {
        requireNonNull(rules, "The rules cannot be set to null.");
        this.rules = rules;
        return this;
    }

    public LeafResource getResource() {
        requireNonNull(resource, "The resource has not been set.");
        return resource;
    }

    public void setResource(final LeafResource resource) {
        resource(resource);
    }

    public User getUser() {
        requireNonNull(user, "The user has not been set.");
        return user;
    }

    public void setUser(final User user) {
        user(user);
    }

    public Context getContext() {
        requireNonNull(context, "The context has not been set.");
        return context;
    }

    public void setContext(final Context context) {
        context(context);
    }


    public Rules getRules() {
        requireNonNull(rules, "The rules have not been set.");
        return rules;
    }

    public void setRules(final Rules rules) {
        rules(rules);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final DataReaderRequest that = (DataReaderRequest) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(resource, that.resource)
                .append(user, that.user)
                .append(context, that.context)
                .append(rules, that.rules)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 73)
                .appendSuper(super.hashCode())
                .append(resource)
                .append(user)
                .append(context)
                .append(rules)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("resource", resource)
                .append("user", user)
                .append("justification", context)
                .append("rules", rules)
                .toString();
    }
}
