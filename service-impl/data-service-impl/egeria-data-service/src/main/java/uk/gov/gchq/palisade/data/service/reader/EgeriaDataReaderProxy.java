package uk.gov.gchq.palisade.data.service.reader;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import uk.gov.gchq.palisade.data.service.impl.reader.HadoopDataReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class EgeriaDataReaderProxy extends CachedSerialisedDataReader {
    @JsonIgnore
    private CachedSerialisedDataReader proxyOf;

    public EgeriaDataReaderProxy() {
        proxyOf = new HadoopDataReader();
    }

    @JsonCreator
    public EgeriaDataReaderProxy(@JsonProperty("conf") final Map<String, String> conf) throws IOException {
        proxyOf = new HadoopDataReader(conf);
    }

    public EgeriaDataReaderProxy(CachedSerialisedDataReader dataReader) {
        proxyOf = dataReader;
    }

    @Override
    protected InputStream readRaw(final uk.gov.gchq.palisade.resource.LeafResource resource) {
        return proxyOf.readRaw(resource);
    }
}
