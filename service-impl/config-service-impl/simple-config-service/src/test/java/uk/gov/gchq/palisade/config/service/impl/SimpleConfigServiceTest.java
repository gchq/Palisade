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
package uk.gov.gchq.palisade.config.service.impl;

import org.junit.BeforeClass;
import org.junit.Test;

import uk.gov.gchq.palisade.cache.service.impl.HashMapBackingStore;
import uk.gov.gchq.palisade.cache.service.impl.SimpleCacheService;
import uk.gov.gchq.palisade.cache.service.request.AddCacheRequest;
import uk.gov.gchq.palisade.config.service.InitialConfigurationService;
import uk.gov.gchq.palisade.config.service.exception.NoConfigException;
import uk.gov.gchq.palisade.config.service.request.GetConfigRequest;
import uk.gov.gchq.palisade.service.Service;
import uk.gov.gchq.palisade.service.request.InitialConfig;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SimpleConfigServiceTest {

    private static SimpleConfigService scs;

    private static InitialConfig clientConfig = new InitialConfig()
            .put("test1_client", "value1_client")
            .put("test2_client", "value2_client");

    private static InitialConfig genericService = new InitialConfig()
            .put("test1_generic", "value1_generic")
            .put("test2_generic", "value2_generic");

    private static InitialConfig serviceClass1 = new InitialConfig()
            .put("test1_service1", "value1_service1")
            .put("test2_service1", "value2_service1");

    private static InitialConfig serviceClass2 = new InitialConfig()
            .put("test1_service2", "value1_service2")
            .put("test2_service2", "value2_service2");

    static class Dummy1 implements Service {
    }

    static class Dummy2 implements Service {
    }

    static class NotInCache implements Service {
    }

    @BeforeClass
    public static void createConfig() {
        scs = new SimpleConfigService(
                new SimpleCacheService()
                        .backingStore(
                                new HashMapBackingStore(false)
                        )
        );
        scs.getCache().add(new AddCacheRequest<InitialConfig>()
                .service(InitialConfigurationService.class)
                .key(SimpleConfigService.ANONYMOUS_CONFIG_KEY)
                .value(clientConfig)).join();
        scs.getCache().add(new AddCacheRequest<InitialConfig>()
                .service(InitialConfigurationService.class)
                .key(Dummy1.class.getCanonicalName())
                .value(serviceClass1)).join();
        scs.getCache().add(new AddCacheRequest<InitialConfig>()
                .service(InitialConfigurationService.class)
                .key(Dummy2.class.getCanonicalName())
                .value(serviceClass2)).join();
        scs.getCache().add(new AddCacheRequest<InitialConfig>()
                .service(InitialConfigurationService.class)
                .key(Service.class.getCanonicalName())
                .value(genericService)).join();
    }

    @Test(expected = NoConfigException.class)
    public void throwNoConfig() {
        //Given
        InitialConfigurationService localConfigService = new SimpleConfigService(
                new SimpleCacheService()
                        .backingStore(
                                new HashMapBackingStore(false)
                        )
        );
        GetConfigRequest req = new GetConfigRequest();
        //When
        localConfigService.get(req);
        //Then
        fail("exception expected");
    }

    @Test
    public void shouldRetrieveAnonymousConfig() {
        //Given
        GetConfigRequest req = new GetConfigRequest();
        //When
        InitialConfig actual = scs.get(req).join();
        //Then
        assertEquals(clientConfig, actual);
    }

    @Test
    public void shouldRetrieveServiceConfigForService() {
        //Given
        GetConfigRequest req = new GetConfigRequest().service(Optional.of(Dummy1.class));
        //When
        InitialConfig actual = scs.get(req).join();
        //Then
        assertEquals(serviceClass1, actual);
    }

    @Test
    public void shouldRetrieveServiceConfigForOtherService() {
        //Given
        GetConfigRequest req = new GetConfigRequest().service(Optional.of(Dummy2.class));
        //When
        InitialConfig actual = scs.get(req).join();
        //Then
        assertEquals(serviceClass2, actual);
    }

    @Test
    public void shouldRetrieveGenericServiceConfig() {
        //Given
        GetConfigRequest req = new GetConfigRequest().service(Optional.of(NotInCache.class));
        //When
        InitialConfig actual = scs.get(req).join();
        //Then
        assertEquals(genericService, actual);
    }
}
