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

package uk.gov.gchq.palisade.data.service.impl;

import org.apache.commons.io.input.NullInputStream;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.audit.service.AuditService;
import uk.gov.gchq.palisade.cache.service.CacheService;
import uk.gov.gchq.palisade.cache.service.request.AddCacheRequest;
import uk.gov.gchq.palisade.cache.service.request.GetCacheRequest;
import uk.gov.gchq.palisade.data.serialise.Serialiser;
import uk.gov.gchq.palisade.data.serialise.SimpleStringSerialiser;
import uk.gov.gchq.palisade.data.service.DataService;
import uk.gov.gchq.palisade.data.service.reader.CachedSerialisedDataReader;
import uk.gov.gchq.palisade.data.service.reader.DataFlavour;
import uk.gov.gchq.palisade.data.service.reader.request.DataReaderRequest;
import uk.gov.gchq.palisade.data.service.reader.request.DataReaderResponse;
import uk.gov.gchq.palisade.resource.StubResource;
import uk.gov.gchq.palisade.rule.Rules;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static org.apache.commons.io.output.NullOutputStream.NULL_OUTPUT_STREAM;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

public class SerialisedDataReaderTest {

    private CachedSerialisedDataReader reader;

    private CacheService mockCache;

    private AuditService mockAudit;

    private AddCacheRequest<CachedSerialisedDataReader.MapWrap> addCacheRequest;

    private GetCacheRequest<CachedSerialisedDataReader.MapWrap> getCacheRequest;

    private DataReaderRequest request;

    @Before
    public void resetCache() {
        mockCache = Mockito.mock(CacheService.class);
        mockAudit = Mockito.mock(AuditService.class);

        addCacheRequest = new AddCacheRequest<>()
                .key(CachedSerialisedDataReader.SERIALISER_KEY)
                .service(DataService.class);

        when(mockCache.add(refEq(addCacheRequest, "id", "value"))).thenReturn(CompletableFuture.completedFuture(true));

        getCacheRequest = new GetCacheRequest<>()
                .key(CachedSerialisedDataReader.SERIALISER_KEY)
                .service(DataService.class);

        Map<DataFlavour, Serialiser<?>> serMap = new HashMap<>();
        serMap.put(DataFlavour.of("type1", "format1"), new SimpleStringSerialiser());

        when(mockCache.get(refEq(getCacheRequest, "id"))).thenReturn(
                CompletableFuture.completedFuture(
                        Optional.of(
                                new CachedSerialisedDataReader.MapWrap(serMap)
                        )
                )
        );

        reader = new TestDataReader(new NullInputStream(10), mockAudit);
        reader.serialisers(serMap);
        reader.cacheService(mockCache);

        request = new DataReaderRequest().rules(new Rules()).resource(new StubResource().type("type1").serialisedFormat("format1"));
        request.originalRequestId(new RequestId().id("originalRequestId"));
    }

    @Test(expected = IOException.class)
    public void throwOnMultipleCallsToWrite() throws IOException {
        //Given
        DataReaderResponse response = reader.read(request);

        //When
        //make first write request
        response.getWriter().write(NULL_OUTPUT_STREAM);

        //Then - should fail on second attempt
        response.getWriter().write(NULL_OUTPUT_STREAM);
        fail("exception expected");
    }

    @Test
    public void shouldCloseInputStreamNormally() throws IOException {
        //Given

        //flag that is set once stream is closed
        AtomicBoolean closed = new AtomicBoolean(false);
        DataReaderResponse response = createTestResponseForStringStream(closed);

        //When
        assertFalse(closed.get());
        response.getWriter().write(NULL_OUTPUT_STREAM);

        //Then
        //close should have been called
        assertTrue(closed.get());
    }

    @Test
    public void shouldCloseInputStreamExceptionally() throws IOException {
        //Given
        //flag that is set once stream is closed
        AtomicBoolean closed = new AtomicBoolean(false);
        DataReaderResponse response = createTestResponseForStringStream(closed);

        //create an output stream that throws an exception
        OutputStream exceptionStream = Mockito.mock(OutputStream.class);
        doThrow(IOException.class).when(exceptionStream).write(anyInt());
        doThrow(IOException.class).when(exceptionStream).write(any());
        doThrow(IOException.class).when(exceptionStream).write(any(), anyInt(), anyInt());

        //When
        assertFalse(closed.get());
        try {
            response.getWriter().write(exceptionStream);
        } catch (IOException expected) {
        }

        //Then
        assertTrue(closed.get());
    }

    private DataReaderResponse createTestResponseForStringStream(final AtomicBoolean closed) {
        //create a simple input stream
        SimpleStringSerialiser ser = new SimpleStringSerialiser();
        ByteArrayOutputStream serialisedOS = new ByteArrayOutputStream();
        ser.serialise(Stream.of("line1", "line2"), serialisedOS);

        //create an input stream we can test for closure
        InputStream serialisedSource = new FilterInputStream(new ByteArrayInputStream(serialisedOS.toByteArray())) {
            @Override
            public void close() throws IOException {
                super.close();
                closed.set(true);
            }
        };

        //inject this into a data reader
        reader = new TestDataReader(serialisedSource, mockAudit);
        Map<DataFlavour, Serialiser<?>> serMap = new HashMap<>();
        serMap.put(DataFlavour.of("type1", "format1"), ser);
        reader.serialisers(serMap);
        reader.cacheService(mockCache);

        return reader.read(request);
    }
}
