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
import uk.gov.gchq.palisade.service.request.DataRequestResponse;
import uk.gov.gchq.palisade.service.request.StubConnectionDetail;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

public class PalisadeRecordReaderTest {
    /*
    * Tests to write
    * make the stubconnectiondetail return dataservice that can either return results or nothing
    * test nothing else returned after close called
    * test all records come back from one and two resources
    * test records come back from empty resources
    * test records come back from one resource one empty
    * test iterator cascades with one then empty then one
    * use a treemap
    * test should return each key value in turn correctly
     */

    @BeforeClass
    public static void setup() {
        conf = new Configuration();
        //make sure this is available for the tests
        serialiser = new StubSerialiser("test_serialiser");
        PalisadeInputFormat.setSerialiser(conf, serialiser);
        con = new TaskAttemptContextImpl(conf, new TaskAttemptID());
        //make the data service that can either respond with some fake
        //data or an empty stream
        mockDataService = mock(DataService.class);
        //explicitly declare the generic type as Mockito thinks it should
        //be ReadResponse<Object>
        readResponse = new ReadResponse<>();
        Mockito.<CompletableFuture<ReadResponse<String>>>when(mockDataService.read(any(ReadRequest.class))).thenReturn(CompletableFuture.completedFuture(readResponse));
    }

    private static Configuration conf;
    private static TaskAttemptContext con;
    private static Serialiser<String, String> serialiser;
    private static DataService mockDataService;
    private static ReadResponse<String> readResponse;

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
        PalisadeInputSplit is = new PalisadeInputSplit(new RequestId("test"), new HashMap<>());
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
        response.getResources().put(new StubResource("type_a", "id1", "format6"), new StubConnectionDetail("con1").setServiceToCreate(mockDataService));
        response.getResources().put(new StubResource("type_b", "id2", "format7"), new StubConnectionDetail("con2").setServiceToCreate(mockDataService));

        readResponse.setData(Stream.of("value1", "value2"));

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
}
