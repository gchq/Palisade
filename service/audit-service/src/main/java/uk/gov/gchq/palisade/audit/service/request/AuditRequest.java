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

package uk.gov.gchq.palisade.audit.service.request;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.ToStringBuilder;
import uk.gov.gchq.palisade.service.request.Request;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

/**
 * This is the abstract class that is passed to the {@link uk.gov.gchq.palisade.audit.service.AuditService}
 * to be able to store an audit record. The default information is when was the audit record created and by what server
 */
public class AuditRequest extends Request {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuditRequest.class);

    private final Date timestamp;
    private final String serverIp;
    private final String serverHostname;

    // no-arg constructor required
    public AuditRequest() {
        timestamp = new Date();
        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        serverHostname = inetAddress.getHostName();
        serverIp = inetAddress.getHostAddress();
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getServerIp() {
        return serverIp;
    }

    public String getServerHostname() {
        return serverHostname;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final AuditRequest that = (AuditRequest) o;
        return new EqualsBuilder()
                //TODO should appendSuper be here, if so it needs adding on other objects where it is missing
                .appendSuper(super.equals(o))
                .append(timestamp, that.timestamp)
                .append(serverIp, that.serverIp)
                .append(serverHostname, that.serverHostname)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 37)
                .appendSuper(super.hashCode())
                .append(timestamp)
                .append(serverIp)
                .append(serverHostname)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("timestamp", timestamp)
                .append("serverIp", serverIp)
                .append("serverHostname", serverHostname)
                .toString();
    }
}
