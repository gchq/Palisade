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
package uk.gov.gchq.palisade.client;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.data.service.DataService;
import uk.gov.gchq.palisade.data.service.impl.MockDataService;
import uk.gov.gchq.palisade.data.service.request.ReadRequest;
import uk.gov.gchq.palisade.data.service.request.ReadResponse;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.service.ConnectionDetail;
import uk.gov.gchq.palisade.service.PalisadeService;
import uk.gov.gchq.palisade.service.SimpleConnectionDetail;
import uk.gov.gchq.palisade.service.impl.MockPalisadeService;
import uk.gov.gchq.palisade.service.request.DataRequestResponse;
import uk.gov.gchq.palisade.service.request.RegisterDataRequest;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;

public class CatClientTest {

    // mock creation
    private static final PalisadeService mockPalisadeService = MockPalisadeService.getMock();
    private static final DataService mockDataService = MockDataService.getMock();
    private static final ConnectionDetail mockConnectionDetail = new SimpleConnectionDetail().service(mockDataService);

    private static RegisterDataRequest registerDataRequest; // Client to Palisade service
    private static CompletableFuture<DataRequestResponse> reqResponse; // Palisade to Client response
    private static ReadRequest readRequest1; // Client to Data Service request 1
    private static ReadRequest readRequest2; // Client to Data Service request 2
    private static CompletableFuture<ReadResponse> readResponse1; // Data Service to Client response 1
    private static CompletableFuture<ReadResponse> readResponse2; // Data Service to Client response 2

    private static FileResource resource1 = new FileResource().id("resource 1");
    private static FileResource resource2 = new FileResource().id("resource 2");

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @Test
    public void testClientToPalisade() throws InterruptedException {
        //Given
        String dir = "test directory";
        String userId = "Alice";
        String purpose = "test purpose";

        RequestId reqId = new RequestId().id("Test ID");

        registerDataRequest = new RegisterDataRequest().userId(new UserId().id(userId)).resourceId(dir).context(new Context().purpose(purpose));

        reqResponse = CompletableFuture.completedFuture(
                new DataRequestResponse()
                .requestId(reqId)
                .resource(resource1, mockConnectionDetail)
                .resource(resource2, mockConnectionDetail));


        readRequest1 = new ReadRequest().requestId(reqId).resource(resource1);
        readRequest2 = new ReadRequest().requestId(reqId).resource(resource2);

        readResponse1 = CompletableFuture.completedFuture(
                new ReadResponse().data(IOUtils.toInputStream("Test data 1", StandardCharsets.UTF_8)));

        readResponse2 = CompletableFuture.completedFuture(
                new ReadResponse().data(IOUtils.toInputStream("Test data 2", StandardCharsets.UTF_8)));

        Mockito.when(mockPalisadeService.registerDataRequest(Mockito.refEq(registerDataRequest, "id"))).thenReturn(reqResponse);
        Mockito.when(mockDataService.read(Mockito.refEq(readRequest1, "id"))).thenReturn(readResponse1);
        Mockito.when(mockDataService.read(Mockito.refEq(readRequest2,"id"))).thenReturn(readResponse2);

        //When
        CatClient catClient = new CatClient(mockPalisadeService);
        catClient.read(userId, dir, purpose);

        //Verify

        Mockito.verify(mockPalisadeService, Mockito.times(1)).registerDataRequest(Mockito.refEq(registerDataRequest, "id"));
        Mockito.verify(mockDataService, Mockito.times(1)).read(Mockito.refEq(readRequest1, "id"));
        Mockito.verify(mockDataService, Mockito.times(1)).read(Mockito.refEq(readRequest2, "id"));

        //Then
        assertEquals("Test data 1\nTest data 2\n", outContent.toString());
    }

    @After
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }
}
