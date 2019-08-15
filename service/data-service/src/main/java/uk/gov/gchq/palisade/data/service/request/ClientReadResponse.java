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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static java.util.Objects.requireNonNull;

/**
 * A read response that can be passed to clients. The {@link ClientReadResponse#asInputStream()} returns the given {@link InputStream}.
 * The {@link ClientReadResponse#writeTo(OutputStream)} method performs a straight copy to whatever {@link OutputStream} the client
 * provides. This method is unlikely to be needed by clients, but we have to provide it for symmetry.
 */
public class ClientReadResponse extends ReadResponse {

    /**
     * The stream the client can read from.
     */
    private final InputStream stream;

    /**
     * Create a response.
     *
     * @param stream the stream to provide to a client
     */
    public ClientReadResponse(final InputStream stream) {
        requireNonNull(stream, "stream");
        this.stream = stream;
    }

    @Override
    public InputStream asInputStream() {
        return stream;
    }

    @Override
    public void writeTo(final OutputStream output) throws IOException {
        requireNonNull(output, "output");
        //check this hasn't already been used
        boolean used = setUsed();
        if (used) {
            throw new IOException("writeTo can only be called once per instance");
        }

        IOUtils.copy(asInputStream(), output);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ClientReadResponse that = (ClientReadResponse) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(stream, that.stream)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(stream)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("stream", stream)
                .toString();
    }
}
