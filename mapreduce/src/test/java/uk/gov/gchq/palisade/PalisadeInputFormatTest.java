package uk.gov.gchq.palisade;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.JobContext;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import uk.gov.gchq.palisade.data.serialise.Serialiser;
import uk.gov.gchq.palisade.data.serialise.StubSerialiser;
import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.palisade.service.request.RegisterDataRequest;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
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


/* Tests to write
should test splits empty
test collector produces correct results empty, single and many

test split creator with an empty mappings, several to single and 2. use junit paramter run
test create split from a known list to one mapper, several

 */
}