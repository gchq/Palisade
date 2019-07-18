/*
 * Copyright 2019 Crown Copyright
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
package uk.gov.gchq.palisade.audit.service.request;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import uk.gov.gchq.palisade.ToStringBuilder;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.rule.Rules;

import static java.util.Objects.requireNonNull;

/**
 * This is one of the objects that is passed to the {@link uk.gov.gchq.palisade.audit.service.AuditService}
 * to be able to store an audit record. This class extends {@link AuditRequest} This class
 * is used for the indication to the Audit logs that data has started to be sent to the client
 */
public class ReadResponseAuditRequest extends AuditRequest {
    private LeafResource resource;
    private Rules rulesApplied;

    public ReadResponseAuditRequest() {
    }

    /**
     * @param rulesApplied {@link Rules} is the rules that are being applied to this resource for this request
     * @return the {@link ReadRequestExceptionAuditRequest}
     */
    public ReadResponseAuditRequest rulesApplied(final Rules rulesApplied) {
        requireNonNull(rulesApplied, "The rulesApplied cannot be null");
        this.rulesApplied = rulesApplied;
        return this;
    }

    public Rules getRulesApplied() {
        requireNonNull(rulesApplied, "The field rulesApplied has not been set.");
        return rulesApplied;
    }

    public void setRulesApplied(final Rules rulesApplied) {
        rulesApplied(rulesApplied);
    }

    /**
     * @param resource {@link LeafResource} is the leafResource that is being read
     * @return the {@link ReadRequestExceptionAuditRequest}
     */
    public ReadResponseAuditRequest resource(final LeafResource resource) {
        requireNonNull(resource, "The leafResource cannot be null");
        this.resource = resource;
        return this;
    }

    public LeafResource getResource() {
        requireNonNull(resource, "The resource has not been set.");
        return resource;
    }

    public void setResource(final LeafResource resource) {
        resource(resource);
    }



    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ReadResponseAuditRequest that = (ReadResponseAuditRequest) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(rulesApplied, that.rulesApplied)
                .append(resource, that.resource)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 41)
                .appendSuper(super.hashCode())
                .append(rulesApplied)
                .append(resource)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("rulesApplied", rulesApplied)
                .append("resource", resource)
                .toString();
    }
}
