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

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import uk.gov.gchq.palisade.ToStringBuilder;

import java.util.stream.Stream;

/**
 * This class is used to return to the client the stream of data in the expected
 * format along with any error/warning/info messages that the client should be aware of.
 *
 * @param <RAW_DATA_TYPE> The Java class that the data is expected to be returned as.
 */
public class ReadResponse<RAW_DATA_TYPE> {
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.CLASS,
            include = As.WRAPPER_OBJECT,
            property = "class"
    )
    private Stream<RAW_DATA_TYPE> data;

    private String message;

    // no-args constructor
    public ReadResponse() {
    }

    /**
     * Default constructor
     *
     * @param data a stream of data in the expected format
     */
    public ReadResponse(final Stream<RAW_DATA_TYPE> data) {
        this.data = data;
    }

    /**
     * Constructor if you also need to send a message with the stream of data
     *
     * @param data a stream of data in the expected format
     * @param message a message for the client
     */
    public ReadResponse(final Stream<RAW_DATA_TYPE> data, final String message) {
        this.data = data;
        this.message = message;
    }

    public Stream<RAW_DATA_TYPE> getData() {
        return data;
    }

    public void setData(final Stream<RAW_DATA_TYPE> data) {
        this.data = data;
    }

    @JsonGetter("data")
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.CLASS,
            include = As.WRAPPER_OBJECT,
            property = "class"
    )
    Object[] getDataArray() {
        return data.toArray();
    }

    @JsonSetter("data")
    void setDataArray(final RAW_DATA_TYPE[] data) {
        setData(Stream.of(data));
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ReadResponse<?> that = (ReadResponse<?>) o;

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
