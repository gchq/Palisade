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

import java.util.stream.Stream;

/**
 * This class is used to pass back to the data service the stream of data in the
 * format expected by the client, along with any error/info messages for the client.
 *
 * @param <RAW_DATA_TYPE> A Java class that each record of the data is expected to
 *                   be returned to the client as.
 */
public class DataReaderResponse<RAW_DATA_TYPE> {
    private Stream<RAW_DATA_TYPE> data;
    private String message;

    // no args constructor required
    public DataReaderResponse() {
    }

    /**
     * Default constructor
     *
     * @param data A stream of data in the format expected by the client.
     */
    public DataReaderResponse(final Stream<RAW_DATA_TYPE> data) {
        this.data = data;
    }

    /**
     * Default constructor
     *
     * @param data A stream of data in the format expected by the client.
     * @param message an error/info message to be returned to the client.
     */
    public DataReaderResponse(final Stream<RAW_DATA_TYPE> data, final String message) {
        this.data = data;
        this.message = message;
    }

    public Stream<RAW_DATA_TYPE> getData() {
        return data;
    }

    public void setData(final Stream<RAW_DATA_TYPE> data) {
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

        final DataReaderResponse<?> that = (DataReaderResponse<?>) o;

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
