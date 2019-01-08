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
import uk.gov.gchq.palisade.config.service.ConfigurationService;
import uk.gov.gchq.palisade.exception.NoConfigException;
import uk.gov.gchq.palisade.config.service.request.AddConfigRequest;
import uk.gov.gchq.palisade.config.service.request.GetConfigRequest;
import uk.gov.gchq.palisade.service.Service;
import uk.gov.gchq.palisade.service.request.ServiceConfiguration;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SimpleConfigServiceTest {

    private static SimpleConfigService scs;

    private static ServiceConfiguration clientConfig = new ServiceConfiguration()
            .put("test1_client", "value1_client")
            .put("test2_client", "value2_client");

    private static ServiceConfiguration genericService = new ServiceConfiguration()
            .put("test1_generic", "value1_generic")
            .put("test2_generic", "value2_generic");

    private static ServiceConfiguration serviceClass1 = new ServiceConfiguration()
            .put("test1_service1", "value1_service1")
            .put("test2_service1", "value2_service1");

    private static ServiceConfiguration serviceClass2 = new ServiceConfiguration()
            .put("test1_service2", "value1_service2")
            .put("test2_service2", "value2_service2");

    static class Dummy1 implements Service {
    }

    static class Dummy2 implements Service {
    }

    static class NotInCache implements Service {
    }

    static class CacheAddTest implements Service {
    }

    @BeforeClass
    public static void createConfig() {
        scs = new SimpleConfigService(
                new SimpleCacheService()
                        .backingStore(
                                new HashMapBackingStore(false)
                        )
        );
        scs.getCache().add(new AddCacheRequest<ServiceConfiguration>()
                .service(ConfigurationService.class)
                .key(SimpleConfigService.ANONYMOUS_CONFIG_KEY)
                .value(clientConfig)).join();
        scs.getCache().add(new AddCacheRequest<ServiceConfiguration>()
                .service(ConfigurationService.class)
                .key(Dummy1.class.getTypeName())
                .value(serviceClass1)).join();
        scs.getCache().add(new AddCacheRequest<ServiceConfiguration>()
                .service(ConfigurationService.class)
                .key(Dummy2.class.getTypeName())
                .value(serviceClass2)).join();
        scs.getCache().add(new AddCacheRequest<ServiceConfiguration>()
                .service(ConfigurationService.class)
                .key(Service.class.getTypeName())
                .value(genericService)).join();
    }

    @Test(expected = NoConfigException.class)
    public void throwNoConfig() {
        //Given
        ConfigurationService localConfigService = new SimpleConfigService(
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
        ServiceConfiguration actual = scs.get(req).join();
        //Then
        assertEquals(clientConfig, actual);
    }

    @Test
    public void shouldRetrieveServiceConfigForService() {
        //Given
        GetConfigRequest req = new GetConfigRequest().service(Optional.of(Dummy1.class));
        //When
        ServiceConfiguration actual = scs.get(req).join();
        //Then
        assertEquals(serviceClass1, actual);
    }

    @Test
    public void shouldRetrieveServiceConfigForOtherService() {
        //Given
        GetConfigRequest req = new GetConfigRequest().service(Optional.of(Dummy2.class));
        //When
        ServiceConfiguration actual = scs.get(req).join();
        //Then
        assertEquals(serviceClass2, actual);
    }

    @Test
    public void shouldRetrieveGenericServiceConfig() {
        //Given
        GetConfigRequest req = new GetConfigRequest().service(Optional.of(NotInCache.class));
        //When
        ServiceConfiguration actual = scs.get(req).join();
        //Then
        assertEquals(genericService, actual);
    }

    @Test
    public void shouldPutAndRetrieveConfig() {
        //Given
        ServiceConfiguration expected = serviceClass2;
        AddConfigRequest request = (AddConfigRequest) new AddConfigRequest()
                .config(expected)
                .service(Optional.of(CacheAddTest.class));
        scs.add(request);
        //When
        GetConfigRequest req = new GetConfigRequest().service(Optional.of(CacheAddTest.class));
        ServiceConfiguration actual = scs.get(req).join();
        //Then
        assertEquals(expected, actual);
    }
}
