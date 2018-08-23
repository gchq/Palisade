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

package uk.gov.gchq.palisade.data.service.request;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import uk.gov.gchq.palisade.ToStringBuilder;

import java.io.InputStream;

import static java.util.Objects.requireNonNull;

/**
 * This class is used to return to the client the {@link InputStream} of data
 * along with any error/warning/info messages that the client should be aware of.
 */
public class ReadResponse {
    private InputStream data;
    private String message;

    public ReadResponse data(final InputStream data) {
        requireNonNull(data, "The data stream cannot be set to null.");
        this.data = data;
        return this;
    }

    public InputStream getData() {
        requireNonNull(data, "The data stream has not been set.");
        return data;
    }

    public void setData(final InputStream data) {
        data(data);
    }

    public ReadResponse message(final String message) {
        requireNonNull(message, "The message cannot be set to null.");
        this.message = message;
        return this;
    }

    public String getMessage() {
        requireNonNull(message, "The message has not been set.");
        return message;
    }

    public void setMessage(final String message) {
        message(message);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ReadResponse that = (ReadResponse) o;

        return new EqualsBuilder()
                .append(data, that.data)
                .append(message, that.message)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 61)
                .append(data)
                .append(message)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("data", data)
                .append("message", message)
                .toString();
    }
}
