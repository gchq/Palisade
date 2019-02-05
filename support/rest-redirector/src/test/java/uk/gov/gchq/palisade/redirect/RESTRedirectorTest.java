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

package uk.gov.gchq.palisade.redirect;

import uk.gov.gchq.palisade.cache.service.heart.Heartbeat;
import uk.gov.gchq.palisade.cache.service.impl.HashMapBackingStore;
import uk.gov.gchq.palisade.cache.service.impl.SimpleCacheService;
import uk.gov.gchq.palisade.config.service.ConfigurationService;
import uk.gov.gchq.palisade.config.service.impl.ProxyRestConfigService;
import uk.gov.gchq.palisade.config.service.impl.RestConfigServiceV1;
import uk.gov.gchq.palisade.config.service.request.AddConfigRequest;
import uk.gov.gchq.palisade.redirect.impl.SimpleRandomRedirector;
import uk.gov.gchq.palisade.rest.EmbeddedHttpServer;
import uk.gov.gchq.palisade.service.request.ServiceConfiguration;

import java.util.Optional;

public class RESTRedirectorTest {

    private static ProxyRestConfigService proxy;
    private static EmbeddedHttpServer server;

    public static void main(String... args) throws Exception {
        try {
            SimpleCacheService scs = new SimpleCacheService().backingStore(new HashMapBackingStore(true));
            Redirector<String> urlRedirector = new SimpleRandomRedirector().cacheService(scs).redirectionClass(ConfigurationService.class);
            //start a fake config service heartbeat
            Heartbeat beat = new Heartbeat().cacheService(scs).serviceClass(ConfigurationService.class).instanceName("test-instance");
            beat.start();

            proxy = (ProxyRestConfigService) new ProxyRestConfigService("http://localhost:8080/palisade").retryMax(1);
            server = new EmbeddedHttpServer(proxy.getBaseUrlWithVersion(), new RESTRedirector(RestConfigServiceV1.class, ConfigurationService.class, urlRedirector));
            server.startServer();
            proxy.add((AddConfigRequest) new AddConfigRequest().config(new ServiceConfiguration().put("test1", "test2")).service(Optional.empty())).join();
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            server.stopServer();
        }
    }
}
