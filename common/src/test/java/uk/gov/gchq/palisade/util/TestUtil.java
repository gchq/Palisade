package uk.gov.gchq.palisade.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestUtil.class);

    public static File TMP_DIRECTORY;

    static {
        final String tmpDirectoryProperty = System.getProperty("java.io.tmpdir");

        if (null != tmpDirectoryProperty) {
            TMP_DIRECTORY = new File(tmpDirectoryProperty);
        } else {
            LOGGER.warn("Could not determine default temporary directory, using current directory.");
            TMP_DIRECTORY = new File(".");
        }
    }

    /**
     * Compare two streams for equality. Each stream must be of the same length and contain the same elements (by
     * value). The streams are sorted beforehand. Therefore T must be naturally comparable.
     *
     * @param expected first stream
     * @param actual   second stream
     * @param <T>      type of list element
     * @return true if streams are equal
     */
    public static <T> boolean streamEqual(final Stream<? extends T> expected, final Stream<? extends T> actual) {
        Stream<? extends T> sort_expected = expected.sorted();
        Stream<? extends T> sort_actual = actual.sorted();
        List<? extends T> lhs = sort_expected.collect(Collectors.toList());
        List<? extends T> rhs = sort_actual.collect(Collectors.toList());
        return lhs.equals(rhs);
    }
}
