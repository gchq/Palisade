package uk.gov.gchq.palisade.mapreduce;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.data.serialise.Serialiser;
import uk.gov.gchq.palisade.data.serialise.SimpleStringSerialiser;
import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.resource.StubResource;
import uk.gov.gchq.palisade.service.PalisadeService;
import uk.gov.gchq.palisade.service.request.DataRequestResponse;
import uk.gov.gchq.palisade.service.request.RegisterDataRequest;
import uk.gov.gchq.palisade.service.request.StubConnectionDetail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class PalisadeInputFormatTest {

    @Test
    public void shouldSerialiseandDeserialise() throws IOException {
        //Given
        SimpleStringSerialiser serial = new SimpleStringSerialiser();
        Configuration c = new Configuration();
        //When
        PalisadeInputFormat.setSerialiser(c, serial);
        Serialiser<String> deserial = PalisadeInputFormat.getSerialiser(c);
        //Then
        assertEquals(serial.getClass(), deserial.getClass());
    }

    @Test
    public void shouldAddDataRequest() {
        //Given
        Configuration c = new Configuration();
        JobContext mockJob = Mockito.mock(JobContext.class);
        when(mockJob.getConfiguration()).thenReturn(c);

        RegisterDataRequest rdr = new RegisterDataRequest().resourceId("testResource").userId(new UserId().id("user")).context(new Context().purpose("purpose"));
        RegisterDataRequest[] rdrArray = {rdr};
        String json = new String(JSONSerialiser.serialise(rdrArray), StandardCharsets.UTF_8);
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
        RegisterDataRequest rdr = new RegisterDataRequest().resourceId("testResource").userId(new UserId().id("user")).context(new Context().purpose("purpose"));
        //When
        PalisadeInputFormat.addDataRequest(mockJob, rdr);
        PalisadeInputFormat.addDataRequest(mockJob, rdr);
        RegisterDataRequest[] rdrArray = {rdr, rdr};
        String json = new String(JSONSerialiser.serialise(rdrArray), StandardCharsets.UTF_8);
        //Then
        assertEquals(json, c.get(PalisadeInputFormat.REGISTER_REQUESTS_KEY));
    }

    @Test(expected = RuntimeException.class)
    public void shouldErrorWhenAddingEmptyRequest() {
        //Given
        Configuration c = new Configuration();
        JobContext mockJob = Mockito.mock(JobContext.class);
        when(mockJob.getConfiguration()).thenReturn(c);
        RegisterDataRequest rdr = new RegisterDataRequest();
        //When
        PalisadeInputFormat.addDataRequest(mockJob, rdr);
        //Then
        fail();
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
        RegisterDataRequest rdr = new RegisterDataRequest().resourceId("testResource").userId(new UserId().id("user")).context(new Context().purpose("purpose"));
        RegisterDataRequest rdr2 = new RegisterDataRequest().resourceId("testResource2").userId(new UserId().id("user2")).context(new Context().purpose("purpose2"));
        //When
        PalisadeInputFormat.addDataRequests(mockJob, rdr, rdr2);
        List<RegisterDataRequest> expected = Stream.of(rdr, rdr2).collect(Collectors.toList());
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
        PalisadeInputFormat.setPalisadeService(mockJob, server);
        //When
        //nothing
        //Then
        new PalisadeInputFormat().getSplits(mockJob);
        fail("exception expected");
    }

    private static RegisterDataRequest request1;
    private static DataRequestResponse req1Response;
    private static RegisterDataRequest request2;
    private static DataRequestResponse req2Response;

    @BeforeClass
    public static void setup() {
        request1 = new RegisterDataRequest().resourceId("res1").userId(new UserId().id("user1")).context(new Context().purpose("purpose1"));
        req1Response = new DataRequestResponse().requestId(new RequestId().id("request1"))
                .resource(new StubResource("type1", "id1", "format1"), new StubConnectionDetail("con1"))
                .resource(new StubResource("type2", "id2", "format2"), new StubConnectionDetail("con2"))
                .resource(new StubResource("type3", "id3", "format3"), new StubConnectionDetail("con3"))
                .resource(new StubResource("type4", "id4", "format4"), new StubConnectionDetail("con4"))
                .resource(new StubResource("type5", "id5", "format5"), new StubConnectionDetail("con5"))
                .originalRequestId("request1.setup");

        request2 = new RegisterDataRequest().resourceId("res2").userId(new UserId().id("user2")).context(new Context().purpose("purpose2"));
        req2Response = new DataRequestResponse().requestId(new RequestId().id("request2"))
                .resource(new StubResource("type_a", "id6", "format6"), new StubConnectionDetail("con6"))
                .resource(new StubResource("type_b", "id7", "format7"), new StubConnectionDetail("con7"))
                .originalRequestId("request2.setup");
    }

    /**
     * Simulate a job set up, mock up a job and ask the input format to create splits for it. The given {@link
     * PalisadeService} will be used to provide data for the requests.
     *
     * @param maxMapHint      maximum mappers to set
     * @param reqs            the map of requests and responses for a palisade service
     * @param palisadeService the service to send requests to
     * @return input splits
     * @throws IOException shouldn't happen
     */
    private static List<InputSplit> callGetSplits(int maxMapHint, Map<RegisterDataRequest, DataRequestResponse> reqs, PalisadeService palisadeService) throws IOException {
        Configuration c = new Configuration();
        JobContext mockJob = Mockito.mock(JobContext.class);
        when(mockJob.getConfiguration()).thenReturn(c);
        //configure the input format as the client would
        PalisadeInputFormat.setMaxMapTasksHint(mockJob, maxMapHint);
        PalisadeInputFormat.setPalisadeService(mockJob, palisadeService);
        for (RegisterDataRequest req : reqs.keySet()) {
            PalisadeInputFormat.addDataRequest(mockJob, req);
        }
        //simulate a job run
        PalisadeInputFormat<String> pif = new PalisadeInputFormat<>();
        return pif.getSplits(mockJob);
    }

    /**
     * Simulate a job set up, mock up a job and a {@link PalisadeService} that responds in a realistic way with the
     * given map of responses.
     *
     * @param maxMapHint maximum mappers to set
     * @param reqs       the map of requests and responses for a palisade service
     * @return input splits
     * @throws IOException shouldn't happen
     */
    private static List<InputSplit> callGetSplits(int maxMapHint, Map<RegisterDataRequest, DataRequestResponse> reqs) throws IOException {
        //make a mock palisade service that the input format can talk to
        PalisadeService palisadeService = Mockito.mock(PalisadeService.class);
        //tell it what to respond with
        for (Map.Entry<RegisterDataRequest, DataRequestResponse> req : reqs.entrySet()) {
            when(palisadeService.registerDataRequest(req.getKey())).thenReturn(CompletableFuture.supplyAsync(() -> {
                //wait random time for the palisade service to pretend to process the resource
                try {
                    Thread.sleep(ThreadLocalRandom.current().nextInt(0, 500));
                } catch (InterruptedException ignored) {
                }
                return req.getValue();
            }));
        }
        return callGetSplits(maxMapHint, reqs, palisadeService);
    }

    @SuppressWarnings("unchecked")
    private static <R extends InputSplit> List<R> convert(List<InputSplit> list) {
        return (List<R>) list;
    }

    @Test
    public void shouldThrowOnSingleFailedRequest() throws IOException {
        //Given
        Map<RegisterDataRequest, DataRequestResponse> resources = new HashMap<>();
        resources.put(request1, req1Response);
        //make palisade service that throws exceptions
        PalisadeService mockService = Mockito.mock(PalisadeService.class);
        when(mockService.registerDataRequest(any(RegisterDataRequest.class))).thenReturn(CompletableFuture.supplyAsync(() -> {
            throw new IllegalStateException("test exception");
        }));
        //When
        List<PalisadeInputSplit> splits = convert(callGetSplits(1, resources, mockService));
        //Then
        checkForExpectedResources(splits, 0, 0);
    }

    @Test
    public void shouldCreateOneSplitFromOneRequest() throws IOException {
        //Given
        Map<RegisterDataRequest, DataRequestResponse> resources = new HashMap<>();
        resources.put(request1, req1Response);
        //When
        List<PalisadeInputSplit> splits = convert(callGetSplits(1, resources));
        //Then
        checkForExpectedResources(splits, 1, 5);
    }

    @Test
    public void shouldCreateTwoSplitFromOneRequest() throws IOException {
        //Given
        Map<RegisterDataRequest, DataRequestResponse> resources = new HashMap<>();
        resources.put(request1, req1Response);
        //When
        List<PalisadeInputSplit> splits = convert(callGetSplits(2, resources));
        //Then
        checkForExpectedResources(splits, 2, 5);
    }

    @Test
    public void shouldCreateManySplitFromOneRequest() throws IOException {
        //Given
        Map<RegisterDataRequest, DataRequestResponse> resources = new HashMap<>();
        resources.put(request1, req1Response);
        //When
        List<PalisadeInputSplit> splits = convert(callGetSplits(99999, resources));
        //Then
        checkForExpectedResources(splits, 5, 5);
    }

    @Test
    public void shouldCreateTwoSplitsFromTwoRequests() throws IOException {
        //Given
        Map<RegisterDataRequest, DataRequestResponse> resources = new HashMap<>();
        resources.put(request1, req1Response);
        resources.put(request2, req2Response);
        //When
        List<PalisadeInputSplit> splits = convert(callGetSplits(1, resources));
        //Then
        checkForExpectedResources(splits, 2, 7);
    }

    @Test
    public void shouldCreateFourSplitsFromTwoRequests() throws IOException {
        //Given
        Map<RegisterDataRequest, DataRequestResponse> resources = new HashMap<>();
        resources.put(request1, req1Response);
        resources.put(request2, req2Response);
        //When
        List<PalisadeInputSplit> splits = convert(callGetSplits(2, resources));
        //Then
        checkForExpectedResources(splits, 4, 7);
    }

    @Test
    public void shouldCreateManySplitsFromTwoRequests() throws IOException {
        //Given
        Map<RegisterDataRequest, DataRequestResponse> resources = new HashMap<>();
        resources.put(request1, req1Response);
        resources.put(request2, req2Response);
        //When
        List<PalisadeInputSplit> splits = convert(callGetSplits(7, resources));
        //Then
        checkForExpectedResources(splits, 7, 7);
    }

    @Test
    public void shouldCreateManySplitsFromTwoRequestsNoMapHint() throws IOException {
        //Given
        Map<RegisterDataRequest, DataRequestResponse> resources = new HashMap<>();
        resources.put(request1, req1Response);
        resources.put(request2, req2Response);
        //When
        List<PalisadeInputSplit> splits = convert(callGetSplits(0, resources));
        //Then
        checkForExpectedResources(splits, 7, 7);
    }

    private void checkForExpectedResources(List<PalisadeInputSplit> splits, int expectedSplits, int expectedNumberResources) {
        assertEquals(expectedSplits, splits.size());
        //combine all the resources from both splits and check we have all 5 resources covered
        //first check expectedTotal total
        assertEquals(expectedNumberResources, splits
                .stream()
                .flatMap(split -> split.getRequestResponse().getResources().entrySet().stream())
                .count());
        //check for no duplicates
        Set<Resource> allResponses = splits
                .stream()
                .flatMap(split -> split.getRequestResponse().getResources().keySet().stream())
                .collect(Collectors.toSet());
        assertEquals(expectedNumberResources, allResponses.size());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowOnNegativeMapHint() throws IOException {
        //Given
        Configuration c = new Configuration();
        JobContext mockJob = Mockito.mock(JobContext.class);
        when(mockJob.getConfiguration()).thenReturn(c);
        //make a mock palisade service that the input format can talk to
        PalisadeService palisadeService = Mockito.mock(PalisadeService.class);
        //When
        PalisadeInputFormat.addDataRequest(mockJob, request1);
        c.setInt(PalisadeInputFormat.MAXIMUM_MAP_HINT_KEY, -1);
        //directly put illegal value into config
        //Then
        new PalisadeInputFormat<String>().getSplits(mockJob);
        fail("Should throw exception");
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowOnNoPalisadeService() throws IOException {
        //Given
        JobContext mockJob = Mockito.mock(JobContext.class);
        //When - nothing
        //Then
        new PalisadeInputFormat<String>().getSplits(mockJob);
        fail("Should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenMaxMapHintNegative() {
        //Given
        Configuration c = new Configuration();
        JobContext mockJob = Mockito.mock(JobContext.class);
        when(mockJob.getConfiguration()).thenReturn(c);
        //When
        PalisadeInputFormat.setMaxMapTasksHint(mockJob, -1);
        //Then
        fail("Should throw exception");
    }

    @Test
    public void shouldReturnNullForNoPalisadeService() {
        //Given
        Configuration c = new Configuration();
        JobContext mockJob = Mockito.mock(JobContext.class);
        when(mockJob.getConfiguration()).thenReturn(c);
        //When
        PalisadeService service = PalisadeInputFormat.getPalisadeService(mockJob);
        //Then
        assertNull(service);
    }

    @Test
    public void shouldReturnPalisadeService() {
        //Given
        Configuration c = new Configuration();
        JobContext mockJob = Mockito.mock(JobContext.class);
        when(mockJob.getConfiguration()).thenReturn(c);
        PalisadeService expected = Mockito.mock(PalisadeService.class);
        //When
        PalisadeInputFormat.setPalisadeService(mockJob, expected);
        PalisadeService actual = PalisadeInputFormat.getPalisadeService(mockJob);
        //Then
        assertSame(expected, actual);
    }
}