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

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import sun.misc.Cache;
import uk.gov.gchq.palisade.cache.service.CacheService;
import uk.gov.gchq.palisade.cache.service.request.AddCacheRequest;
import uk.gov.gchq.palisade.cache.service.request.GetCacheRequest;
import uk.gov.gchq.palisade.cache.service.request.ListCacheRequest;
import uk.gov.gchq.palisade.cache.service.request.RemoveCacheRequest;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public class SimpleCacheServiceTest {
    private SimpleCacheService service;

    private static BackingStore store;
    private static CacheService fakeService;

    private final static String BASE_KEY_1 = "test";
    private final static String BASE_KEY_2 = "test2";
    private final static String BASE_KEY_3 = "test3";
    private final static String BASE_KEY_4 = "test4";
    private final static String NOTHING = "nothing";

    private static String KEY_1;
    private static String VALUE_1 = "hello this is a test";

    private static String KEY_2;
    private static String VALUE_2 = "another test string";

    private static String KEY_3;
    private static String VALUE_3 = "alternate service string";

    private static String KEY_4;
    private static String VALUE_4 = "another alternate string";

    private static String KEY_5;
    private static String KEY_6;
    private static String VALUE_5 = "some more test data";

    private static String NO_KEY;

    private static Function<String, byte[]> encoder;

    @BeforeClass
    public static void setup() {
        store = Mockito.mock(BackingStore.class);
        fakeService = Mockito.mock(CacheService.class);

        KEY_1 = new AddCacheRequest<String>()
                .service(MockCacheService.class)
                .key(BASE_KEY_1)
                .makeBaseName();
        KEY_2 = new AddCacheRequest<String>()
                .service(MockCacheService.class)
                .key(BASE_KEY_2)
                .makeBaseName();
        KEY_3 = new AddCacheRequest<String>()
                .service(fakeService.getClass())
                .key(BASE_KEY_1)
                .makeBaseName();
        KEY_4 = new AddCacheRequest<String>()
                .service(fakeService.getClass())
                .key(BASE_KEY_2)
                .makeBaseName();
        KEY_5 = new AddCacheRequest<String>()
                .service(MockCacheService.class)
                .key(BASE_KEY_3)
                .makeBaseName();
        KEY_6 = new AddCacheRequest<String>()
                .service(MockCacheService.class)
                .key(BASE_KEY_4)
                .makeBaseName();

        NO_KEY = new GetCacheRequest<String>()
                .service(MockCacheService.class)
                .key(NOTHING)
                .makeBaseName();

        when(store.get(any())).thenReturn(new SimpleCacheObject(Object.class, Optional.empty(), Optional.empty()));

        //get the string encoder
        encoder = new SimpleCacheService().getCodecs().getValueEncoder(String.class);

        AddCacheRequest<?> addRequest = new AddCacheRequest<>().locallyCacheable(false);
        AddCacheRequest<?> addRequestCachedLocal = new AddCacheRequest<>().locallyCacheable(true);

        //configure backing add to act with test data
        byte[] encoded = CacheMetadata.addMetaData(encoder.apply(VALUE_1), addRequest);
        byte[] encoded_local_cache = CacheMetadata.addMetaData(encoder.apply(VALUE_1), addRequestCachedLocal);
        when(store.add(eq(KEY_1), eq(String.class), eq(encoded), any())).thenReturn(Boolean.TRUE);
        when(store.add(eq(KEY_1), eq(String.class), eq(encoded_local_cache), any())).thenReturn(Boolean.TRUE);
        when(store.get(KEY_1)).thenReturn(new SimpleCacheObject(String.class, Optional.of(encoded), Optional.empty()));

        byte[] encoded2 = CacheMetadata.addMetaData(encoder.apply(VALUE_2), addRequest);
        when(store.add(eq(KEY_2), eq(String.class), eq(encoded2), any())).thenReturn(Boolean.TRUE);
        when(store.get(KEY_2)).thenReturn(new SimpleCacheObject(String.class, Optional.of(encoded2), Optional.empty()));

        //set an entry in a different Service

        byte[] encoded3 = CacheMetadata.addMetaData(encoder.apply(VALUE_3), addRequest);
        when(store.add(eq(KEY_3), eq(String.class), eq(encoded3), any())).thenReturn(Boolean.TRUE);
        when(store.get(KEY_3)).thenReturn(new SimpleCacheObject(String.class, Optional.of(encoded3), Optional.empty()));

        byte[] encoded4 = CacheMetadata.addMetaData(encoder.apply(VALUE_4), addRequest);
        when(store.add(eq(KEY_4), eq(String.class), eq(encoded4), any())).thenReturn(Boolean.TRUE);
        when(store.get(KEY_4)).thenReturn(new SimpleCacheObject(String.class, Optional.of(encoded4), Optional.empty()));

        //set up the lists
        when(store.list(eq(((ListCacheRequest) new ListCacheRequest().service(MockCacheService.class)).prefix("").makeBaseName())))
                .thenReturn(Stream.<String>of(KEY_1, KEY_2));

        when(store.list(eq(((ListCacheRequest) new ListCacheRequest().service(fakeService.getClass())).prefix("").makeBaseName())))
                .thenReturn(Stream.<String>of(KEY_3, KEY_4));

        when(store.list(eq(((ListCacheRequest) new ListCacheRequest().service(MockCacheService.class)).prefix(NOTHING).makeBaseName())))
                .thenReturn(Stream.empty());

        //set up the remove
        when(store.remove(eq(KEY_1))).thenReturn(Boolean.TRUE);

        //set up a return of an empty object
        when(store.get(eq(NO_KEY))).thenReturn(new SimpleCacheObject(Object.class, Optional.empty(), Optional.empty()));

        //set up the return of locally cacheable objects
        byte[] encoded5 = CacheMetadata.addMetaData(encoder.apply(VALUE_5), addRequestCachedLocal);
        when(store.add(eq(KEY_5), eq(String.class), any(), any())).thenReturn(Boolean.TRUE);
        when(store.get(eq(KEY_5))).thenReturn(new SimpleCacheObject(String.class, Optional.of(encoded5), Optional.empty()));
        //set up a  specifically non local cacheable object
        byte[] encoded6 = CacheMetadata.addMetaData(encoder.apply(VALUE_5), addRequest);
        when(store.add(eq(KEY_6), eq(String.class), any(), any())).thenReturn(Boolean.TRUE);
        when(store.get(eq(KEY_6))).thenReturn(new SimpleCacheObject(String.class, Optional.of(encoded6), Optional.empty()));

    }

    @Before
    public void createCache() {
        service = new SimpleCacheService().backingStore(store);
    }

    @Test
    public void shouldReturnEmptyList() {
        //Given - nothing
        //When
        CompletableFuture<Stream<String>> getFuture = service.list(((ListCacheRequest) new ListCacheRequest()
                .service(MockCacheService.class)).prefix(NOTHING));

        //Then
        assertTrue(StreamUtil.streamEqual(Stream.empty(), getFuture.join()));
    }

    @Test
    public void shouldReturnNormalList() {
        //Given
        AddCacheRequest<String> req = new AddCacheRequest<String>().service(MockCacheService.class).key(BASE_KEY_1).value(VALUE_1);
        CompletableFuture<Boolean> future = service.add(req);
        assertTrue(future.join());
        AddCacheRequest<String> req2 = new AddCacheRequest<String>().service(MockCacheService.class).key(BASE_KEY_2).value(VALUE_2);
        CompletableFuture<Boolean> future2 = service.add(req2);
        assertTrue(future2.join());
        //When
        CompletableFuture<Stream<String>> getFuture = service.list(((ListCacheRequest) new ListCacheRequest().service(MockCacheService.class))
                .prefix(""));
        //Then
        assertTrue(StreamUtil.streamEqual(Stream.of(BASE_KEY_1, BASE_KEY_2), getFuture.join()));
    }

    @Test
    public void shouldReturnSameValue() {
        //Given
        AddCacheRequest<String> req = new AddCacheRequest<String>().service(MockCacheService.class).key(BASE_KEY_1).value(VALUE_1);
        CompletableFuture<Boolean> future = service.add(req);
        assertTrue(future.join());
        AddCacheRequest<String> req2 = new AddCacheRequest<String>().service(MockCacheService.class).key(BASE_KEY_2).value(VALUE_2);
        CompletableFuture<Boolean> future2 = service.add(req2);
        assertTrue(future2.join());
        //When
        CompletableFuture<Optional<String>> getFuture = service.get(new GetCacheRequest<String>().service(MockCacheService.class).key(BASE_KEY_1));
        //Then
        assertEquals(VALUE_1, getFuture.join().get());
    }

    @Test
    public void shouldReturnTwoValues() {
        //Given
        AddCacheRequest<String> req = new AddCacheRequest<String>().service(MockCacheService.class).key(BASE_KEY_1).value(VALUE_1);
        CompletableFuture<Boolean> future = service.add(req);
        assertTrue(future.join());
        AddCacheRequest<String> req2 = new AddCacheRequest<String>().service(MockCacheService.class).key(BASE_KEY_2).value(VALUE_2);
        CompletableFuture<Boolean> future2 = service.add(req2);
        assertTrue(future2.join());
        //When
        CompletableFuture<Optional<String>> getFuture = service.get(new GetCacheRequest<String>().service(MockCacheService.class).key(BASE_KEY_1));
        CompletableFuture<Optional<String>> getFuture2 = service.get(new GetCacheRequest<String>().service(MockCacheService.class).key(BASE_KEY_2));

        //Then
        assertEquals(VALUE_1, getFuture.join().get());
        assertEquals(VALUE_2, getFuture2.join().get());
    }

    @Test
    public void shouldReturnDifferentValuesSameKeyDifferentService() {
        //Given
        AddCacheRequest<String> req = new AddCacheRequest<String>().service(MockCacheService.class).key(BASE_KEY_1).value(VALUE_1);
        CompletableFuture<Boolean> future = service.add(req);
        assertTrue(future.join());
        AddCacheRequest<String> req2 = new AddCacheRequest<String>().service(fakeService.getClass()).key(BASE_KEY_1).value(VALUE_3);
        CompletableFuture<Boolean> future2 = service.add(req2);
        assertTrue(future2.join());
        //When
        CompletableFuture<Optional<String>> getFuture = service.get(new GetCacheRequest<String>().service(MockCacheService.class).key(BASE_KEY_1));
        Optional<String> actual = getFuture.join();
        CompletableFuture<Optional<String>> getFuture2 = service.get(new GetCacheRequest<String>().service(fakeService.getClass()).key(BASE_KEY_1));
        Optional<String> actual2 = getFuture2.join();

        //Then
        assertEquals(VALUE_1, actual.get());
        assertEquals(VALUE_3, actual2.get());
    }

    @Test
    public void shouldNotShareKeysAcrossServices() {
        //Given - configure a separate backing add for this test
        BackingStore uniqueStore = Mockito.mock(BackingStore.class);
        AddCacheRequest<?> addRequest = new AddCacheRequest<>().locallyCacheable(false);
        when(uniqueStore.get(any())).thenReturn(new SimpleCacheObject(Object.class, Optional.empty(), Optional.empty()));
        byte[] encoded = CacheMetadata.addMetaData(encoder.apply(VALUE_1), addRequest);
        when(uniqueStore.add(eq(KEY_1), eq(String.class), eq(encoded), any())).thenReturn(Boolean.TRUE);
        when(uniqueStore.get(KEY_1)).thenReturn(new SimpleCacheObject(String.class, Optional.of(encoded), Optional.empty()));

        byte[] encoded4 = CacheMetadata.addMetaData(encoder.apply(VALUE_4), addRequest);
        when(uniqueStore.add(eq(KEY_4), eq(String.class), eq(encoded4), any())).thenReturn(Boolean.TRUE);
        when(uniqueStore.get(KEY_4)).thenReturn(new SimpleCacheObject(String.class, Optional.of(encoded4), Optional.empty()));

        try {
            service.backingStore(uniqueStore);
            AddCacheRequest<String> req = new AddCacheRequest<String>().service(MockCacheService.class).key(BASE_KEY_1).value(VALUE_1);
            CompletableFuture<Boolean> future = service.add(req);
            assertTrue(future.join());
            AddCacheRequest<String> req2 = new AddCacheRequest<String>().service(fakeService.getClass()).key(BASE_KEY_2).value(VALUE_4);
            CompletableFuture<Boolean> future2 = service.add(req2);
            assertTrue(future2.join());
            //When
            CompletableFuture<Optional<String>> getFuture = service.get(new GetCacheRequest<String>().service(fakeService.getClass()).key(BASE_KEY_1));
            //Then
            assertFalse(getFuture.join().isPresent());
        } finally {
            service.backingStore(store);
        }
    }

    @Test
    public void shouldReturnEmptyCorrectly() {
        //Given - nothing
        //When
        CompletableFuture<Optional<String>> result = service.get(new GetCacheRequest<String>().service(MockCacheService.class).key(NOTHING));
        //Then
        assertFalse(result.join().isPresent());
    }

    @Test
    public void shouldNotRemoveFromWrongService() {
        //Given
        AddCacheRequest<String> req = new AddCacheRequest<String>().service(MockCacheService.class).key(BASE_KEY_1).value(VALUE_1);
        CompletableFuture<Boolean> future = service.add(req);
        assertTrue(future.join());
        AddCacheRequest<String> req2 = new AddCacheRequest<String>().service(fakeService.getClass()).key(BASE_KEY_1).value(VALUE_3);
        CompletableFuture<Boolean> future2 = service.add(req2);
        assertTrue(future2.join());

        //When
        RemoveCacheRequest request = new RemoveCacheRequest().service(MockCacheService.class).key(BASE_KEY_1);
        CompletableFuture<Boolean> answer = service.remove(request);

        //Then
        assertTrue(answer.join());
    }

    @Test
    public void shouldNotErrorOnLocallyCacheLowTTL() {
        //Given
        AddCacheRequest<String> req = new AddCacheRequest<String>().service(MockCacheService.class).key(BASE_KEY_1).value(VALUE_1).timeToLive(2000).locallyCacheable(true);
        //When
        CompletableFuture<Boolean> future = service.add(req);
        //Then
        assertTrue(future.join());
    }

    @Test
    public void shouldNotRetrieveLocallyFirstTime() {
        //Given
        AddCacheRequest<String> req = new AddCacheRequest<String>().service(MockCacheService.class).key(BASE_KEY_3).value(VALUE_5).timeToLive(2000).locallyCacheable(true);
        CompletableFuture<Boolean> future = service.add(req);
        assertTrue(future.join());
        //When
        SimpleCacheObject retrieved = service.doCacheRetrieve(KEY_5, Duration.ofMillis(50));
        //Then
        assertTrue(retrieved.metadata().get().canBeRetrievedLocally());
        assertFalse(retrieved.metadata().get().wasRetrievedLocally());
    }

    @Test
    public void shouldRetrieveLocallySecondTime() {
        //Given
        AddCacheRequest<String> req = new AddCacheRequest<String>().service(MockCacheService.class).key(BASE_KEY_3).value(VALUE_5).timeToLive(2000).locallyCacheable(true);
        CompletableFuture<Boolean> future = service.add(req);
        assertTrue(future.join());
        //When
        SimpleCacheObject firstRetrieve = service.doCacheRetrieve(KEY_5, Duration.ofMillis(50));
        SimpleCacheObject secondRetrieve = service.doCacheRetrieve(KEY_5, Duration.ofMillis(50));

        //Then
        assertTrue(firstRetrieve.metadata().get().canBeRetrievedLocally());
        assertFalse(firstRetrieve.metadata().get().wasRetrievedLocally());
        //second time, the request should have been served locally
        assertTrue(secondRetrieve.metadata().get().canBeRetrievedLocally());
        assertTrue(secondRetrieve.metadata().get().wasRetrievedLocally());
    }

    @Test
    public void shouldRetrieveNotRetrieveLocallyAfterExpiry() throws InterruptedException {
        //Given
        AddCacheRequest<String> req = new AddCacheRequest<String>().service(MockCacheService.class).key(BASE_KEY_3).value(VALUE_5).timeToLive(2000).locallyCacheable(true);
        CompletableFuture<Boolean> future = service.add(req);
        assertTrue(future.join());
        //When
        SimpleCacheObject firstRetrieve = service.doCacheRetrieve(KEY_5, Duration.ofMillis(50));
        SimpleCacheObject secondRetrieve = service.doCacheRetrieve(KEY_5, Duration.ofMillis(50));
        Thread.sleep(100);
        SimpleCacheObject thirdRetrieve = service.doCacheRetrieve(KEY_5, Duration.ofMillis(50));

        //Then
        assertTrue(firstRetrieve.metadata().get().canBeRetrievedLocally());
        assertFalse(firstRetrieve.metadata().get().wasRetrievedLocally());
        //second time, the request should have been served locally
        assertTrue(secondRetrieve.metadata().get().canBeRetrievedLocally());
        assertTrue(secondRetrieve.metadata().get().wasRetrievedLocally());
        //after waiting, the local cache should have expired
        assertTrue(thirdRetrieve.metadata().get().canBeRetrievedLocally());
        assertFalse(thirdRetrieve.metadata().get().wasRetrievedLocally());
    }

    @Test
    public void shouldNotRetrieveFromLocalCache() {
        //Given - a non local cacheable object
        AddCacheRequest<String> req = new AddCacheRequest<String>().service(MockCacheService.class).key(BASE_KEY_4).value(VALUE_5).timeToLive(2000).locallyCacheable(false);
        CompletableFuture<Boolean> future = service.add(req);
        assertTrue(future.join());
        //When
        SimpleCacheObject firstRetrieve = service.doCacheRetrieve(KEY_6, Duration.ofMillis(50));
        SimpleCacheObject secondRetrieve = service.doCacheRetrieve(KEY_6, Duration.ofMillis(50));

        //Then
        assertFalse(firstRetrieve.metadata().get().canBeRetrievedLocally());
        assertFalse(firstRetrieve.metadata().get().wasRetrievedLocally());
        assertFalse(secondRetrieve.metadata().get().canBeRetrievedLocally());
        assertFalse(secondRetrieve.metadata().get().wasRetrievedLocally());
    }

    @Test(expected = NullPointerException.class)
    public void throwOnNullBackingStore() {
        //Given - nothing
        //When
        service.backingStore(null);
        //Then
        fail("exception expected");
    }

    @Test(expected = NullPointerException.class)
    public void throwOnGetWithNoBackingStore() {
        //Given - nothing
        //When
        new SimpleCacheService().getBackingStore();
        //Then
        fail("exception expected");
    }

    @Test(expected = NullPointerException.class)
    public void throwOnNullAdd() {
        //Given - nothing
        //When
        service.add(null);
        //Then
        fail("exception expected");
    }

    @Test(expected = NullPointerException.class)
    public void throwOnNullGet() {
        //Given - nothing
        //When
        service.get(null);
        //Then
        fail("exception expected");
    }

    @Test(expected = NullPointerException.class)
    public void throwOnNullList() {
        //Given - nothing
        //When
        service.list(null);
        //Then
        fail("exception expected");
    }

    @Test(expected = NullPointerException.class)
    public void throwOnNullRemove() {
        //Given - nothing
        //When
        service.remove(null);
        //Then
        fail("exception expected");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwOnHighTTLLocalCacheItem() {
        //Given - nothing
        //When
        service.add(new AddCacheRequest<Object>()
                .service(MockCacheService.class)
                .key("test").value(new Object())
                .locallyCacheable(true)
                .timeToLive(Optional.of(Duration.ofDays(365))));
        //Then
        fail("exception expected");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwOnNoTTLLocalCacheItem() {
        //Given - nothing
        //When
        service.add(new AddCacheRequest<Object>()
                .service(MockCacheService.class)
                .key("test").value(new Object())
                .locallyCacheable(true));
        //Then
        fail("exception expected");
    }
}