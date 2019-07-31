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
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import uk.gov.gchq.palisade.cache.service.CacheService;
import uk.gov.gchq.palisade.cache.service.request.AddCacheRequest;
import uk.gov.gchq.palisade.cache.service.request.GetCacheRequest;
import uk.gov.gchq.palisade.data.serialise.Serialiser;
import uk.gov.gchq.palisade.data.serialise.SimpleStringSerialiser;
import uk.gov.gchq.palisade.data.service.DataService;
import uk.gov.gchq.palisade.data.service.reader.CachedSerialisedDataReader;
import uk.gov.gchq.palisade.data.service.reader.DataFlavour;
import uk.gov.gchq.palisade.data.service.reader.request.DataReaderRequest;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.rule.Rules;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CachedSerialisedDataReaderTest {

    private static CacheService mockCache;

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
    }

    @Test
    public void shouldcallOnceToRetrieveAndAddForAddSerialise() throws Exception {
        //Given

        //When
        CachedSerialisedDataReader.addSerialiserToCache(mockCache, DataFlavour.of("type2", "format2"), new StubSerialiser());

        //Then
        ArgumentCaptor<AddCacheRequest> captor = ArgumentCaptor.forClass(AddCacheRequest.class);
        verify(mockCache, times(1)).get(refEq(getCacheRequest, "id", "value"));

        verify(mockCache, times(1)).add(captor.capture());

        //check the new map was correct
        Map<DataFlavour, Serialiser<?>> expectedMap = new HashMap<>();
        expectedMap.put(DataFlavour.of("type1", "format1"), new StubSerialiser());
        expectedMap.put(DataFlavour.of("type2", "format2"), new StubSerialiser());

        AddCacheRequest<CachedSerialisedDataReader.MapWrap> captured = captor.getValue();

        //the add serialiser to cache call should have added the type2/format2 mapping into the map
        assertThat(captured.getValue().getInstance(), equalTo(expectedMap));
    }

    @Test
    public void shouldBeAbleToRetrieveSerialiser() {
        //Given
        TestDataReader reader = (TestDataReader) new TestDataReader()
                .cacheService(mockCache);

        //When
        reader.retrieveSerialisersFromCache();
        Serialiser<?> actual = reader.getSerialiser(DataFlavour.of("type1", "format1"));

        //Then
        assertThat(actual, equalTo(new StubSerialiser()));
    }

    @Test
    public void shouldReturnDefaultSerialiserOnNoRead() {
        //Given
        TestDataReader reader = (TestDataReader) new TestDataReader()
                .cacheService(mockCache);
        //set default serialiser
        Serialiser<?> defSerialiser = new SimpleStringSerialiser();
        reader.defaultSerialiser(defSerialiser);

        //When
        //should initially have no serialiers in cache, so return default
        Serialiser<?> actual = reader.getSerialiser(DataFlavour.of("nothing", "nothing"));

        //Then
        assertThat(actual, equalTo(defSerialiser));
    }

    @Test
    public void shouldReturnSerialiserFromCache() {
        //Given
        TestDataReader reader = (TestDataReader) new TestDataReader()
                .cacheService(mockCache);
        //set default serialiser
        Serialiser<?> defSerialiser = new SimpleStringSerialiser();
        reader.defaultSerialiser(defSerialiser);

        //When
        try {
            reader.read(new DataReaderRequest().rules(new Rules()).resource(new FileResource().type("someType").serialisedFormat("someFormat")));
        } catch (NullPointerException ignored) {
            //this will throw a null pointer because the input stream is null in the readRaw method above
        }

        Serialiser<?> actual = reader.getSerialiser(DataFlavour.of("type1", "format1"));

        //Then
        assertThat(actual, equalTo(new StubSerialiser()));
    }
}
