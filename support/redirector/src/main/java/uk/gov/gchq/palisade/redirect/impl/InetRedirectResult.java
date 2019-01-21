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

package uk.gov.gchq.palisade.redirect.impl;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import uk.gov.gchq.palisade.redirect.RedirectionResult;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import static java.util.Objects.requireNonNull;

/**
 * The result of a redirection request. Instances of this class contain all the information necessary to redirect a client
 * request to a Palisade service that is based on a hostname/port pair endpoint..
 */
public class InetRedirectResult implements RedirectionResult<SocketAddress> {
    /**
     * Where the request should be re-directed to.
     */
    private final SocketAddress destination;

    /**
     * Create a new redirection result from the given host and port.
     *
     * @param hostName the host name
     * @param port     the port number
     */
    public InetRedirectResult(final String hostName, final int port) {
        this(new InetSocketAddress(hostName, port));
        requireNonNull(hostName);
    }

    /**
     * Create a new redirection result from the given address.
     *
     * @param destination address of destination service
     */
    public InetRedirectResult(final SocketAddress destination) {
        requireNonNull(destination, "destination");
        this.destination = destination;
    }

    @Override
    public SocketAddress getRedirectResult() {
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

        InetRedirectResult that = (InetRedirectResult) o;

        return new EqualsBuilder()
                .append(getRedirectResult(), that.getRedirectResult())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 23)
                .append(getRedirectResult())
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
