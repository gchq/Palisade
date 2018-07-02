package uk.gov.gchq.palisade.policy;

import org.junit.Test;

import uk.gov.gchq.koryphe.tuple.MapTuple;
import uk.gov.gchq.koryphe.tuple.Tuple;
import uk.gov.gchq.palisade.Justification;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.policy.adapted.tuple.TupleAdaptedPredicateRule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TupleAdaptedPredicateRuleTest {
    private static final String FIELD_A = "fieldA";

    @Test
    public void shouldTestTupleFieldATrue() throws Exception {
        // Given
        final TupleAdaptedPredicateRule<String, ?> tupleRule = new TupleAdaptedPredicateRule<>(new TestRule(), new String[]{FIELD_A});
        final Tuple<String> tuple = new MapTuple<>();
        tuple.put(FIELD_A, 3);

        // When
        final boolean result = tupleRule.test(tuple, null, null);

        // Then
        assertTrue(result);
    }

    @Test
    public void shouldTestTupleFieldAFalse() throws Exception {
        // Given
        final TupleAdaptedPredicateRule<String, ?> tupleRule = new TupleAdaptedPredicateRule<>(new TestRule(), new String[]{FIELD_A});
        final Tuple<String> tuple = new MapTuple<>();
        tuple.put(FIELD_A, 8);

        // When
        final boolean result = tupleRule.test(tuple, null, null);

        // Then
        assertFalse(result);
    }

    private static class TestRule implements PredicateRule<Integer> {
        @Override
        public boolean test(final Integer record, final User user, final Justification justification) {
            return record < 5;
        }
    }
}
