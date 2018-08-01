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

public class SuppliedInputStream extends InputStream {
    private static final Logger LOGGER = LoggerFactory.getLogger(SuppliedInputStream.class);

    private final Supplier<byte[]> supplier;
    private byte[] buf = null;
    private int i = 0;
    private boolean end;

    public SuppliedInputStream(final Supplier<byte[]> supplier) {
        this.supplier = supplier;
    }

    @Override
    public int read() throws IOException {
        if (end) {
            return -1;
        }

        if (null == buf || i >= buf.length) {
            LOGGER.debug("Requesting more bytes");
            buf = supplier.get();
            if (null != buf && LOGGER.isWarnEnabled()) {
                LOGGER.debug("Loaded bytes {}", new String(buf));
            }
            i = 0;
        }
        if (null == buf || buf.length == 0) {
            LOGGER.debug("Reached the end of the buffer");
            end = true;
            return -1;
        }

        byte b = buf[i];
        i++;
        return b & 0xff;
    }
}
