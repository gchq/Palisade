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

import uk.gov.gchq.palisade.cache.service.CacheService;
import uk.gov.gchq.palisade.cache.service.heart.Heartbeat;
import uk.gov.gchq.palisade.cache.service.heart.Stethoscope;

import java.time.Duration;
import java.util.stream.Stream;

import static org.junit.Assert.assertTrue;
import static uk.gov.gchq.palisade.util.TestUtil.streamEqual;

public class HeartbeatIT {

    private CacheService cache;

    @Before
    public void resetCache() {
        cache = new SimpleCacheService().backingStore(new HeartbeatTestBackingStore());
    }

    @Test
    public void shouldStartAndStopCorrectly() throws Exception {
        //Given
        Heartbeat heart = new Heartbeat()
                .heartRate(Duration.ofSeconds(1))
                .cacheService(cache)
                .serviceClass(MockCacheService.class)
                .instanceName("test");
        Stethoscope scope = Stethoscope.thatListensTo(heart);

        Heartbeat heart2 = new Heartbeat()
                .heartRate(Duration.ofSeconds(1))
                .cacheService(cache)
                .serviceClass(MockCacheService.class)
                .instanceName("test2");

        //When
        assertTrue(streamEqual(scope.auscultate(), Stream.empty()));
        heart.start();
        Thread.sleep(100);

        assertTrue(streamEqual(scope.auscultate(), Stream.of("test")));
        heart2.start();
        Thread.sleep(100);
        assertTrue(streamEqual(scope.auscultate(), Stream.of("test","test2")));

        //Then
        heart.stop();
        //wait for heart beat to expire in cache
        Thread.sleep(2100);
        assertTrue(streamEqual(scope.auscultate(), Stream.of("test2")));
        heart2.stop();
    }

    @Test
    public void shouldStartTwoAndBeVisible() throws Exception {
        //Given
        Heartbeat heart = new Heartbeat()
                .heartRate(Duration.ofSeconds(1))
                .cacheService(cache)
                .serviceClass(MockCacheService.class)
                .instanceName("test");

        Heartbeat heart2 = new Heartbeat()
                .heartRate(Duration.ofSeconds(1))
                .cacheService(cache)
                .serviceClass(MockCacheService.class)
                .instanceName("test2");
        Stethoscope scope = Stethoscope.thatListensTo(heart);

        //When
        heart.start();
        Thread.sleep(100);
        //start second heart
        assertTrue(streamEqual(scope.auscultate(), Stream.of("test")));
        heart2.start();
        Thread.sleep(100);
        //Then
        assertTrue(streamEqual(scope.auscultate(), Stream.of("test", "test2")));
    }

    @Test
    public void shouldStartTwoAndHaveNoOverlap() throws Exception {
        //Given
        Heartbeat heart = new Heartbeat()
                .heartRate(Duration.ofSeconds(1))
                .cacheService(cache)
                .serviceClass(MockCacheService.class)
                .instanceName("test");

        Heartbeat heart2 = new Heartbeat()
                .heartRate(Duration.ofSeconds(1))
                .cacheService(cache)
                .serviceClass(CacheService.class)
                .instanceName("test2");

        Stethoscope scope = Stethoscope.thatListensTo(heart);
        Stethoscope scope2 = Stethoscope.thatListensTo(heart).serviceClass(CacheService.class);

        //When
        heart.start();
        heart2.start();
        Thread.sleep(100);
        assertTrue(streamEqual(scope.auscultate(), Stream.of("test")));
        //Then
        assertTrue(streamEqual(scope2.auscultate(), Stream.of("test2")));
    }
}