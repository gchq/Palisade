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

import uk.gov.gchq.palisade.ToStringBuilder;

import static java.util.Objects.requireNonNull;

/**
 * This class is used to pass back to the data service the stream of data in the
 * format expected by the client, along with any error/info messages for the client.
 */
public class DataReaderResponse {
    private ResponseWriter writer;
    private String message;

    // no args constructor required
    public DataReaderResponse() {
    }

    /**
     * Set the writer object for this response.
     *
     * @param writer the data writer object
     * @return the {@link DataReaderResponse}
     */
    public DataReaderResponse writer(final ResponseWriter writer) {
        requireNonNull(writer, "The writer cannot be set to null.");
        this.writer = writer;
        return this;
    }

    /**
     * @param message an error/info message to be returned to the client
     * @return the {@link DataReaderResponse}
     */
    public DataReaderResponse message(final String message) {
        requireNonNull(message, "The message cannot be set to null.");
        this.message = message;
        return this;
    }

    public ResponseWriter getWriter() {
        requireNonNull(writer, "The writer has not been set.");
        return writer;
    }

    public void setWriter(final ResponseWriter writer) {
        writer(writer);
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

        final DataReaderResponse that = (DataReaderResponse) o;

        return new EqualsBuilder()
                .append(writer, that.writer)
                .append(message, that.message)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 37)
                .append(writer)
                .append(message)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("data", writer)
                .append("message", message)
                .toString();
    }
}
