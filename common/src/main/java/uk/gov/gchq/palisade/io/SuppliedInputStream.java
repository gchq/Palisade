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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

/**
 * A {@code SuppliedInputStream} is a lazy {@link InputStream} that fetches
 * bytes from the provided {@link Supplier} of bytes. When {@link #read()} is
 * called, an initial array of bytes is fetched from the supplier - this is
 * then cached. Subsequent reads will return the next byte in the cached byte
 * array. When we reach the end of the cached byte array, the next byte array
 * is fetched from the {@link Supplier}. When the supplier returns null or
 * an empty array the input stream is terminated.
 *
 * @see BytesSuppliedInputStream
 */
public class SuppliedInputStream extends InputStream {
    private static final Logger LOGGER = LoggerFactory.getLogger(SuppliedInputStream.class);
    private static final int END_MARKER = -1;

    private final Supplier<byte[]> supplier;
    private byte[] bytes = null;
    private int bytesCount;
    private int pointer;
    private boolean end;

    public SuppliedInputStream(final Supplier<byte[]> supplier) {
        requireNonNull(supplier, "supplier is required");
        this.supplier = supplier;
    }

    @Override
    public int read() throws IOException {
        return end ? END_MARKER : continueReading();
    }

    private int continueReading() {
        final boolean pointerAtEnd = pointer >= bytesCount;
        if (pointerAtEnd) {
            loadMoreBytes();
        }
        return end ? END_MARKER : getByte() & 0xff;
    }

    private byte getByte() {
        byte b = bytes[pointer++];
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Reading byte {}", new String(new byte[]{b}));
        }
        return b;
    }

    private void loadMoreBytes() {
        LOGGER.debug("Requesting more bytes");
        bytes = supplier.get();
        pointer = 0;
        bytesCount = isNull(bytes) ? 0 : bytes.length;
        end = bytesCount <= 0;
        if (LOGGER.isDebugEnabled()) {
            if (end) {
                LOGGER.debug("Reached the end of the buffer");
            } else {
                LOGGER.debug("Loaded {} bytes {}", bytesCount, new String(bytes, 0, bytesCount, StandardCharsets.UTF_8));
            }
        }
    }
}
