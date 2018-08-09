package uk.gov.gchq.palisade.data.serialise;

import org.junit.Test;

import java.io.InputStream;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public abstract class SerialiserTest<I> {

    public abstract Serialiser<I> getSerialiser();

    public abstract I getObject();

    @Test
    public void shouldSerialise() throws Exception {

        final Serialiser<I> serialiser = getSerialiser();
        final I object = getObject();
        final InputStream serialisedObject;
        try {
            serialisedObject = serialiser.serialise(Stream.of(object));
        } catch (Exception e) {
            throw new RuntimeException("Failed serialising the object");
        }
        final I deserialisedObject;
        try {
            deserialisedObject = serialiser.deserialise(serialisedObject).findFirst().get();
        } catch (Exception e) {
            throw new RuntimeException("Failed deserialising the bytes", e);
        }
        assertEquals("serialising and deserialising is not symmetrical", object, deserialisedObject);
    }

    public abstract InputStream getHistorical();

    @Test
    public void shouldMatchSerialisedObjectAndHistoricalValue() {
        assertEquals("The historical value does not equal the serialised form of the given object", getHistorical(), getSerialiser().serialise(Stream.of(getObject())));
    }
}
