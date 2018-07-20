package uk.gov.gchq.palisade;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.JobContext;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import uk.gov.gchq.palisade.data.serialise.Serialiser;
import uk.gov.gchq.palisade.data.serialise.StubSerialiser;
import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.palisade.resource.StubResource;
import uk.gov.gchq.palisade.service.PalisadeService;
import uk.gov.gchq.palisade.service.request.ConnectionDetail;
import uk.gov.gchq.palisade.service.request.DataRequestResponse;
import uk.gov.gchq.palisade.service.request.RegisterDataRequest;
import uk.gov.gchq.palisade.service.request.StubConnectionDetail;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PrimitiveIterator;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

public class PalisadeInputFormatTest {

    @Test
    public void shouldSerialiseandDeserialise() throws IOException {
        //Given
        StubSerialiser<Object> serial = new StubSerialiser<>("nothing");
        Configuration c = new Configuration();
        //When
        PalisadeInputFormat.setSerialiser(c, serial);
        Serialiser<Object, Object> deserial = PalisadeInputFormat.getSerialiser(c);
        //Then
        assertEquals(serial, deserial);
    }

    @Test
    public void shouldAddDataRequest() {
        //Given
        Configuration c = new Configuration();
        JobContext mockJob = Mockito.mock(JobContext.class);
        when(mockJob.getConfiguration()).thenReturn(c);

        RegisterDataRequest rdr = new RegisterDataRequest("testResource", new UserId("user"), new Justification("justification"));
        RegisterDataRequest[] rdrArray = {rdr};
        String json = new String(JSONSerialiser.serialise(rdrArray), PalisadeInputFormat.UTF8);
        //When
        PalisadeInputFormat.addDataRequest(mockJob, rdr);
        //Then
        assertEquals(json, c.get(PalisadeInputFormat.REGISTER_REQUESTS_KEY));
    }

    @Test
    public void shouldAddMultipleWithComma() {
        //Given
        Configuration c = new Configuration();
        JobContext mockJob = Mockito.mock(JobContext.class);
        when(mockJob.getConfiguration()).thenReturn(c);
        RegisterDataRequest rdr = new RegisterDataRequest("testResource", new UserId("user"), new Justification("justification"));
        //When
        PalisadeInputFormat.addDataRequest(mockJob, rdr);
        PalisadeInputFormat.addDataRequest(mockJob, rdr);
        RegisterDataRequest[] rdrArray = {rdr, rdr};
        String json = new String(JSONSerialiser.serialise(rdrArray), PalisadeInputFormat.UTF8);
        //Then
        assertEquals(json, c.get(PalisadeInputFormat.REGISTER_REQUESTS_KEY));
    }

    @Test
    public void shouldAddEmptyRequest() {
        //Given
        Configuration c = new Configuration();
        JobContext mockJob = Mockito.mock(JobContext.class);
        when(mockJob.getConfiguration()).thenReturn(c);
        RegisterDataRequest rdr = new RegisterDataRequest();
        RegisterDataRequest[] rdrArray = {rdr};
        String json = new String(JSONSerialiser.serialise(rdrArray), PalisadeInputFormat.UTF8);
        //When
        PalisadeInputFormat.addDataRequest(mockJob, rdr);
        //Then
        assertEquals(json, c.get(PalisadeInputFormat.REGISTER_REQUESTS_KEY));
    }

    @Test
    public void canGetEmptyRequestList() {
        //Given
        Configuration c = new Configuration();
        JobContext mockJob = Mockito.mock(JobContext.class);
        when(mockJob.getConfiguration()).thenReturn(c);
        //When
        //nothing
        //Then
        List<RegisterDataRequest> reqs = PalisadeInputFormat.getDataRequests(mockJob);
        assertEquals(0, reqs.size());
    }

    @Test
    public void addAndGetRequests() {
        //Given
        Configuration c = new Configuration();
        JobContext mockJob = Mockito.mock(JobContext.class);
        when(mockJob.getConfiguration()).thenReturn(c);
        RegisterDataRequest rdr = new RegisterDataRequest("testResource", new UserId("user"), new Justification("justification"));
        RegisterDataRequest rdr2 = new RegisterDataRequest("testResource2", new UserId("user2"), new Justification("justification2"));
        RegisterDataRequest rdr3 = new RegisterDataRequest();
        //When
        PalisadeInputFormat.addDataRequests(mockJob, rdr, rdr2, rdr3);
        List<RegisterDataRequest> expected = Stream.of(rdr, rdr2, rdr3).collect(Collectors.toList());
        //Then
        assertEquals(expected, PalisadeInputFormat.getDataRequests(mockJob));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowOnNoRequests() throws IOException {
        //Given
        Configuration c = new Configuration();
        JobContext mockJob = Mockito.mock(JobContext.class);
        when(mockJob.getConfiguration()).thenReturn(c);
        PalisadeService server = Mockito.mock(PalisadeService.class);
        PalisadeInputFormat.setPalisadeService(server);
        //When
        //nothing
        //Then
        new PalisadeInputFormat().getSplits(mockJob);
        fail("exception expected");
    }

    @Test
    public void testListToMapCollector() {
        //Given - a Map of numbers to their double
        Map<Integer, Integer> original = IntStream
                .range(1, 20)
                .boxed()
                .collect(Collectors.toMap(Function.identity(), x -> x * 2));
        //When - convert to a list of entry
        List<Map.Entry<Integer, Integer>> entries = original
                .entrySet()
                .stream()
                .collect(Collectors.toList());
        //Then - should equal original map
        assertEquals(original, entries
                        .stream()
                        .collect(PalisadeInputFormat.listToMapCollector())
        );
    }

    @Test
    public void testListToMapCollectorEmpty() {
        //Given - a Map of numbers to their double
        Map<Integer, Integer> original = Collections.emptyMap();
        //When - convert to a list of entry
        List<Map.Entry<Integer, Integer>> entries = original
                .entrySet()
                .stream()
                .collect(Collectors.toList());
        //Then - should equal original map
        assertEquals(original, entries
                        .stream()
                        .collect(PalisadeInputFormat.listToMapCollector())
        );
    }

    @Test
    public void shouldReturnEmptySplits() {
        //Given
        DataRequestResponse req = new DataRequestResponse();
        PrimitiveIterator.OfInt index = IntStream.range(1, 9999).iterator();
        //When
        List<PalisadeInputSplit> result = PalisadeInputFormat.toInputSplits(req, index);
        //Then
        assertEquals(Collections.emptyList(), result);
    }

    private static DataRequestResponse request;

    @BeforeClass
    public static void setup() {
        request = new DataRequestResponse();
        request.getResources().put(new StubResource("type1", "id1", "format1"), new StubConnectionDetail("con1"));
        request.getResources().put(new StubResource("type2", "id2", "format2"), new StubConnectionDetail("con2"));
        request.getResources().put(new StubResource("type3", "id3", "format3"), new StubConnectionDetail("con3"));
        request.getResources().put(new StubResource("type4", "id4", "format4"), new StubConnectionDetail("con4"));
        request.getResources().put(new StubResource("type5", "id5", "format5"), new StubConnectionDetail("con5"));
    }

    @Test
    public void shouldReturnSingleSplit() {
        //Given - ask for a single split
        PrimitiveIterator.OfInt index = IntStream.generate(() -> 1).iterator();
        //When
        List<PalisadeInputSplit> result = PalisadeInputFormat.toInputSplits(request, index);
        //Then
        assertEquals(1, result.size());
    }

    @Test
    public void shouldReturnMultipleSplit() {
        //Given - ask for 3 splits
        PrimitiveIterator.OfInt index = IntStream.of(0, 1, 2, 0, 1, 2, 0, 1, 2).iterator();
        //When
        List<PalisadeInputSplit> result = PalisadeInputFormat.toInputSplits(request, index);
        //Then
        assertEquals(3, result.size());
        //should be two in the first two splits, one in the last
        assertEquals(2, result.get(0).getRequestResponse().getResources().size());
        assertEquals(2, result.get(1).getRequestResponse().getResources().size());
        assertEquals(1, result.get(2).getRequestResponse().getResources().size());
        //now check we still got 5 distinct values
        //create set of the values from the map
        Set<String> values0 = result.get(0).getRequestResponse().getResources()
                .values()
                .stream()
                .map(x -> ((StubConnectionDetail) x).getCon())
                .collect(Collectors.toSet());
        Set<String> values1 = result.get(1).getRequestResponse().getResources()
                .values()
                .stream()
                .map(x -> ((StubConnectionDetail) x).getCon())
                .collect(Collectors.toSet());
        Set<String> values2 = result.get(2).getRequestResponse().getResources()
                .values()
                .stream()
                .map(x -> ((StubConnectionDetail) x).getCon())
                .collect(Collectors.toSet());
        Set<String> merged = new HashSet<>();
        merged.addAll(values0);
        merged.addAll(values1);
        merged.addAll(values2);
        assertEquals(5, merged.size());
    }

/* Tests to write
test create split from a known list to one mapper, several
 */
}