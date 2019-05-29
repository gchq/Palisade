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

import java.io.ByteArrayOutputStream;

/**
 * A {@code BytesOutputStream} extends {@link ByteArrayOutputStream} and
 * exposes the buffered bytes and count fields.
 */
public class BytesOutputStream extends ByteArrayOutputStream implements Bytes {
    @Override
    public byte[] getBytes() {
        return buf;
    }

    @Override
    public int getCount() {
        return count;
    }

    /**
     * Resets the byte buffer to a new byte array.
     */
    public void reset() {
        buf = new byte[512]; //improve performance by having a larger serialised buffer
        count = 0;
    }
}
