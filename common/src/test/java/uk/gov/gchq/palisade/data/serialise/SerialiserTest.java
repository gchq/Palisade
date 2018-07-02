package uk.gov.gchq.palisade.data.serialise;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public abstract class SerialiserTest<I, O> {

    public abstract Serialiser<I, O> getSerialiser();

    public abstract O getObject();

    @Test
    public void shouldSerialise() throws Exception {

        final Serialiser<I, O> serialiser = getSerialiser();
        final O object = getObject();
        final I serialisedObject;
        try {
            serialisedObject = serialiser.serialise(object);
        } catch (Exception e) {
            throw new RuntimeException("Failed serialising the object");
        }
        final O deserialisedObject;
        try {
            deserialisedObject = serialiser.deserialise(serialisedObject);
        } catch (Exception e) {
            throw new RuntimeException("Failed deserialising the bytes", e);
        }
        assertEquals("serialising and deserialising is not symmetrical", object, deserialisedObject);
    }

    public abstract I getHistorical();

    @Test
    public void shouldMatchSerialisedObjectAndHistoricalValue() {
        assertEquals("The historical value does not equal the serialised form of the given object", getHistorical(), getSerialiser().serialise(getObject()));

    }
}