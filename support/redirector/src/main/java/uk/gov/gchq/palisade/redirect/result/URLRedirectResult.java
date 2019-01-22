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

import java.net.URL;

import static java.util.Objects.requireNonNull;

/**
 * A redirection result that redirects clients to a different URL.
 */
public class URLRedirectResult implements RedirectionResult<URL> {
    /**
     * The URL being redirected to.
     */
    private final URL destination;

    /**
     * Create a new redirection based on a URL.
     *
     * @param destination new destination for the request
     */
    public URLRedirectResult(final URL destination) {
        requireNonNull(destination, "destination");
        this.destination = destination;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("destination", destination)
                .toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        URLRedirectResult that = (URLRedirectResult) o;

        return new EqualsBuilder()
                .append(getRedirectResult(), that.getRedirectResult())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 31)
                .append(getRedirectResult())
                .toHashCode();
    }

    @Override
    public URL getRedirectResult() {
        return destination;
    }
}
