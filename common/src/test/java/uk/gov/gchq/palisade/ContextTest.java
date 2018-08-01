package uk.gov.gchq.palisade;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;

public class ContextTest {

    private Context testObject;

    @Before
    public void setUp() throws Exception {
        testObject = new Context().addJustification("justification1");
    }

    @Test
    public void shouldJsonSerialise() throws Exception {
        //when
        final byte[] serialise = JSONSerialiser.serialise(testObject, true);
        final Context deserialise = JSONSerialiser.deserialise(serialise, Context.class);
        //then
        Assert.assertEquals(testObject, deserialise);
    }

    @Test
    public void shouldHistoricalJsonSerialise() throws Exception {
        //when
        final byte[] serialise = JSONSerialiser.serialise(testObject, true);
        Context deserialise = JSONSerialiser.deserialise(
                "{\n" +
                "  \"context\" : {\n" +
                "    \"justification\" : \"justification1\"\n" +
                "  }\n" +
                "} ", Context.class);
        //then
        Assert.assertEquals(new String(serialise), testObject, deserialise);
    }
}