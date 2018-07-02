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

package uk.gov.gchq.palisade.resource.service.request;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import uk.gov.gchq.palisade.ToStringBuilder;
import uk.gov.gchq.palisade.service.request.Request;

/**
 * This class is used to request a list of {@link uk.gov.gchq.palisade.resource.Resource}'s
 * from the {@link uk.gov.gchq.palisade.resource.service.ResourceService} based on the format of those resources.
 * For example getting a list of all {@link uk.gov.gchq.palisade.resource.Resource}'s where the format is CSV.
 */
public class GetResourcesByFormatRequest extends Request {
    private String format;

    // no-args constructor required
    public GetResourcesByFormatRequest() {
    }

    /**
     * Default constructor
     *
     * @param format The format of the {@link uk.gov.gchq.palisade.resource.Resource}'s that you want to know about.
     */
    public GetResourcesByFormatRequest(final String format) {
        this.format = format;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(final String format) {
        this.format = format;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final GetResourcesByFormatRequest that = (GetResourcesByFormatRequest) o;

        return new EqualsBuilder()
                .append(format, that.format)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31)
                .append(format)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("format", format)
                .toString();
    }
}
