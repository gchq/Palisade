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
import java.util.Arrays;
import java.util.function.Supplier;

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

    private final Supplier<byte[]> supplier;
    private byte[] bytes = null;
    private int bytesCount;
    private int i = 0;
    private boolean end;

    public SuppliedInputStream(final Supplier<byte[]> supplier) {
        requireNonNull(supplier, "supplier is required");
        this.supplier = supplier;
    }

    @Override
    public int read() throws IOException {
        if (end) {
            return -1;
        }

        if (null == bytes || i >= bytesCount) {
            LOGGER.debug("Requesting more bytes");
            bytes = supplier.get();
            i = 0;
            if (null == bytes) {
                bytesCount = 0;
            } else {
                bytesCount = bytes.length;
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Loaded {} bytes {}", bytesCount, new String(Arrays.copyOf(bytes, bytesCount)));
                }
            }
        }
        if (null == bytes || bytesCount == 0) {
            LOGGER.debug("Reached the end of the buffer");
            end = true;
            return -1;
        }

        byte b = bytes[i];
        if (null != bytes && LOGGER.isDebugEnabled()) {
            LOGGER.debug("Reading byte {}", new String(new byte[]{b}));
        }
        i++;
        return b & 0xff;
    }
}
