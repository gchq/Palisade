/*
 * Copyright 2018 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.gov.gchq.palisade;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.task.TaskAttemptContextImpl;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import uk.gov.gchq.palisade.data.serialise.Serialiser;
import uk.gov.gchq.palisade.data.serialise.StubSerialiser;
import uk.gov.gchq.palisade.data.service.DataService;
import uk.gov.gchq.palisade.data.service.request.ReadRequest;
import uk.gov.gchq.palisade.data.service.request.ReadResponse;
import uk.gov.gchq.palisade.resource.StubResource;
import uk.gov.gchq.palisade.service.request.ConnectionDetail;
import uk.gov.gchq.palisade.service.request.DataRequestResponse;
import uk.gov.gchq.palisade.service.request.StubConnectionDetail;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class PalisadeRecordReaderTest {

    @BeforeClass
    public static void setup() {
        conf = new Configuration();
        //make sure this is available for the tests
        serialiser = new StubSerialiser("test_serialiser");
        PalisadeInputFormat.setSerialiser(conf, serialiser);
        con = new TaskAttemptContextImpl(conf, new TaskAttemptID());
    }

    private static Configuration conf;
    private static TaskAttemptContext con;
    private static Serialiser<String, String> serialiser;

    @Test
    public void shouldReportZeroProgressWhenClose() throws Exception {
        //Given
        PalisadeRecordReader<String> prr = new PalisadeRecordReader<>();
        //When - nothing
        //Then
        assertEquals(0, prr.getProgress(), 0);
    }

    @Test(expected = ClassCastException.class)
    public void shouldThrowOnNonPalisadeInputSplit() throws IOException {
        //Given
        PalisadeRecordReader<String> prr = new PalisadeRecordReader<>();
        InputSplit is = mock(InputSplit.class);
        //When
        prr.initialize(is, con);
        //Then
        fail("expected exception");
    }

    @Test(expected = IOException.class)
    public void throwOnNoResourceInSplit() throws IOException {
        //Given
        PalisadeRecordReader<String> prr = new PalisadeRecordReader<>();
        PalisadeInputSplit is = new PalisadeInputSplit(new RequestId().id("test"), new HashMap<>());
        //When
        prr.initialize(is, con);
        //Then
        fail("expected exception");
    }

    @Test
    public void shouldReturnFalseAfterClosed() throws IOException {
        //Given
        PalisadeRecordReader<String> prr = new PalisadeRecordReader<>();
        DataRequestResponse response = new DataRequestResponse();
        Collection<String> resData = Arrays.asList("s1", "s2", "s3", "s4");
        response.getResources().put(new StubResource("type_a", "id1", "format1"), new StubConnectionDetail("con1")
                .setServiceToCreate(createMockDS(resData)));
        response.getResources().put(new StubResource("type_b", "id2", "format2"), new StubConnectionDetail("con2")
                .setServiceToCreate(createMockDS(resData)));

        //When
        PalisadeInputSplit split = new PalisadeInputSplit(response);
        prr.initialize(split, con);
        //start the stream reading
        prr.nextKeyValue();
        prr.nextKeyValue();
        prr.close();
        //Then
        assertFalse(prr.nextKeyValue());
    }

    /**
     * Validate that a record reader is returning the expected results in order and that {@link
     * PalisadeRecordReader#nextKeyValue()} is responding correctly.
     *
     * @param expected   the list of items expected
     * @param testReader the reader under test
     * @param <T>        value type of reader
     * @throws IOException that shouldn't happen
     */
    private static <T> void readValuesAndValidate(Stream<T> expected, PalisadeRecordReader<T> testReader) throws IOException {
        expected.forEach(item -> {
            try {
                assertTrue(testReader.nextKeyValue());
                assertEquals(item, testReader.getCurrentValue());
            } catch (IOException e) {
                fail("unexpected IOException on test");
                e.printStackTrace();
            }
        });
        assertFalse(testReader.nextKeyValue());
    }

    /**
     * Validates that the read method for each mock data service was called once. This method assumes that each resource
     * in the DataResponseRequest has its own unique mock DataService.
     *
     * @param response the collection of all responses that will have been read
     */
    private static void verifyMocksCalled(DataRequestResponse response) {
        for (ConnectionDetail entry : response.getResources().values()) {
            verify(((DataService) entry.createService()), times(1)).read(any());
        }
    }

    /**
     * Creates a DataService that will respond to any request with the given data.
     *
     * @param dataToReturn collection of data that will be streamed back from the data reader
     * @param <T>          the value type of the items
     * @return a mock data service instance
     */
    private static <T> DataService createMockDS(Collection<T> dataToReturn) {
        //create the simulated response
        ReadResponse<T> readResponse = new ReadResponse<>();
        readResponse.setData(dataToReturn.stream());
        //mock a data service to return it
        DataService mock = mock(DataService.class);
        Mockito.<CompletableFuture<ReadResponse<T>>>when(mock.read(any(ReadRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(readResponse));
        return mock;
    }

    @Test
    public void shouldReadbackResourcesFromOneResource() throws IOException {
        //Given
        PalisadeRecordReader<String> prr = new PalisadeRecordReader<>();
        DataRequestResponse response = new DataRequestResponse();
        //set up some data
        List<String> returnResources = Arrays.asList("s1", "s2", "s3", "s4");
        //set up the mock data service
        response.getResources().put(new StubResource("type_a", "id1", "format1"), new StubConnectionDetail("con1").setServiceToCreate(createMockDS(returnResources)));
        //When
        PalisadeInputSplit split = new PalisadeInputSplit(response);
        prr.initialize(split, con);
        //Then
        readValuesAndValidate(returnResources.stream(), prr);
        verifyMocksCalled(response);
    }

    @Test
    public void shouldReadbackResultsFromOneResourcePlusOneEmpty() throws IOException {
        PalisadeRecordReader<String> prr = new PalisadeRecordReader<>();
        //inject a treemap to ensure iteration order
        DataRequestResponse response = new DataRequestResponse().requestId(new RequestId().id("test")).resources(new TreeMap<>());
        //set up some data
        List<String> returnResources = Arrays.asList("s1", "s2", "s3", "s4");
        response.getResources().put(new StubResource("type_a", "id1", "format1"),
                new StubConnectionDetail("con1").setServiceToCreate(createMockDS(returnResources)));
        //make an empty resource response
        response.getResources().put(new StubResource("type_b", "id2", "format2"),
                new StubConnectionDetail("con2").setServiceToCreate(createMockDS(Collections.emptyList())));
        //When
        PalisadeInputSplit split = new PalisadeInputSplit(response);
        prr.initialize(split, con);
        //Then
        readValuesAndValidate(Stream.concat(returnResources.stream(), Stream.empty()), prr);
        verifyMocksCalled(response);
    }

    @Test
    public void shouldReadbackNothingFromEmptyResource() throws IOException {
        PalisadeRecordReader<String> prr = new PalisadeRecordReader<>();
        DataRequestResponse response = new DataRequestResponse();
        //make an empty resource response
        response.getResources().put(new StubResource("type_a", "id1", "format1"),
                new StubConnectionDetail("con1").setServiceToCreate(createMockDS(Collections.emptyList())));
        //When
        PalisadeInputSplit split = new PalisadeInputSplit(response);
        prr.initialize(split, con);
        //Then
        readValuesAndValidate(Stream.empty(), prr);
        verifyMocksCalled(response);
    }

    @Test
    public void shouldReadbackResultsFromEmptyResourceThenOneResourceWithData() throws IOException {
        PalisadeRecordReader<String> prr = new PalisadeRecordReader<>();
        DataRequestResponse response = new DataRequestResponse().requestId(new RequestId().id("test")).resources(new TreeMap<>());
        //make an empty resource response
        response.getResources().put(new StubResource("type_a", "id1", "format1"),
                new StubConnectionDetail("con1").setServiceToCreate(createMockDS(Collections.emptyList())));
        //set up some data
        List<String> returnResources = Arrays.asList("s1", "s2", "s3", "s4");
        response.getResources().put(new StubResource("type_b", "id2", "format2"), new StubConnectionDetail("con2").setServiceToCreate(createMockDS(returnResources)));
        //When
        PalisadeInputSplit split = new PalisadeInputSplit(response);
        prr.initialize(split, con);
        //Then
        readValuesAndValidate(Stream.concat(Stream.empty(), returnResources.stream()), prr);
        verifyMocksCalled(response);
    }

    @Test
    public void shouldReturnResultsFromTwoResources() throws IOException {
        PalisadeRecordReader<String> prr = new PalisadeRecordReader<>();
        DataRequestResponse response = new DataRequestResponse().requestId(new RequestId().id("test")).resources(new TreeMap<>());
        //set up some data
        List<String> returnResources = Arrays.asList("s1", "s2", "s3", "s4");
        response.getResources().put(new StubResource("type_a", "id1", "format1"), new StubConnectionDetail("con1").setServiceToCreate(createMockDS(returnResources)));
        //add more data
        List<String> returnResources2 = Arrays.asList("s5", "s6", "s7", "s8");
        response.getResources().put(new StubResource("type_b", "id2", "format2"), new StubConnectionDetail("con2").setServiceToCreate(createMockDS(returnResources2)));
        //When
        PalisadeInputSplit split = new PalisadeInputSplit(response);
        prr.initialize(split, con);
        //Then
        readValuesAndValidate(Stream.concat(returnResources.stream(), returnResources2.stream()), prr);
        verifyMocksCalled(response);
    }

    @Test
    public void shouldReturnResultsFromTwoResourcesWithEmptyInBetween() throws IOException {
        PalisadeRecordReader<String> prr = new PalisadeRecordReader<>();
        DataRequestResponse response = new DataRequestResponse().requestId(new RequestId().id("test")).resources(new TreeMap<>());
        //set up some data
        List<String> returnResources = Arrays.asList("s1", "s2", "s3", "s4");
        response.getResources().put(new StubResource("type_a", "id1", "format1"), new StubConnectionDetail("con1").setServiceToCreate(createMockDS(returnResources)));
        //add an empty
        response.getResources().put(new StubResource("type_b", "id2", "format2"), new StubConnectionDetail("con2").setServiceToCreate(createMockDS(Collections.emptyList())));
        //add more data
        List<String> returnResources2 = Arrays.asList("s5", "s6", "s7", "s8");
        response.getResources().put(new StubResource("type_c", "id3", "format3"), new StubConnectionDetail("con3").setServiceToCreate(createMockDS(returnResources2)));
        //When
        PalisadeInputSplit split = new PalisadeInputSplit(response);
        prr.initialize(split, con);
        //Then
        readValuesAndValidate(Stream.concat(returnResources.stream(), returnResources2.stream()), prr);
        verifyMocksCalled(response);
    }

    @Test
    public void shouldReturnNothingFromEmpties() throws IOException {
        PalisadeRecordReader<String> prr = new PalisadeRecordReader<>();
        DataRequestResponse response = new DataRequestResponse().requestId(new RequestId().id("test")).resources(new TreeMap<>());
        //add empty resources
        response.getResources().put(new StubResource("type_a", "id1", "format1"), new StubConnectionDetail("con1").setServiceToCreate(createMockDS(Collections.emptyList())));
        response.getResources().put(new StubResource("type_b", "id2", "format2"), new StubConnectionDetail("con2").setServiceToCreate(createMockDS(Collections.emptyList())));
        response.getResources().put(new StubResource("type_c", "id3", "format3"), new StubConnectionDetail("con3").setServiceToCreate(createMockDS(Collections.emptyList())));
        //When
        PalisadeInputSplit split = new PalisadeInputSplit(response);
        prr.initialize(split, con);
        //Then
        readValuesAndValidate(Stream.empty(), prr);
        verifyMocksCalled(response);
    }
}
