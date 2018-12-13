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
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import static uk.gov.gchq.palisade.cache.service.impl.StreamUtil.streamEqual;

public abstract class AbstractBackingStoreTest {

    protected BackingStore impl;

    public abstract BackingStore createBackingStore();

    @Before
    public void initEmptyStore() {
        impl = createBackingStore();
    }

    //List tests

    @Test
    public void shouldReturnEmptyListFromEmptyStore() {
        //Given - nothing
        //When
        Stream<String> ret = impl.list("anything");
        //Then
        assertTrue(streamEqual(Stream.empty(), ret));
    }

    @Test
    public void shouldReturnTwoElementsFromTwoKeys() {
        //Given - two keys
        byte[] b1 = new byte[10];
        byte[] b2 = new byte[10];
        impl.add("test1_key1", Object.class, b1);
        impl.add("test1_key2", Object.class, b2);
        //When
        Stream<String> ret = impl.list("test1");
        //Then
        assertTrue(streamEqual(Stream.of("test1_key1", "test1_key2"), ret));
    }

    @Test
    public void shouldReturnOneElementFromTwoDifferentKeys() {
        //Given - two keys
        byte[] b1 = new byte[10];
        byte[] b2 = new byte[10];
        impl.add("foo_key1", Object.class, b1);
        impl.add("bar_key2", Object.class, b2);
        //When
        Stream<String> ret = impl.list("foo");
        Stream<String> ret2 = impl.list("bar");
        //Then
        assertTrue(streamEqual(Stream.of("foo_key1"), ret));
        assertTrue(streamEqual(Stream.of("bar_key2"), ret2));
    }

    @Test
    public void shouldReturnNothingFromTwoDifferentKeys() {
        //Given - two keys
        byte[] b1 = new byte[10];
        byte[] b2 = new byte[10];
        impl.add("foo_key1", Object.class, b1);
        impl.add("bar_key2", Object.class, b2);
        //When
        Stream<String> ret = impl.list("not_there");
        //Then
        assertTrue(streamEqual(Stream.empty(), ret));
    }

    //Remove tests

    @Test
    public void shouldReturnFalseOnNoKey() {
        //Given - nothing

        //When
        boolean present = impl.remove("not_there");

        //Then
        assertFalse(present);
    }

    @Test
    public void shouldReturnTrueOnKey() {
        //Given
        byte[] b1 = new byte[10];
        impl.add("foo_key1", Object.class, b1);

        //When
        boolean present = impl.remove("foo_key1");

        //Then
        assertTrue(present);
    }

    @Test
    public void shouldRemoveKey() {
        //Given
        byte[] b1 = new byte[10];
        //check nothing there
        SimpleCacheObject result = impl.get("foo_key1");
        assertFalse(result.getValue().isPresent());

        //Add key
        impl.add("foo_key1", Object.class, b1);
        //check present
        result = impl.get("foo_key1");
        assertTrue(result.getValue().isPresent());

        //When
        impl.remove("foo_key1");

        //Then
        //check not there
        result = impl.get("foo_key1");
        assertFalse(result.getValue().isPresent());
    }

    @Test
    public void shouldNotRemoveOther() {
        //Given
        byte[] b1 = new byte[10];
        byte[] b2 = new byte[10];

        impl.add("foo_key1", Object.class, b1);
        impl.add("bar_key2", Object.class, b2);

        //When
        impl.remove("bar_key2");

        //Then
        SimpleCacheObject result = impl.get("foo_key1");
        assertTrue(result.getValue().isPresent());
    }

    //Error tests

    @Test(expected = IllegalArgumentException.class)
    public void throwOnEmptyKeyRemove() {
        //Given - nothing
        //When
        impl.remove("");
        //Then
        fail("exception expected");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwOnNullKeyRemove() {
        //Given - nothing
        //When
        impl.remove(null);
        //Then
        fail("exception expected");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwOnWhitespaceKeyRemove() {
        //Given - nothing
        //When
        impl.remove("  ");
        //Then
        fail("exception expected");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwOnEmptyKeyStore() {
        //Given - nothing
        //When
        impl.add("", Object.class, new byte[0]);
        //Then
        fail("exception expected");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwOnNullKeyStore() {
        //Given - nothing
        //When
        impl.add(null, Object.class, new byte[0]);
        //Then
        fail("exception expected");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwOnWhitespaceKeyStore() {
        //Given - nothing
        //When
        impl.add("  ", Object.class, new byte[0]);
        //Then
        fail("exception expected");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwOnEmptyKeyRetrieve() {
        //Given - nothing
        //When
        impl.get("");
        //Then
        fail("exception expected");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwOnNullKeyRetrieve() {
        //Given - nothing
        //When
        impl.get(null);
        //Then
        fail("exception expected");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwOnWhitespaceKeyRetrieve() {
        //Given - nothing
        //When
        impl.get("  ");
        //Then
        fail("exception expected");
    }

    @Test(expected = NullPointerException.class)
    public void throwOnNullKeyList() {
        //Given - nothing
        //When
        impl.list(null);
        //Then
        fail("exception expected");
    }

    @Test(expected = NullPointerException.class)
    public void throwOnNullDuration() {
        //Given - nothing
        //When
        impl.add("test2", Object.class, new byte[]{1}, null);
        //Then
        fail("exception expected");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwOnNegativeDuration() {
        //Given - nothing
        //When
        impl.add("test3", Object.class, new byte[0], Optional.of(Duration.of(-10, ChronoUnit.SECONDS)));
        //Then
        fail("exception expected");
    }

    @Test(expected = NullPointerException.class)
    public void throwOnNullClass() {
        //Given - nothing
        //When
        impl.add("test4", null, new byte[0]);
        //Then
        fail("exception expected");
    }

    @Test(expected = NullPointerException.class)
    public void throwOnNullValue() {
        //Given - nothing
        //When
        impl.add("test5", Object.class, null);
        //Then
        fail("exception expected");
    }

    //Retrieve tests
    @Test
    public void shouldReturnEmptyCacheObject() {
        //Given - nothing
        //When
        SimpleCacheObject ob = impl.get("nothing");
        //Then
        assertFalse(ob.getValue().isPresent());
    }

    //Storage tests

    @Test
    public void shouldStoreAndRetrieveObject() {
        //Given
        byte[] expected = new byte[]{1, 2, 3, 4};
        //When
        impl.add("test6", Integer.class, expected);
        SimpleCacheObject actual = impl.get("test6");
        //Then
        assertArrayEquals(expected, actual.getValue().get());
        assertEquals(Integer.class, actual.getValueClass());
    }

    @Test
    public void shouldStoreTwoAndRetrieveTwoObjects() {
        //Given
        byte[] expected = new byte[]{1, 2, 3, 4};
        byte[] expected2 = new byte[]{5, 6, 7, 8};
        //When
        impl.add("test7", Integer.class, expected);
        impl.add("test8", String.class, expected2);
        SimpleCacheObject actual = impl.get("test7");
        SimpleCacheObject actual2 = impl.get("test8");

        //Then
        assertArrayEquals(expected, actual.getValue().get());
        assertEquals(Integer.class, actual.getValueClass());
        assertArrayEquals(expected2, actual2.getValue().get());
        assertEquals(String.class, actual2.getValueClass());
    }

    @Test
    public void shouldStoreTwoAndNotRetrieveOther() {
        //Given
        byte[] expected = new byte[]{1, 2, 3, 4};
        byte[] expected2 = new byte[]{5, 6, 7, 8};
        //When
        impl.add("new_test", Integer.class, expected);
        impl.add("new_test1", String.class, expected2);
        SimpleCacheObject actual = impl.get("not_there");
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
        impl.add("new_test2", Integer.class, expected);
        SimpleCacheObject actual = impl.get("new_test2");
        //overwrite it
        impl.add("new_test2", String.class, expected2);
        SimpleCacheObject overwrite = impl.get("new_test2");
        //Then
        assertEquals(Integer.class, actual.getValueClass());
        assertArrayEquals(expected, actual.getValue().get());
        //check overwrite
        assertEquals(String.class, overwrite.getValueClass());
        assertArrayEquals(expected2, overwrite.getValue().get());
    }

    //Time to live tests

    protected void delay(long millis) {
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
        impl.add("new_test3", Object.class, expected, Optional.of(Duration.of(0, ChronoUnit.SECONDS)));
        //When
        delay(100);
        SimpleCacheObject empty = impl.get("new_test3");
        //Then
        assertFalse(empty.getValue().isPresent());
    }

    static class TestResult {
        boolean completedInTime;
        SimpleCacheObject result;

        public TestResult(final boolean completedInTime, final SimpleCacheObject result) {
            this.completedInTime = completedInTime;
            this.result = result;
        }
    }

    /**
     * Method to check if the retrieval happened within a certain time.
     */
    protected TestResult timedRetrieve(String key, long safeDelay) {
        long time = System.currentTimeMillis();
        SimpleCacheObject result = impl.get(key);
        time = System.currentTimeMillis() - time;
        return new TestResult((time > safeDelay), result);
    }

    @Test
    public void shouldRemoveAfterDelay() {
        //Given
        byte[] expected = new byte[]{1, 2, 3, 4};
        impl.add("new_test4", Object.class, expected, Optional.of(Duration.of(1, ChronoUnit.SECONDS)));
        //When
        //we avoid this first test to protect against the corner case where the backing add takes longer than the delay
        //respond and ages off the add before we check it the first time which would cause the test case to fail
        TestResult present = timedRetrieve("new_test4", 1000);
        if (present.completedInTime) {
            assertTrue(present.result.getValue().isPresent());
        }
        //Then
        delay(1200);
        SimpleCacheObject empty = impl.get("new_test4");
        assertFalse(empty.getValue().isPresent());
    }

    @Test
    public void shouldRemoveSeveralAfterDelays() {
        //Given
        byte[] expected = new byte[]{1, 2, 3, 4};
        byte[] expected2 = new byte[]{5, 6, 7, 8};
        byte[] expected3 = new byte[]{9, 10, 11, 12};
        impl.add("new_test5", Object.class, expected, Optional.of(Duration.of(1, ChronoUnit.SECONDS)));
        impl.add("new_test6", Object.class, expected2, Optional.of(Duration.of(2, ChronoUnit.SECONDS)));
        impl.add("new_test7", Object.class, expected3, Optional.empty());
        //When
        TestResult zeroSeconds1 = timedRetrieve("new_test5", 333);
        TestResult zeroSeconds2 = timedRetrieve("new_test6", 333);
        TestResult zeroSeconds3 = timedRetrieve("new_test7", 333);
        if (zeroSeconds1.completedInTime && zeroSeconds2.completedInTime && zeroSeconds3.completedInTime) {
            assertTrue(zeroSeconds1.result.getValue().isPresent());
            assertTrue(zeroSeconds2.result.getValue().isPresent());
            assertTrue(zeroSeconds3.result.getValue().isPresent());
        }
        delay(1200);
        TestResult oneSecond1 = timedRetrieve("new_test5", 333);
        TestResult oneSecond2 = timedRetrieve("new_test6", 333);
        TestResult oneSecond3 = timedRetrieve("new_test7", 333);
        if (oneSecond1.completedInTime && oneSecond2.completedInTime && oneSecond3.completedInTime) {
            //first one should have expired
            assertFalse(oneSecond1.result.getValue().isPresent());
            assertTrue(oneSecond2.result.getValue().isPresent());
            assertTrue(oneSecond3.result.getValue().isPresent());
        }
        delay(1000);
        TestResult twoSeconds1 = timedRetrieve("new_test5", 1000);
        TestResult twoSeconds2 = timedRetrieve("new_test6", 1000);
        TestResult twoSeconds3 = timedRetrieve("new_test7", 1000);
        //Then
        //second one should have expired
        assertFalse(twoSeconds1.result.getValue().isPresent());
        assertFalse(twoSeconds2.result.getValue().isPresent());
        assertTrue(twoSeconds3.result.getValue().isPresent());
    }
}
