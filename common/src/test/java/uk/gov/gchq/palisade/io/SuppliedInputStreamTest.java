package uk.gov.gchq.palisade.io;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Supplier;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class SuppliedInputStreamTest {
    @Test
    public void shouldValidateSupplier() throws Exception {
        // Given
        final Supplier<byte[]> supplier = null;

        // When / Then
        try {
            new SuppliedInputStream(supplier);
            fail("Exception expected");
        } catch (final NullPointerException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void shouldLoadBytesFromSupplier() throws Exception {
        // Given
        final Iterator<byte[]> bytes = Arrays.asList(new byte[]{1, 2, 3, 4}, new byte[]{5, 6, 7, 8}).iterator();

        final Supplier<byte[]> supplier = () -> {
            if (bytes.hasNext()) {
                return bytes.next();
            }
            return null;
        };

        // When
        final SuppliedInputStream stream = new SuppliedInputStream(supplier);

        // Then
        final byte[] allBytes = IOUtils.toByteArray(stream);
        assertArrayEquals(new byte[]{1, 2, 3, 4, 5, 6, 7, 8}, allBytes);
    }
}
