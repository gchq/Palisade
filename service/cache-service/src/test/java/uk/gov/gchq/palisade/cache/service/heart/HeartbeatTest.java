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

package uk.gov.gchq.palisade.cache.service.heart;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import uk.gov.gchq.palisade.cache.service.CacheService;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.Objects.nonNull;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class HeartbeatTest {

    private Heartbeat heart;
    private CacheService mockCache = Mockito.mock(CacheService.class);

    @Before
    public void setup() {
        heart = new Heartbeat();
    }

    @After
    public void tearDown() {
        if (nonNull(heart)) {
            if (heart.isBeating()) {
                heart.stop();
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwWhenIllegalHeartRate() {
        //Given
        //When
        heart.setHeartRate(HeartUtil.MIN_HEARTBEAT_DURATION.dividedBy(2));
        //Then
        fail("exception expected");
    }

    @Test(expected = IllegalStateException.class)
    public void throwWhenBeatingSetRate() {
        //Given
        heart.cacheService(mockCache);
        heart.serviceClass(StubService.class);
        heart.start();
        //When
        heart.heartRate(Duration.ofSeconds(2));
        //Then
        fail("exception expected");
    }

    @Test(expected = IllegalStateException.class)
    public void throwWhenBeatingSetCache() {
        //Given
        heart.cacheService(mockCache);
        heart.serviceClass(StubService.class);
        heart.start();
        //When
        heart.cacheService(mockCache);
        //Then
        fail("exception expected");
    }

    @Test(expected = IllegalStateException.class)
    public void throwWhenBeatingSetService() {
        //Given
        heart.cacheService(mockCache);
        heart.serviceClass(StubService.class);
        heart.start();
        //When
        heart.serviceClass(StubService.class);
        //Then
        fail("exception expected");
    }

    @Test(expected = IllegalStateException.class)
    public void throwWhenBeatingSetInstance() {
        //Given
        heart.cacheService(mockCache);
        heart.serviceClass(StubService.class);
        heart.start();
        //When
        heart.instanceName("test");
        //Then
        fail("exception expected");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwEmptyInstanceName() {
        //Given
        //When
        heart.instanceName("");
        //Then
        fail("exception expected");
    }

    @Test
    public void shouldBeStoppedOnConstruction() {
        assertFalse(heart.isBeating());
    }

    @Test
    public void shouldBeatAfterStart() {
        //Given
        heart.cacheService(mockCache);
        heart.serviceClass(StubService.class);
        //When
        heart.start();
        //Then
        assertTrue(heart.isBeating());
    }

    @Test
    public void shouldNotBeatAfterStop() {
        //Given
        heart.cacheService(mockCache);
        heart.serviceClass(StubService.class);
        //When
        heart.start();
        heart.stop();
        //Then
        assertFalse(heart.isBeating());
    }

    @Test
    public void shouldBeatAfterRestart() {
        //Given
        heart.cacheService(mockCache);
        heart.serviceClass(StubService.class);
        //When
        heart.start();
        heart.stop();
        heart.start();
        //Then
        assertTrue(heart.isBeating());
    }

    @Test
    public void shouldUseDefaultNameOnNullInstanceName() {
        //Given
        heart.instanceName(null);
        //When
        String actual = heart.getInstanceName();
        //Then
        assertThat(actual, is(equalTo(HeartUtil.createDefaultName())));
    }

    @Test(expected = IllegalStateException.class)
    public void throwOnNoCache() {
        //Given
        heart.serviceClass(StubService.class);
        //When
        heart.start();
        //Then
        fail("exception expected");
    }

    @Test(expected = IllegalStateException.class)
    public void throwOnNoService() {
        //Given
        heart.cacheService(mockCache);
        //When
        heart.start();
        //Then
        fail("exception expected");
    }

    @Test(expected = IllegalStateException.class)
    public void throwOnDoubleStart() {
        //Given
        heart.cacheService(mockCache);
        heart.serviceClass(StubService.class);
        //When
        heart.start();
        heart.start();
        //Then
        fail("exception expected");
    }

    @Test
    public void schedulerShouldTerminateOnGC() throws Exception {
        //Given
        heart.cacheService(mockCache);
        heart.serviceClass(StubService.class);
        //When
        heart.start();
        ScheduledExecutorService scheduler = heart.getExecutor();
        //null out reference
        heart = null;
        //Trigger manual gc
        System.gc();
        long time = System.currentTimeMillis() + 2000;

        //Then - wait a limited amount of time for scheduler to stop
        while (System.currentTimeMillis() < time && !scheduler.isTerminated()) {
            Thread.sleep(50);
        }

        assertTrue("Scheduler failed to terminate after heartbeat was GC'd", scheduler.isTerminated());
    }
}