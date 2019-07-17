/*
 * Copyright 2019 Crown Copyright
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

import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class K8sBackingStoreTest extends AbstractBackingStoreTest {

    private K8sBackingStore k8sBackingStore;

    @Rule
    public KubernetesServer server = new KubernetesServer(true, true);

    @Override
    public K8sBackingStore createBackingStore() {
        k8sBackingStore = new K8sBackingStore();
        return k8sBackingStore;
    }


    @After
    public void closeClients() {
        k8sBackingStore.close();
    }

    /**
     * These tests have been modified due to k8s not strictly sticking to the lease TTL times
     * I found that while the items are removed, it could be a couple of seconds after the TTL
     */

    @Override
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
        delay(2500);
        SimpleCacheObject empty = impl.get("new_test4");
        assertFalse(empty.getValue().isPresent());
    }

    @Override
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
        delay(1900);
        TestResult oneSecond1 = timedRetrieve("new_test5", 333);
        TestResult oneSecond2 = timedRetrieve("new_test6", 333);
        TestResult oneSecond3 = timedRetrieve("new_test7", 333);
        if (oneSecond1.completedInTime && oneSecond2.completedInTime && oneSecond3.completedInTime) {
            //first one should have expired
            assertFalse(oneSecond1.result.getValue().isPresent());
            assertTrue(oneSecond2.result.getValue().isPresent());
            assertTrue(oneSecond3.result.getValue().isPresent());
        }
        delay(2000);
        TestResult twoSeconds1 = timedRetrieve("new_test5", 1000);
        TestResult twoSeconds2 = timedRetrieve("new_test6", 1000);
        TestResult twoSeconds3 = timedRetrieve("new_test7", 1000);
        //Then
        //second one should have expired
        assertFalse(twoSeconds1.result.getValue().isPresent());
        assertFalse(twoSeconds2.result.getValue().isPresent());
        assertTrue(twoSeconds3.result.getValue().isPresent());
    }


    @Override
    public void shouldRemoveKey() {
        //Given
        byte[] b1 = new byte[10];
        //check nothing there
        SimpleCacheObject result = impl.get("remove_test1");
        assertFalse(result.getValue().isPresent());

        //Add key
        impl.add("remove_test1", Object.class, b1);
        //check present
        result = impl.get("remove_test1");
        assertTrue(result.getValue().isPresent());

        //When
        impl.remove("remove_test1");

        delay(2000);

        //Then
        //check not there
        result = impl.get("remove_test1");

        assertFalse(result.getValue().isPresent());
    }


    @Override
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

    @Test
    public void validateConvertKeyToCompatible() {
        //Given

        K8sBackingStore k8sBackingStore = (K8sBackingStore) impl;
        String checkVal1 = k8sBackingStore.convertKeyToCompatible("new_test2");
        String checkVal2 = k8sBackingStore.convertKeyToCompatible("new-test2");
        String checkVal3 = k8sBackingStore.convertKeyToCompatible("new.test2");
        String checkVal4 = k8sBackingStore.convertKeyToCompatible("neWtest2");
        //When

        //Then
        assertEquals(checkVal1, "newtest2");
        assertEquals(checkVal2, "new-test2");
        assertEquals(checkVal3, "newtest2");
        assertEquals(checkVal4, "newtest2");
    }

    @Test
    public void shouldNotRemoveTwice() {
        //Given
        byte[] b1 = new byte[10];

        impl.add("remove_test4", Object.class, b1);

        //When
        boolean removePresent = impl.remove("remove_test4");
        delay(1000); //delay while key is removed
        boolean removeEmpty = impl.remove("remove_test4");

        //Then
        assertTrue(removePresent);
        assertFalse(removeEmpty);
    }


}
