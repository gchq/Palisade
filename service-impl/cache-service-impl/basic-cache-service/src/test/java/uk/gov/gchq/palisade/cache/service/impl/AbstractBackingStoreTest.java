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
import org.junit.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public abstract class AbstractBackingStoreTest {

    protected BackingStore impl;

    public abstract BackingStore createBackingStore();

    @Before
    public void initEmptyStore() {
        impl = createBackingStore();
    }

    /**
     * Compare two streams for equality. Each stream must be of the same length and contain the same elements (by
     * value). The streams are sorted beforehand. Therefore T must be naturally comparable.
     *
     * @param expected first stream
     * @param actual   second stream
     * @param <T>      type of list element
     * @return true if streams are equal
     */
    private static <T> boolean areEqual(final Stream<? extends T> expected, final Stream<? extends T> actual) {
        Stream<? extends T> sort_expected = expected.sorted();
        Stream<? extends T> sort_actual = actual.sorted();
        List<? extends T> lhs = sort_expected.collect(Collectors.toList());
        List<? extends T> rhs = sort_actual.collect(Collectors.toList());
        return lhs.equals(rhs);
    }

    //List tests

    @Test
    public void shouldReturnEmptyListFromEmptyStore() {
        //Given - nothing
        //When
        Stream<String> ret = impl.list("anything");
        //Then
        assertTrue(areEqual(Stream.empty(), ret));
    }

    @Test
    public void shouldReturnTwoElementsFromTwoKeys() {
        //Given - two keys
        byte[] b1 = new byte[10];
        byte[] b2 = new byte[10];
        impl.store("test_key1", Object.class, b1);
        impl.store("test_key2", Object.class, b2);
        //When
        Stream<String> ret = impl.list("test");
        //Then
        assertTrue(areEqual(Stream.of("test_key1", "test_key2"), ret));
    }

    @Test
    public void shouldReturnOneElementFromTwoDifferentKeys() {
        //Given - two keys
        byte[] b1 = new byte[10];
        byte[] b2 = new byte[10];
        impl.store("foo_key1", Object.class, b1);
        impl.store("bar_key2", Object.class, b2);
        //When
        Stream<String> ret = impl.list("foo");
        Stream<String> ret2 = impl.list("bar");
        //Then
        assertTrue(areEqual(Stream.of("foo_key1"), ret));
        assertTrue(areEqual(Stream.of("bar_key2"), ret2));
    }

    @Test
    public void shouldReturnNothingFromTwoDifferentKeys() {
        //Given - two keys
        byte[] b1 = new byte[10];
        byte[] b2 = new byte[10];
        impl.store("foo_key1", Object.class, b1);
        impl.store("bar_key2", Object.class, b2);
        //When
        Stream<String> ret = impl.list("not_there");
        //Then
        assertTrue(areEqual(Stream.empty(), ret));
    }

    //Error tests

    @Test(expected = IllegalArgumentException.class)
    public void throwOnEmptyKeyStore() {
        //Given - nothing
        //When
        impl.store("", Object.class, new byte[0]);
        //Then
        fail("exception expected");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwOnNullKeyStore() {
        //Given - nothing
        //When
        impl.store(null, Object.class, new byte[0]);
        //Then
        fail("exception expected");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwOnWhitespaceKeyStore() {
        //Given - nothing
        //When
        impl.store("  ", Object.class, new byte[0]);
        //Then
        fail("exception expected");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwOnEmptyKeyRetrieve() {
        //Given - nothing
        //When
        impl.retrieve("");
        //Then
        fail("exception expected");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwOnNullKeyRetrieve() {
        //Given - nothing
        //When
        impl.retrieve(null);
        //Then
        fail("exception expected");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwOnWhitespaceKeyRetrieve() {
        //Given - nothing
        //When
        impl.retrieve("  ");
        //Then
        fail("exception expected");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwOnEmptyKeyList() {
        //Given - nothing
        //When
        impl.list("");
        //Then
        fail("exception expected");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwOnNullKeyList() {
        //Given - nothing
        //When
        impl.list(null);
        //Then
        fail("exception expected");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwOnWhitespaceKeyList() {
        //Given - nothing
        //When
        impl.list("  ");
        //Then
        fail("exception expected");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwOnNegativeDuration() {
        //Given - nothing
        //When
        impl.store("test", Object.class, new byte[0], Optional.of(Duration.of(-10, ChronoUnit.SECONDS)));
        //Then
        fail("exception expected");
    }

    @Test(expected = NullPointerException.class)
    public void throwOnNullClass() {
        //Given - nothing
        //When
        impl.store("test", null, new byte[0]);
        //Then
        fail("exception expected");
    }

    @Test(expected = NullPointerException.class)
    public void throwOnNullValue() {
        //Given - nothing
        //When
        impl.store("test", Object.class, null);
        //Then
        fail("exception expected");
    }

    //Retrieve tests
    @Test
    public void shouldReturnEmptyCacheObject() {
        //Given - nothing
        //When
        BasicCacheObject ob = impl.retrieve("nothing");
        //Then
        assertFalse(ob.getValue().isPresent());
    }

    //Storage tests

    @Test
    public void shouldStoreAndRetrieveObject() {
        //Given
        byte[] expected = new byte[]{1, 2, 3, 4};
        //When
        impl.store("test", Integer.class, expected);
        BasicCacheObject actual = impl.retrieve("test");
        //Then
        assertEquals(expected, actual.getValue().get());
        assertEquals(Integer.class, actual.getValueClass());
    }

    @Test
    public void shouldStoreTwoAndRetrieveTwoObjects() {
        //Given
        byte[] expected = new byte[]{1, 2, 3, 4};
        byte[] expected2 = new byte[]{5, 6, 7, 8};
        //When
        impl.store("test", Integer.class, expected);
        impl.store("test2", String.class, expected2);
        BasicCacheObject actual = impl.retrieve("test");
        BasicCacheObject actual2 = impl.retrieve("test2");

        //Then
        assertEquals(expected, actual.getValue().get());
        assertEquals(Integer.class, actual.getValueClass());
        assertEquals(expected2, actual2.getValue().get());
        assertEquals(String.class, actual2.getValueClass());
    }

    @Test
    public void shouldStoreTwoAndNotRetrieveOther() {
        //Given
        byte[] expected = new byte[]{1, 2, 3, 4};
        byte[] expected2 = new byte[]{5, 6, 7, 8};
        //When
        impl.store("test", Integer.class, expected);
        impl.store("test2", String.class, expected2);
        BasicCacheObject actual = impl.retrieve("not_there");
        //Then
        assertFalse(actual.getValue().isPresent());
        assertEquals(Object.class, actual.getValueClass());
    }

    @Test
    public void shouldStoreAndOverwrite() {
        //Given
        byte[] expected = new byte[]{1, 2, 3, 4};
        byte[] expected2 = new byte[]{5, 6, 7, 8};
        //When
        impl.store("test", Integer.class, expected);
        BasicCacheObject actual = impl.retrieve("test");
        //overwrite it
        impl.store("test", String.class, expected2);
        BasicCacheObject overwrite = impl.retrieve("test");
        //Then
        assertEquals(Integer.class, actual.getValueClass());
        assertEquals(expected, actual.getValue().get());
        //check overwrite
        assertEquals(String.class, overwrite.getValueClass());
        assertEquals(expected2, overwrite.getValue().get());
    }

    //Time to live tests

    private void delay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            //ignore
        }
    }

    @Test
    public void shouldRemoveZeroDurationObject() {
        //Given
        byte[] expected = new byte[]{1, 2, 3, 4};
        impl.store("test", Object.class, expected, Optional.of(Duration.of(0, ChronoUnit.SECONDS)));
        //When
        delay(100);
        BasicCacheObject empty = impl.retrieve("test");
        //Then
        assertFalse(empty.getValue().isPresent());
    }

    @Test
    public void shouldRemoveAfterDelay() {
        //Given
        byte[] expected = new byte[]{1, 2, 3, 4};
        impl.store("test", Object.class, expected, Optional.of(Duration.of(1, ChronoUnit.SECONDS)));
        //When
        BasicCacheObject present = impl.retrieve("test");
        delay(1200);
        BasicCacheObject empty = impl.retrieve("test");
        //Then
        assertTrue(present.getValue().isPresent());
        assertFalse(empty.getValue().isPresent());
    }

    @Test
    public void shouldRemoveSeveralAfterDelays() {
        //Given
        byte[] expected = new byte[]{1, 2, 3, 4};
        byte[] expected2 = new byte[]{5, 6, 7, 8};
        byte[] expected3 = new byte[]{9, 10, 11, 12};
        impl.store("test", Object.class, expected, Optional.of(Duration.of(1, ChronoUnit.SECONDS)));
        impl.store("test2", Object.class, expected2, Optional.of(Duration.of(2, ChronoUnit.SECONDS)));
        impl.store("test3", Object.class, expected3, Optional.empty());
        //When
        BasicCacheObject zeroSeconds1 = impl.retrieve("test");
        BasicCacheObject zeroSeconds2 = impl.retrieve("test2");
        BasicCacheObject zeroSeconds3 = impl.retrieve("test3");
        delay(1200);
        BasicCacheObject oneSecond1 = impl.retrieve("test");
        BasicCacheObject oneSecond2 = impl.retrieve("test2");
        BasicCacheObject oneSecond3 = impl.retrieve("test3");
        delay(1000);
        BasicCacheObject twoSeconds1 = impl.retrieve("test");
        BasicCacheObject twoSeconds2 = impl.retrieve("test2");
        BasicCacheObject twoSeconds3 = impl.retrieve("test3");
        //Then
        assertTrue(zeroSeconds1.getValue().isPresent());
        assertTrue(zeroSeconds2.getValue().isPresent());
        assertTrue(zeroSeconds3.getValue().isPresent());
        //first one should have expired
        assertFalse(oneSecond1.getValue().isPresent());
        assertTrue(oneSecond2.getValue().isPresent());
        assertTrue(oneSecond3.getValue().isPresent());
        //second one should have expired
        assertFalse(twoSeconds1.getValue().isPresent());
        assertFalse(twoSeconds2.getValue().isPresent());
        assertTrue(twoSeconds3.getValue().isPresent());
    }
}
