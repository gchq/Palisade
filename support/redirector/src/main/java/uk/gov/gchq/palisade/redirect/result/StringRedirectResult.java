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

package uk.gov.gchq.palisade.redirect.result;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import uk.gov.gchq.palisade.redirect.RedirectionResult;

import static java.util.Objects.requireNonNull;

/**
 * The result of a redirection request. Instances of this class contain all the information necessary to redirect a client
 * request to a Palisade service that is based on a string endpoint.
 */
public class StringRedirectResult implements RedirectionResult<String> {
    /**
     * Where the request should be re-directed to.
     */
    private final String destination;

    /**
     * Create a new redirection result from the given address.
     *
     * @param destination address of destination service
     */
    public StringRedirectResult(final String destination) {
        requireNonNull(destination, "destination");
        this.destination = destination;
    }

    @Override
    public String get() {
        return destination;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StringRedirectResult that = (StringRedirectResult) o;

        return new EqualsBuilder()
                .append(get(), that.get())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 23)
                .append(get())
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("destination", destination)
                .toString();
    }
}
