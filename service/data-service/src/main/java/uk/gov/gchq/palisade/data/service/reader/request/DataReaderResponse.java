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

import java.io.InputStream;

/**
 * This class is used to pass back to the data service the stream of data in the
 * format expected by the client, along with any error/info messages for the client.
 */
public class DataReaderResponse {
    private InputStream data;
    private String message;

    // no args constructor required
    public DataReaderResponse() {
    }

    /**
     * @param data an {@link InputStream} of data.
     * @return the {@link DataReaderResponse}
     */
    public DataReaderResponse data(final InputStream data) {
        this.data = data;
        return this;
    }

    /**
     * @param message an error/info message to be returned to the client
     * @return the {@link DataReaderResponse}
     */
    public DataReaderResponse message(final String message) {
        this.message = message;
        return this;
    }

    public InputStream getData() {
        return data;
    }

    public void setData(final InputStream data) {
        this.data = data;
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
                .append(data, that.data)
                .append(message, that.message)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 37)
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
