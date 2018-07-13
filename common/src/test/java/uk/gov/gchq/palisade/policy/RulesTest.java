package uk.gov.gchq.palisade.policy;

import org.junit.Before;
import org.junit.Test;

import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.palisade.util.JsonAssert;

import static org.junit.Assert.assertEquals;

public class RulesTest {

    private Rules<String> rules;
    private byte[] json;

    @Before
    public void setUp() throws Exception {
        rules = new Rules<String>()
                .message("Age off and visibility filtering")
                .rule("ageOffRule", new TestRule()
                );
        json = JSONSerialiser.serialise(rules, true);
    }

    @Test
    public void shouldSerialiseToEqualObject() throws Exception {
        Rules deserialise = JSONSerialiser.deserialise(json, Rules.class);
        final String thisSerialise = new String(JSONSerialiser.serialise(this.rules, true));
        final String thatSerialise = new String(JSONSerialiser.serialise(deserialise, true));

        assertEquals(thisSerialise, thatSerialise);

        JsonAssert.assertEquals(rules, deserialise);
    }

    @Test
    public void shouldSerialiseTo() throws Exception {
        final String text = String.format("{%n" +
                "  \"message\" : \"Age off and visibility filtering\",%n" +
                "  \"rules\" : {%n" +
                "    \"ageOffRule\" : {%n" +
                "      \"class\" : \"uk.gov.gchq.palisade.policy.TestRule\"%n" +
                "    }%n" +
                "  }%n" +
                "}");

        assertEquals(text, new String(json));
    }

    @Test
    public void shouldDeserialiseText() throws Exception {
        final String text = String.format("{%n" +
                "  \"message\" : \"Age off and visibility filtering\",%n" +
                "  \"rules\" : {%n" +
                "    \"ageOffRule\" : {%n" +
                "      \"class\" : \"uk.gov.gchq.palisade.policy.TestRule\"%n" +
                "    }%n" +
                "  }%n" +
                "}");

        final Rules deserialise = JSONSerialiser.deserialise(text, Rules.class);
        JsonAssert.assertEquals(rules, deserialise);

    }
}
