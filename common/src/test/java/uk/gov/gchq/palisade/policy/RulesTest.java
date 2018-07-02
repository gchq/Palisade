package uk.gov.gchq.palisade.policy;

import org.junit.Before;
import org.junit.Test;

import uk.gov.gchq.koryphe.impl.predicate.IsMoreThan;
import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.palisade.util.JsonAssert;

import static org.junit.Assert.assertEquals;

public class RulesTest {

    private Rules rules;
    private byte[] serialise;

    @Before
    public void setUp() throws Exception {
        rules = new Rules<>()
                .message("Age off and visibility filtering")
                .tuplePredicateRule(
                        "ageOff",
                        new IsMoreThan(10),
                        "timestamp"
                );
        serialise = JSONSerialiser.serialise(rules, true);
    }

    @Test
    public void shouldSerialiseToEqualObject() throws Exception {
        Rules deserialise = JSONSerialiser.deserialise(serialise, Rules.class);
        final String thisSerialise = new String(JSONSerialiser.serialise(this.rules, true));
        final String thatSerialise = new String(JSONSerialiser.serialise(deserialise, true));

        assertEquals(thisSerialise, thatSerialise);

        JsonAssert.assertEquals(rules, deserialise);
    }

    @Test
    public void shouldSerialiseTo() throws Exception {
        //Without the optional classes
        final String text = String.format("{%n" +
                "  \"message\" : \"Age off and visibility filtering\",%n" +
                "  \"rules\" : {%n" +
                "    \"ageOff\" : {%n" +
                "      \"predicate\" : {%n" +
                "        \"class\" : \"uk.gov.gchq.koryphe.impl.predicate.IsMoreThan\",%n" +
                "        \"orEqualTo\" : false,%n" +
                "        \"value\" : 10%n" +
                "      },%n" +
                "      \"selection\" : [ \"timestamp\" ]%n" +
                "    }%n" +
                "  }%n" +
                "}");

        assertEquals(text, new String(serialise));
    }

    @Test
    public void shouldDeserialiseText() throws Exception {
        //with or without the optional classes
        final String text = String.format("{%n" +
                "  \"message\" : \"Age off and visibility filtering\",%n" +
                "  \"rules\" : {%n" +
                "    \"ageOff\" : {%n" +
                "      \"predicate\" : {%n" +
                "        \"class\" : \"uk.gov.gchq.koryphe.impl.predicate.IsMoreThan\",%n" +
                "        \"orEqualTo\" : false,%n" +
                "        \"value\" : 10%n" +
                "      },%n" +
                "      \"selection\" : [ \"timestamp\" ]%n" +
                "    }%n" +
                "  }%n" +
                "}");

        final Rules deserialise = JSONSerialiser.deserialise(text, Rules.class);
        JsonAssert.assertEquals(rules, deserialise);

    }
}
