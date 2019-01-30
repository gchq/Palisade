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
package uk.gov.gchq.palisade.cache.service.impl;

import org.junit.Test;

import uk.gov.gchq.palisade.cache.service.request.AddCacheRequest;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class CacheMetadataTest {

    @Test
    public void shouldBeCorrectLength() {
        //Given
        byte[] test = new byte[4];
        AddCacheRequest<?> addReq = new AddCacheRequest<>().key("test").locallyCacheable(false).service(MockCacheService.class).value(test);
        //When
        byte[] actual = CacheMetadata.addMetaData(test, addReq);
        //Then
        assertThat(actual.length, is(equalTo(test.length + 1)));
    }

    @Test
    public void shouldBeCorrectFlagFalse() {
        //Given
        byte[] test = new byte[4];
        AddCacheRequest<?> addReq = new AddCacheRequest<>().key("test").locallyCacheable(false).service(MockCacheService.class).value(test);
        //When
        byte[] actual = CacheMetadata.addMetaData(test, addReq);
        //Then
        assertEquals(0, actual[0]);
    }

    @Test
    public void shouldBeCorrectFlagTrue() {
        //Given
        byte[] test = new byte[4];
        AddCacheRequest<?> addReq = new AddCacheRequest<>().key("test").locallyCacheable(true).service(MockCacheService.class).value(test);
        //When
        byte[] actual = CacheMetadata.addMetaData(test, addReq);
        //Then
        assertEquals(1, actual[0]);
    }

    @Test(expected = IllegalStateException.class)
    public void throwOnNoValue() {
        //Given
        SimpleCacheObject ob = new SimpleCacheObject(Object.class, Optional.empty());
        //When
        CacheMetadata.populateMetaData(ob);
        //Then
        fail("exception expected");
    }

    @Test(expected = IllegalStateException.class)
    public void throwOnAlreadyPopulatedMetadata() {
        //Given
        SimpleCacheObject ob = new SimpleCacheObject(Object.class, Optional.of(new byte[1])).metadata(Optional.of(new CacheMetadata(true)));
        //When
        CacheMetadata.populateMetaData(ob);
        //Then
        fail("exception expected");
    }

    @Test
    public void shouldPopulateLocallyCacheableFalse() {
        //Given
        SimpleCacheObject ob = new SimpleCacheObject(Object.class, Optional.of(new byte[]{0, 0, 0, 0, 0}));
        //When
        CacheMetadata.populateMetaData(ob);
        //Then
        assertTrue(ob.getMetadata().isPresent());
        assertFalse(ob.getMetadata().get().canBeRetrievedLocally());
    }

    @Test
    public void shouldPopulateLocallyCacheableTrue() {
        //Given
        SimpleCacheObject ob = new SimpleCacheObject(Object.class, Optional.of(new byte[]{1, 0, 0, 0, 0}));
        //When
        CacheMetadata.populateMetaData(ob);
        //Then
        assertTrue(ob.getMetadata().isPresent());
        assertTrue(ob.getMetadata().get().canBeRetrievedLocally());
    }
}