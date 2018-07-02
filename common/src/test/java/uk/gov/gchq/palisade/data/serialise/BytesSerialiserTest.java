package uk.gov.gchq.palisade.data.serialise;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;

public abstract class BytesSerialiserTest<T> extends SerialiserTest<byte[], T> {

    @Override
    public abstract BytesSerialiser<T> getSerialiser();

    @Override
    public void shouldMatchSerialisedObjectAndHistoricalValue() {
        final String message = "The historical value does not equal the serialised form of the given object. ";
        final byte[] serialise = getSerialiser().serialise(getObject());
        assertArrayEquals( Arrays.toString(serialise) + message,
                getHistorical(), serialise);
    }
}