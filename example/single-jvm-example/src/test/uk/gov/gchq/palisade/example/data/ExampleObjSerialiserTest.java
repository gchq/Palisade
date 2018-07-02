package uk.gov.gchq.palisade.example.data;

import uk.gov.gchq.palisade.data.serialise.BytesSerialiser;
import uk.gov.gchq.palisade.data.serialise.BytesSerialiserTest;
import uk.gov.gchq.palisade.example.ExampleObj;
import uk.gov.gchq.palisade.example.data.serialiser.ExampleObjSerialiser;

public class ExampleObjSerialiserTest extends BytesSerialiserTest<ExampleObj> {

    @Override
    public BytesSerialiser<ExampleObj> getSerialiser() {
        return new ExampleObjSerialiser();
    }

    @Override
    public ExampleObj getObject() {
        return new ExampleObj("testString", "testString2", 123l);
    }

    @Override
    public byte[] getHistorical() {
        return new byte[]{116, 101, 115, 116, 83, 116, 114, 105, 110, 103, 44, 116, 101, 115, 116, 83, 116, 114, 105, 110, 103, 50, 44, 49, 50, 51};
    }
}