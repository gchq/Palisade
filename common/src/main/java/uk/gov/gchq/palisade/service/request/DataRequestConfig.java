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
import uk.gov.gchq.palisade.policy.MultiPolicy;
import uk.gov.gchq.palisade.policy.Policy;
import uk.gov.gchq.palisade.resource.Resource;

/**
 * This is the high level API for the object that contains all the information
 * that the data service will require from the palisade, for the data service to
 * respond to requests for access to data.
 */
public class DataRequestConfig extends Request {
    private User user = new User();
    private Justification justification = new Justification();
    private MultiPolicy multiPolicy = new MultiPolicy();

    public DataRequestConfig() {
    }

    public DataRequestConfig user(final User user) {
        this.user = user;
        return this;
    }

    public DataRequestConfig justification(final Justification justification) {
        this.justification = justification;
        return this;
    }

    public DataRequestConfig multiPolicy(final MultiPolicy multiPolicy) {
        this.multiPolicy = multiPolicy;
        return this;
    }

    public DataRequestConfig policy(final Resource resource, final Policy policy) {
        this.multiPolicy.setPolicy(resource, policy);
        return this;
    }

    public User getUser() {
        return user;
    }

    public void setUser(final User user) {
        this.user = user;
    }

    public MultiPolicy getMultiPolicy() {
        return multiPolicy;
    }

    public void setMultiPolicy(final MultiPolicy multiPolicy) {
        this.multiPolicy = multiPolicy;
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
                .appendSuper(super.equals(o))
                .append(user, that.user)
                .append(justification, that.justification)
                .append(multiPolicy, that.multiPolicy)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(53, 37)
                .appendSuper(super.hashCode())
                .append(user)
                .append(justification)
                .append(multiPolicy)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("user", user)
                .append("justification", justification)
                .append("multiPolicy", multiPolicy)
                .toString();
    }
}
