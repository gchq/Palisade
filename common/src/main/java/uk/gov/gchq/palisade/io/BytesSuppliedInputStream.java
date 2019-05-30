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
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * A {@code SuppliedInputStream} is a lazy {@link InputStream} that fetches
 * bytes from the provided {@link Supplier} of {@link Bytes}. When {@link #read()} is
 * called, an initial array of bytes is fetched from the supplier - this is
 * then cached. Subsequent reads will return the next byte in the cached byte
 * array. When we reach the end of the cached byte array, the next byte array
 * is fetched from the {@link Supplier}. When the supplier returns null or
 * an empty array the input stream is terminated.
 *
 * @see SuppliedInputStream
 */
public class BytesSuppliedInputStream extends InputStream {
    private static final Logger LOGGER = LoggerFactory.getLogger(BytesSuppliedInputStream.class);

    private final Supplier<Bytes> supplier;
    private byte[] bytes = null;
    private int bytesCount;
    private int index = 0;
    private boolean end;

    public BytesSuppliedInputStream(final Supplier<Bytes> supplier) {
        requireNonNull(supplier, "supplier is required");
        this.supplier = supplier;
    }

    private void resetBuffer() {
        bytes = null;
        bytesCount = 0;
        index = 0;
    }

    private int refillBuffer(final byte[] b, final int off, final int len) {
        requireNonNull(b, "buffer cannot be null");
        if (off < 0 || len < 0) {
            throw new IndexOutOfBoundsException("offset or length are negative");
        }
        if (len > (b.length - off)) {
            throw new IndexOutOfBoundsException("length overruns array length");
        }
        if (len == 0) {
            return 0;
        }
        if (end) {
            return -1;
        }
        if (null == bytes || index >= bytesCount) {
            final Bytes newBytes = supplier.get();
            index = 0;
            if (null == newBytes) {
                resetBuffer();
            } else if (((bytes = newBytes.getBytes()) != null) && ((bytesCount = newBytes.getCount()) > bytes.length)) {
                bytesCount = bytes.length;
            }
        }
        if (null == bytes || bytesCount == 0) {
            LOGGER.debug("Reached the end of the buffer");
            end = true;
            return -1;
        }

        int copyAmount = len;
        if ((bytesCount - index) < copyAmount) {
            copyAmount = bytesCount - index;
        }
        if (copyAmount <= 0) {
            return -1;
        }
        System.arraycopy(bytes, index, b, off, copyAmount);
        index += copyAmount;
        return copyAmount;
    }

    @Override
    public int read() throws IOException {

        final byte[] individualElement = new byte[1];
        int success = refillBuffer(individualElement, 0, 1);
        if (success > 0) { //successful get of byte element
            return individualElement[0] & 0xff;
        }
        return -1; //the end has been reached
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        return refillBuffer(b, off, len);
    }
}
