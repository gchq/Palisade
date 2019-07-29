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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static java.util.Objects.requireNonNull;

/**
 * This class is used to return to the client the {@link InputStream} of data
 * along with any error/warning/info messages that the client should be aware of.
 * <p>
 * The actual data stream can be retrieved once only. Clients should call {@link ReadResponse#asInputStream()} to obtain
 * an input stream for the data OR select for that data to be copied to a provided {@link OutputStream}. Any further attempts
 * to call one of these methods on an instance will result in an exception being thrown.
 * <p>
 * Concrete sub-classes of this class should call {@link ReadResponse#setUsed()} as soon as the data stream from Palisade
 * has been realised through either {@link ReadResponse#asInputStream()} or {@link ReadResponse#writeTo(OutputStream)}.
 */
public abstract class ReadResponse {
    private String message;
    /**
     * Specifies if the data stream has been retrieved from this response.
     */
    private volatile boolean isUsed;

    /**
     * Retrieves the data returned from the request as an {@link InputStream}. This method can only be called once.
     *
     * @return a stream of data from Palisade
     * @throws IOException if {@link ReadResponse#isUsed} returns {@code true}, or an underlying IO error occurs
     */
    public abstract InputStream asInputStream() throws IOException;

    /**
     * Instructs the data stream from Palisade be copied to the given {@link OutputStream}. This method can only be called
     * once.
     *
     * @param output the stream to copy to the data to
     * @return this object
     * @throws IOException if {@link ReadResponse#isUsed} returns {@code true}, or an underlying IO error occurs
     */
    public abstract ReadResponse writeTo(final OutputStream output) throws IOException;

    /**
     * Tests whether the data stream from this instance has already been retrieved, either as an {@link InputStream} or
     * copied to another stream.
     *
     * @return true if the stream has already been used
     * @see ReadResponse#asInputStream()
     * @see ReadResponse#writeTo(OutputStream)
     */
    public boolean isUsed() {
        return isUsed;
    }

    /**
     * Sets the data stream as retrieved.
     */
    protected void setUsed() {
        this.isUsed = true;
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
                .append(message, that.message)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 61)
                .append(message)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("message", message)
                .toString();
    }
}
