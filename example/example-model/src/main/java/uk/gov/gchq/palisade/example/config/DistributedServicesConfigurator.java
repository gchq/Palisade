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

import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.cache.service.CacheService;
import uk.gov.gchq.palisade.cache.service.impl.EtcdBackingStore;
import uk.gov.gchq.palisade.cache.service.impl.SimpleCacheService;
import uk.gov.gchq.palisade.config.service.ConfigurationService;
import uk.gov.gchq.palisade.config.service.impl.ProxyRestConfigService;
import uk.gov.gchq.palisade.data.service.DataService;
import uk.gov.gchq.palisade.data.service.impl.ProxyRestDataService;
import uk.gov.gchq.palisade.policy.service.PolicyService;
import uk.gov.gchq.palisade.resource.service.ResourceService;
import uk.gov.gchq.palisade.resource.service.impl.HadoopResourceService;
import uk.gov.gchq.palisade.resource.service.impl.ProxyRestResourceService;
import uk.gov.gchq.palisade.rest.ProxyRestConnectionDetail;
import uk.gov.gchq.palisade.service.PalisadeService;
import uk.gov.gchq.palisade.service.impl.ProxyRestPalisadeService;
import uk.gov.gchq.palisade.service.impl.ProxyRestPolicyService;
import uk.gov.gchq.palisade.service.request.ConnectionDetail;
import uk.gov.gchq.palisade.user.service.UserService;
import uk.gov.gchq.palisade.user.service.impl.ProxyRestUserService;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Convenience class for setting the default config for the various Palisade micro-services
 * which assumes all services are being deployed locally using the standard port.
 *
 * It is expected to be run after the config service has been started but before
 * the other services are started.
 */
public final class DistributedServicesConfigurator extends ServicesConfigurator {
    protected static final Logger LOGGER = LoggerFactory.getLogger(DistributedServicesConfigurator.class);

    public static void main(final String[] args) {
        new DistributedServicesConfigurator(args);
    }

    public DistributedServicesConfigurator(final String[] args) {
        super(args);
    }

    /**
     * Used to validate the arguments into the main method
     *
     * @param args arguments to be passed into the main method
     * @return true if the correct number of arguments are present
     */
    @Override
    protected boolean validateArguments(final String[] args) {
        if (args.length > 6) {
            return true;
        } else {
            LOGGER.error("error");
            return false;
        }

    }

    @Override
    protected CacheService createCacheService(final String[] args) {
        if (args.length > 1) {
            List<String> etcdEndpoints = Arrays.asList(args[0].split(","));
            return new SimpleCacheService().backingStore(new EtcdBackingStore().connectionDetails(etcdEndpoints));
        }
        else {
            LOGGER.error("Failed to create the Configuration for the cache service due to missing the 1st argument, " +
                    "which should be a comma separated list of etcd client endpoints");
            return null;
        }
    }

    /**
     * A method for creating the Config service that a client (any of the other
     * types of services) would use to connect to the Config service
     *
     * @param args  Provides a means to pass in arguments into the method
     * @return a Configuration service that the client would use to talk to the config service server
     */
    @Override
    protected ConfigurationService createConfigServiceForClients(final String[] args) {
        return new ProxyRestConfigService(args[6]);
    }

    /**
     * A method for creating the user service that a client (any of the other
     * types of services) would use to connect to the user service
     *
     * @param args  Provides a means to pass in arguments into the method
     * @return a user service that the client would use to talk to the user service server
     */
    @Override
    protected UserService createUserServiceForClients(final String[] args) {
        return new ProxyRestUserService(args[4]);
    }

    /**
     * A method for creating a resource service as it would be configured as a standalone micro-service (server)
     *
     * @param args  Provides a means to pass in arguments into the method
     * @return a resource service as it would be configured as a standalone micro-service (server)
     */
    @Override
    protected ResourceService createResourceServiceForServer(final String[] args) {
        try {
            Configuration conf = createHadoopConfiguration();
            HadoopResourceService resource = new HadoopResourceService().conf(conf).cacheService(createCacheService(args));
            final Map<String, ConnectionDetail> dataType = new HashMap<>();
            dataType.put(RESOURCE_TYPE, new ProxyRestConnectionDetail().url(args[5]).serviceClass(ProxyRestDataService.class));
            resource.connectionDetail(null, dataType);
            return resource;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * A method for creating the resource service that a client (any of the other
     * types of services) would use to connect to the resource service
     *
     * @param args  Provides a means to pass in arguments into the method
     * @return a resource service that the client would use to talk to the resource service server
     */
    @Override
    protected ResourceService createResourceServiceForClients(final String[] args) {
        return new ProxyRestResourceService(args[3]);
    }

    /**
     * A method for creating the policy service that a client (any of the other
     * types of services) would use to connect to the policy service
     *
     * @param args  Provides a means to pass in arguments into the method
     * @return a policy service that the client would use to talk to the policy service server
     */
    @Override
    protected PolicyService createPolicyServiceForClients(final String[] args) {
        return new ProxyRestPolicyService(args[2]);
    }

    /**
     * A method for creating the palisade service that a client (any of the other
     * types of services) would use to connect to the palisade service
     *
     * @param args  Provides a means to pass in arguments into the method
     * @return a palisade service that the client would use to talk to the palisade service server
     */
    @Override
    protected PalisadeService createPalisadeServiceForClients(final String[] args) {
        return new ProxyRestPalisadeService(args[1]);
    }

    /**
     * A method for creating the data service that a client (any of the other
     * types of services) would use to connect to the data service
     *
     * @param args  Provides a means to pass in arguments into the method
     * @return a data service that the client would use to talk to the data service server
     */
    @Override
    protected DataService createDataServiceForClients(final String[] args) {
        return new ProxyRestDataService(args[5]);
    }
}
