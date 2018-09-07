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

import uk.gov.gchq.palisade.cache.service.CacheService;
import uk.gov.gchq.palisade.cache.service.request.AddCacheRequest;
import uk.gov.gchq.palisade.cache.service.request.GetCacheRequest;
import uk.gov.gchq.palisade.cache.service.request.ListCacheRequest;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public class BasicCacheServiceTest {
    private BasicCacheService service;

    private static BackingStore store;
    private static CacheService fakeService;

    private final static String BASE_KEY_1 = "test";
    private final static String BASE_KEY_2 = "test2";
    private final static String NOTHING = "nothing";

    private static String KEY_1;
    private static String VALUE_1 = "hello this is a test";

    private static String KEY_2;
    private static String VALUE_2 = "another test string";

    private static String KEY_3;
    private static String VALUE_3 = "alternate service string";

    private static String KEY_4;
    private static String VALUE_4 = "another alternate string";

    private static String NO_KEY;

    private static Function<String, byte[]> encoder;

    @BeforeClass
    public static void setup() {
        store = Mockito.mock(BackingStore.class);
        fakeService = Mockito.mock(CacheService.class);

        KEY_1 = new AddCacheRequest<String>()
                .service(MockCacheService.class)
                .key(BASE_KEY_1)
                .value(VALUE_1)
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

        NO_KEY = new GetCacheRequest<String>()
                .service(MockCacheService.class)
                .key(NOTHING)
                .makeBaseName();

        when(store.retrieve(any())).thenReturn(new BasicCacheObject(Object.class, Optional.empty()));

        //get the string encoder
        encoder = new BasicCacheService().getCodecs().getValueEncoder(String.class);

        //configure backing store to act with test data
        byte[] encoded = encoder.apply(VALUE_1);
        when(store.store(eq(KEY_1), eq(String.class), eq(encoded), any())).thenReturn(Boolean.TRUE);
        when(store.retrieve(KEY_1)).thenReturn(new BasicCacheObject(String.class, Optional.of(encoded)));

        byte[] encoded2 = encoder.apply(VALUE_2);
        when(store.store(eq(KEY_2), eq(String.class), eq(encoded2), any())).thenReturn(Boolean.TRUE);
        when(store.retrieve(KEY_2)).thenReturn(new BasicCacheObject(String.class, Optional.of(encoded2)));

        //set an entry in a different Service

        byte[] encoded3 = encoder.apply(VALUE_3);
        when(store.store(eq(KEY_3), eq(String.class), eq(encoded3), any())).thenReturn(Boolean.TRUE);
        when(store.retrieve(KEY_3)).thenReturn(new BasicCacheObject(String.class, Optional.of(encoded3)));

        byte[] encoded4 = encoder.apply(VALUE_4);
        when(store.store(eq(KEY_4), eq(String.class), eq(encoded4), any())).thenReturn(Boolean.TRUE);
        when(store.retrieve(KEY_4)).thenReturn(new BasicCacheObject(String.class, Optional.of(encoded4)));

        //set up the lists
        when(store.list(eq(((ListCacheRequest) new ListCacheRequest().service(MockCacheService.class)).prefix("").makeBaseName())))
                .thenReturn(Stream.<String>of(KEY_1, KEY_2));

        when(store.list(eq(((ListCacheRequest) new ListCacheRequest().service(fakeService.getClass())).prefix("").makeBaseName())))
                .thenReturn(Stream.<String>of(KEY_3, KEY_4));

        when(store.list(eq(((ListCacheRequest) new ListCacheRequest().service(MockCacheService.class)).prefix(NOTHING).makeBaseName())))
                .thenReturn(Stream.empty());

        //set up a return of an empty object
        when(store.retrieve(eq(NO_KEY))).thenReturn(new BasicCacheObject(Object.class, Optional.empty()));
    }

    @Before
    public void createCache() {
        service = new BasicCacheService().backingStore(store);
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
        //Given - configure a separate backing store for this test
        BackingStore uniqueStore = Mockito.mock(BackingStore.class);
        when(uniqueStore.retrieve(any())).thenReturn(new BasicCacheObject(Object.class, Optional.empty()));
        byte[] encoded = encoder.apply(VALUE_1);
        when(uniqueStore.store(eq(KEY_1), eq(String.class), eq(encoded), any())).thenReturn(Boolean.TRUE);
        when(uniqueStore.retrieve(KEY_1)).thenReturn(new BasicCacheObject(String.class, Optional.of(encoded)));

        byte[] encoded4 = encoder.apply(VALUE_4);
        when(uniqueStore.store(eq(KEY_4), eq(String.class), eq(encoded4), any())).thenReturn(Boolean.TRUE);
        when(uniqueStore.retrieve(KEY_4)).thenReturn(new BasicCacheObject(String.class, Optional.of(encoded4)));

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
        new BasicCacheService().getBackingStore();
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
}