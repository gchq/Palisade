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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import uk.gov.gchq.palisade.cache.service.CacheService;
import uk.gov.gchq.palisade.cache.service.request.AddCacheRequest;
import uk.gov.gchq.palisade.cache.service.request.GetCacheRequest;
import uk.gov.gchq.palisade.data.serialise.Serialiser;
import uk.gov.gchq.palisade.data.service.DataService;
import uk.gov.gchq.palisade.data.service.reader.CachedSerialisedDataReader;
import uk.gov.gchq.palisade.data.service.reader.DataFlavour;
import uk.gov.gchq.palisade.data.service.reader.request.DataReaderRequest;
import uk.gov.gchq.palisade.data.service.reader.request.DataReaderResponse;
import uk.gov.gchq.palisade.resource.StubResource;
import uk.gov.gchq.palisade.rule.Rules;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.apache.commons.io.output.NullOutputStream.NULL_OUTPUT_STREAM;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.when;

public class SerialisedDataReaderTest {

    private CachedSerialisedDataReader reader;

    private CacheService mockCache;

    private AddCacheRequest<CachedSerialisedDataReader.MapWrap> addCacheRequest;

    private GetCacheRequest<CachedSerialisedDataReader.MapWrap> getCacheRequest;

    @Before
    public void resetCache() {
        mockCache = Mockito.mock(CacheService.class);

        addCacheRequest = new AddCacheRequest<>()
                .key(CachedSerialisedDataReader.SERIALISER_KEY)
                .service(DataService.class);

        when(mockCache.add(refEq(addCacheRequest, "id", "value"))).thenReturn(CompletableFuture.completedFuture(true));

        getCacheRequest = new GetCacheRequest<>()
                .key(CachedSerialisedDataReader.SERIALISER_KEY)
                .service(DataService.class);

        Map<DataFlavour, Serialiser<?>> serMap = new HashMap<>();
        serMap.put(DataFlavour.of("type1", "format1"), new StubSerialiser());

        when(mockCache.get(refEq(getCacheRequest, "id"))).thenReturn(
                CompletableFuture.completedFuture(
                        Optional.of(
                                new CachedSerialisedDataReader.MapWrap(serMap)
                        )
                )
        );

        reader = new TestDataReader();
        reader.serialisers(serMap);
        reader.cacheService(mockCache);
    }

    @Test(expected = IOException.class)
    public void throwOnMultipleCallsToWrite() throws IOException {
        //Given
        DataReaderRequest req = new DataReaderRequest().rules(new Rules()).resource(new StubResource().type("type1").serialisedFormat("format1"));

        //When
        DataReaderResponse response = reader.read(req);
        //make first write request
        response.getWriter().write(NULL_OUTPUT_STREAM);

        //Then - should fail on second attempt
        response.getWriter().write(NULL_OUTPUT_STREAM);
        fail("exception expected");
    }
}
