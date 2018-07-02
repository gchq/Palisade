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

import uk.gov.gchq.palisade.ToStringBuilder;

/**
 * A simple implementation of the {@link ConnectionDetail}
 */
public class SimpleConnectionDetail implements ConnectionDetail {
    public static final String DEFAULT_DETAILS = "No details";
    private String details;

    public SimpleConnectionDetail() {
        this(DEFAULT_DETAILS);
    }

    public SimpleConnectionDetail(final String details) {
        this.details = details;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(final String details) {
        this.details = details;
    }

    @Override
    public String _getClass() {
        return null;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final SimpleConnectionDetail that = (SimpleConnectionDetail) o;

        return new EqualsBuilder()
                .append(details, that.details)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(29, 31)
                .append(details)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("details", details)
                .toString();
    }
}
