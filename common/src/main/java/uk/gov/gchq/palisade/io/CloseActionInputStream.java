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

package uk.gov.gchq.palisade.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import static java.util.Objects.requireNonNull;

/**
 * An input stream that allows a {@link Runnable} to be called when the stream is closed.
 */
public class CloseActionInputStream extends FilterInputStream {
    /**
     * The action to perform at stream close time.
     */
    private final Runnable closeAction;

    /**
     * Create an {@link InputStream} that will perform the given action when the {@link FilterInputStream#close()} is called.
     *
     * @param underlyingStream the stream to wrap
     * @param closeAction      the action to perform
     * @throws NullPointerException if anything is {@code null}
     */
    public CloseActionInputStream(final InputStream underlyingStream, final Runnable closeAction) {
        super(underlyingStream);
        requireNonNull(underlyingStream, "underlyingStream");
        requireNonNull(closeAction, "closeAction");
        this.closeAction = closeAction;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Will run the close action specified in the constructor AFTER calling {@link InputStream#close()} on the underlying
     * stream.
     */
    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            closeAction.run();
        }
    }
}
