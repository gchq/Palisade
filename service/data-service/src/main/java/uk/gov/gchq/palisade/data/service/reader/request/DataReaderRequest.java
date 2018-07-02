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

import uk.gov.gchq.palisade.Justification;
import uk.gov.gchq.palisade.ToStringBuilder;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.policy.Rules;
import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.service.request.Request;

/**
 * This class is used to request that the {@link uk.gov.gchq.palisade.data.service.reader.DataReader}
 * read a resource and apply the necessary rules.
 *
 * @param <RULES_DATA_TYPE> is the Java class that the Rules expect the data to be in the format of.
 */
public class DataReaderRequest<RULES_DATA_TYPE> extends Request {
    private Resource resource;
    private User user;
    private Justification justification;
    private Rules rules;

    // no-args constructor required
    public DataReaderRequest() {
    }

    /**
     * Default constructor
     *
     * @param resource The resource to be accessed
     * @param user The user that requested the data
     * @param justification The Justification that the user provided for why they want the data
     * @param rules The list of rules to be applied to the data to ensure policy compliance
     * @param <RULES_DATA_TYPE> is the Java class that the Rules expect the data to be in the format of.
     */
    public <RULES_DATA_TYPE> DataReaderRequest(final Resource resource, final User user, final Justification justification, final Rules<RULES_DATA_TYPE> rules) {
        this.resource = resource;
        this.user = user;
        this.justification = justification;
        this.rules = rules;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(final Resource resource) {
        this.resource = resource;
    }

    public User getUser() {
        return user;
    }

    public void setUser(final User user) {
        this.user = user;
    }

    public Justification getJustification() {
        return justification;
    }

    public void setJustification(final Justification justification) {
        this.justification = justification;
    }


    public Rules<RULES_DATA_TYPE> getRules() {
        return rules;
    }

    public void setRules(final Rules<RULES_DATA_TYPE> rules) {
        this.rules = rules;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final DataReaderRequest<?> that = (DataReaderRequest<?>) o;

        return new EqualsBuilder()
                .append(resource, that.resource)
                .append(user, that.user)
                .append(justification, that.justification)
                .append(rules, that.rules)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 73)
                .append(resource)
                .append(user)
                .append(justification)
                .append(rules)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("resource", resource)
                .append("user", user)
                .append("justification", justification)
                .append("rules", rules)
                .toString();
    }
}
