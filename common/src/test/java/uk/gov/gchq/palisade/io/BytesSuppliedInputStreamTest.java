package uk.gov.gchq.palisade.io;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Supplier;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class BytesSuppliedInputStreamTest {
    @Test
    public void shouldValidateSupplier() throws Exception {
        // Given
        final Supplier<Bytes> supplier = null;

        // When / Then
        try {
            new BytesSuppliedInputStream(supplier);
            fail("Exception expected");
        } catch (final NullPointerException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void shouldLoadBytesFromSupplier() throws Exception {
        // Given
        final Iterator<byte[]> bytes = Arrays.asList(new byte[]{1, 2, 3, 4}, new byte[]{5, 6, 7, 8}).iterator();

        final Supplier<Bytes> supplier = () -> {
            if (bytes.hasNext()) {
                final byte[] cachedBytes = bytes.next();
                return () -> cachedBytes;
            }
            return null;
        };

        // When
        final BytesSuppliedInputStream stream = new BytesSuppliedInputStream(supplier);

        // Then
        final byte[] allBytes = IOUtils.toByteArray(stream);
        assertArrayEquals(new byte[]{1, 2, 3, 4, 5, 6, 7, 8}, allBytes);
    }
}
