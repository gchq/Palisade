package uk.gov.gchq.palisade.data.serialise;

public abstract class NullSerialiserTest<T> extends SerialiserTest<T, T> {

    @Override
    public abstract NullSerialiser<T> getSerialiser();
}