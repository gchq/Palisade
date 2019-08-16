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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.audit.service.AuditService;
import uk.gov.gchq.palisade.audit.service.impl.LoggerAuditService;
import uk.gov.gchq.palisade.cache.service.CacheService;
import uk.gov.gchq.palisade.cache.service.impl.EtcdBackingStore;
import uk.gov.gchq.palisade.cache.service.impl.SimpleCacheService;
import uk.gov.gchq.palisade.config.service.ConfigurationService;
import uk.gov.gchq.palisade.config.service.impl.ProxyRestConfigService;
import uk.gov.gchq.palisade.data.service.DataService;
import uk.gov.gchq.palisade.data.service.impl.ProxyRestDataService;
import uk.gov.gchq.palisade.policy.service.PolicyService;
import uk.gov.gchq.palisade.resource.service.ResourceService;
import uk.gov.gchq.palisade.resource.service.impl.ProxyRestResourceService;
import uk.gov.gchq.palisade.rest.ProxyRestConnectionDetail;
import uk.gov.gchq.palisade.service.ConnectionDetail;
import uk.gov.gchq.palisade.service.PalisadeService;
import uk.gov.gchq.palisade.service.impl.ProxyRestPalisadeService;
import uk.gov.gchq.palisade.service.impl.ProxyRestPolicyService;
import uk.gov.gchq.palisade.user.service.UserService;
import uk.gov.gchq.palisade.user.service.impl.ProxyRestUserService;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

/**
 * Creates services based on default ports for services for the examples.
 */
public class ProxyServicesFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyServicesFactory.class);

    private final String[] args;

    public ProxyServicesFactory(final String[] args) {
        requireNonNull(args, "args");
        if (!validateArguments(args)) {
            throw new IllegalArgumentException("example arguments invalid. See log.");
        }
        this.args = args;
    }

    /**
     * Used to validate the arguments into the main method
     *
     * @param args arguments to be passed into the main method
     * @return true if the correct number of arguments are present
     */
    private boolean validateArguments(final String[] args) {
        if (args.length > 8) {
            return true;
        } else {
            LOGGER.error("error not enough arguments have been provided. The following arguments are required:\n" +
                    "1. a commma separated list of the etcd endpoints\n" +
                    "2. the internal url for the palisade service\n" +
                    "3. the internal url for the policy service\n" +
                    "4. the internal url for the resource service\n" +
                    "5. the internal url for the user service\n" +
                    "6. the internal url for the data service\n" +
                    "7. the internal url for the config service\n" +
                    "8. the externally accessible url for the palisade service (for clients)\n" +
                    "9. the externally accessible url for the data service (for clients)");
            return false;
        }
    }

    public ConnectionDetail createClientDataServiceConnection() {
        return new ProxyRestConnectionDetail().url(args[8]).serviceClass(ProxyRestDataService.class);
    }

    private CacheService cacheService;

    public CacheService createInternalCacheService() {
        if (isNull(cacheService)) {
            if (args.length > 1) {
                List<URI> etcdEndpoints = Arrays.stream(args[0].split(",")).map(URI::create).collect(Collectors.toList());
                cacheService = new SimpleCacheService().backingStore(new EtcdBackingStore().connectionDetails(etcdEndpoints));
            } else {
                LOGGER.error("Failed to create the Configuration for the cache service due to missing the 1st argument, " +
                        "which should be a comma separated list of etcd client endpoints");
                throw new RuntimeException("failed to create CacheService due to no etcd endpoints specified");
            }
        }
        return cacheService;
    }

    public AuditService createInternalAuditService() {
        return new LoggerAuditService();
    }

    public ConfigurationService createInternalConfigService() {
        return new ProxyRestConfigService(args[6]);
    }

    public UserService createInternalUserService() {
        return new ProxyRestUserService(args[4]);
    }

    public PolicyService createInternalPolicyService() {
        return new ProxyRestPolicyService(args[2]);
    }

    public ResourceService createInternalResourceService() {
        return new ProxyRestResourceService(args[3]);
    }

    public DataService createInternalDataService() {
        return new ProxyRestDataService(args[5]);
    }

    public PalisadeService createInternalPalisadeService() {
        return new ProxyRestPalisadeService(args[1]);
    }

    public PalisadeService createClientPalisadeService() {
        return new ProxyRestPalisadeService(args[7]);
    }
}
