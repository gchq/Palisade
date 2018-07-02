package uk.gov.gchq.palisade.policy;

import org.junit.Test;

import uk.gov.gchq.koryphe.impl.predicate.IsLessThan;
import uk.gov.gchq.koryphe.tuple.MapTuple;
import uk.gov.gchq.koryphe.tuple.Tuple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TuplePredicateRuleTest {
    private static final String FIELD_A = "fieldA";

    @Test
    public void shouldTestTupleFieldATrue() throws Exception {
        // Given
        final TuplePredicateRule<Tuple<String>> tupleRule = new TuplePredicateRule<>(new IsLessThan(5), new String[]{FIELD_A});
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
        final TuplePredicateRule<Tuple<String>> tupleRule = new TuplePredicateRule<>(new IsLessThan(5), new String[]{FIELD_A});
        final Tuple<String> tuple = new MapTuple<>();
        tuple.put(FIELD_A, 8);

        // When
        final boolean result = tupleRule.test(tuple, null, null);

        // Then
        assertFalse(result);
    }

    @Test
    public void shouldTestObjectFieldATrue() throws Exception {
        // Given
        final TuplePredicateRule<ExampleObj> objRule = new TuplePredicateRule<>(new IsLessThan(5), new String[]{FIELD_A});

        // When
        final boolean result = objRule.test(new ExampleObj(3), null, null);

        // Then
        assertTrue(result);
    }

    @Test
    public void shouldTestObjectFieldAFalse() throws Exception {
        // Given
        final TuplePredicateRule<ExampleObj> objRule = new TuplePredicateRule<>(new IsLessThan(5), new String[]{FIELD_A});

        // When
        final boolean result = objRule.test(new ExampleObj(8), null, null);

        // Then
        assertFalse(result);
    }

    @Test
    public void shouldReturnObj() throws Exception {
        // Given
        final TuplePredicateRule<ExampleObj> objRule = new TuplePredicateRule<>(new IsLessThan(5), new String[]{FIELD_A});
        final ExampleObj obj = new ExampleObj(3);

        // When
        final ExampleObj result = objRule.apply(obj, null, null);

        // Then
        assertEquals(obj, result);
    }

    @Test
    public void shouldReturnNothing() throws Exception {
        // Given
        final TuplePredicateRule<ExampleObj> objRule = new TuplePredicateRule<>(new IsLessThan(5), new String[]{FIELD_A});

        // When
        final ExampleObj result = objRule.apply(new ExampleObj(8), null, null);

        // Then
        assertNull(result);
    }

    private static class ExampleObj {
        public Integer fieldA;

        public ExampleObj(final Integer fieldA) {
            this.fieldA = fieldA;
        }
    }
}
