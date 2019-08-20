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

package uk.gov.gchq.palisade.example.config;

import uk.gov.gchq.palisade.cache.service.CacheService;
import uk.gov.gchq.palisade.cache.service.impl.SimpleCacheService;

/**
 * Convenience class for setting the default config for the various Palisade micro-services
 * which assumes all services are being deployed according to the given arguments.
 * <p>
 * It is expected to be run after the config service has been started but before
 * the other services are started.
 */
public final class DistributedServices {
    private DistributedServices() {
    }

    public static void main(final String[] args) {
        ProxyServicesFactory factory = new ProxyServicesFactory(args);
        new ServicesConfigurator(factory);

        CacheService cs = factory.createInternalCacheService();
        if (cs instanceof SimpleCacheService) {
            ((SimpleCacheService) cs).getBackingStore().close();
        }
    }
}
