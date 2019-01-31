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

import uk.gov.gchq.palisade.audit.service.AuditService;
import uk.gov.gchq.palisade.audit.service.impl.LoggerAuditService;
import uk.gov.gchq.palisade.cache.service.CacheService;
import uk.gov.gchq.palisade.cache.service.impl.EtcdBackingStore;
import uk.gov.gchq.palisade.cache.service.impl.SimpleCacheService;
import uk.gov.gchq.palisade.config.service.ConfigurationService;
import uk.gov.gchq.palisade.config.service.impl.ProxyRestConfigService;
import uk.gov.gchq.palisade.config.service.request.AddConfigRequest;
import uk.gov.gchq.palisade.data.service.DataService;
import uk.gov.gchq.palisade.data.service.impl.ProxyRestDataService;
import uk.gov.gchq.palisade.data.service.impl.SimpleDataService;
import uk.gov.gchq.palisade.data.service.impl.reader.HadoopDataReader;
import uk.gov.gchq.palisade.example.data.serialiser.ExampleObjSerialiser;
import uk.gov.gchq.palisade.policy.service.PolicyService;
import uk.gov.gchq.palisade.policy.service.impl.HierarchicalPolicyService;
import uk.gov.gchq.palisade.resource.service.ResourceService;
import uk.gov.gchq.palisade.resource.service.impl.HadoopResourceService;
import uk.gov.gchq.palisade.resource.service.impl.ProxyRestResourceService;
import uk.gov.gchq.palisade.rest.ProxyRestConnectionDetail;
import uk.gov.gchq.palisade.service.PalisadeService;
import uk.gov.gchq.palisade.service.Service;
import uk.gov.gchq.palisade.service.impl.ProxyRestPalisadeService;
import uk.gov.gchq.palisade.service.impl.ProxyRestPolicyService;
import uk.gov.gchq.palisade.service.impl.SimplePalisadeService;
import uk.gov.gchq.palisade.service.request.ConnectionDetail;
import uk.gov.gchq.palisade.service.request.ServiceConfiguration;
import uk.gov.gchq.palisade.user.service.UserService;
import uk.gov.gchq.palisade.user.service.impl.ProxyRestUserService;
import uk.gov.gchq.palisade.user.service.impl.SimpleUserService;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;

/**
 * Convenience class for setting the default config for the various Palisade micro-services
 * which assumes all services are being deployed locally using the standard port.
 *
 * It is expected to be run after the config service has been started but before
 * the other services are started.
 */
public class ServicesConfigurator {
    protected static final Logger LOGGER = LoggerFactory.getLogger(ServicesConfigurator.class);
    protected static final String HADOOP_CONF_PATH = "HADOOP_CONF_PATH";
    public static final String RESOURCE_TYPE = "exampleObj";

    /**
     * This is the main method which will run through all the services and set the
     * config for each of those services in the config service ready to bootstrap
     * the micro services when they are started.
     *
     * @param args Provides a means to pass in arguments into the method
     */
    public static void main(final String[] args) {
        new ServicesConfigurator(args);
    }

    public ServicesConfigurator(final String[] args) {
        if (validateArguments(args)) {
            LOGGER.info("Starting to set the service configurations.");

            // create the client config service
            ConfigurationService configClient = createConfigServiceForClients(args);

            // create the other client service
            UserService userClient = createUserServiceForClients(args);
            ResourceService resourceClient = createResourceServiceForClients(args);
            PolicyService policyClient = createPolicyServiceForClients(args);
            PalisadeService palisadeClient = createPalisadeServiceForClients(args);
            DataService dataClient = createDataServiceForClients(args);

            // add the config for the clients to the config service
            Collection<Service> services = Stream.of(configClient, userClient, resourceClient, policyClient, palisadeClient, dataClient).collect(Collectors.toList());
            writeClientConfiguration(configClient, services);

            // write the config for the user service to the config service
            writeServerConfiguration(configClient, createUserServiceForServer(args), UserService.class);

            // write the config for the resource service to the config service
            writeServerConfiguration(configClient, createResourceServiceForServer(args), ResourceService.class);

            // write the config for the policy service to the config service
            writeServerConfiguration(configClient, createPolicyServiceForServer(args), PolicyService.class);

            // write the config for the palisade service to the config service
            writeServerConfiguration(configClient, createPalisadeServiceForServer(args), PalisadeService.class);

            // write the config for the data service to the config service
            writeServerConfiguration(configClient, createDataServiceForServer(args), DataService.class);

            LOGGER.info("Finished setting the service configurations.");
        }
    }

    /**
     * Used to validate the arguments into the main method
     *
     * @param args arguments to be passed into the main method
     * @return true if the correct number of arguments are present
     */
    protected boolean validateArguments(final String[] args) {
        return true;
    }

    /**
     * This will write each of the client service configs into the config service ready
     * for any of the services to get the details of how to contact those different services as required.
     *
     * @param configService the configuration service
     * @param services      collection of services to write to the configuration service
     */
    private void writeClientConfiguration(final ConfigurationService configService, final Collection<Service> services) {
        ServiceConfiguration initial = new ServiceConfiguration();

        //each service to write their configuration into the initial configuration
        services.forEach(service -> service.recordCurrentConfigTo(initial));

        //insert this into the cache manually so it can be created later
        configService.add((AddConfigRequest) new AddConfigRequest()
                .config(initial)
                .service(Optional.empty())).join();
    }

    /**
     * This will write the provided services configuration into the config service
     * ready to bootstrap that service when it is started up as a micro-service
     *
     * @param configService the configuration service to write to
     * @param service       the service that will write its configuration
     * @param serviceClass  the type of Palisade service
     */
    private void writeServerConfiguration(final ConfigurationService configService, final Service service, final Class<? extends Service> serviceClass) {
        ServiceConfiguration config = new ServiceConfiguration();
        service.recordCurrentConfigTo(config);
        configService.add((AddConfigRequest) new AddConfigRequest()
                .config(config)
                .service(Optional.of(serviceClass))).join();
    }

    protected CacheService createCacheService(final String[] args) {
        return new SimpleCacheService().backingStore(new EtcdBackingStore().connectionDetails(Collections.singletonList(URI.create("http://localhost:2379"))));
    }

    protected AuditService createAuditService(final String[] args) {
        return new LoggerAuditService();
    }

    /**
     * A method for creating the Config service that a client (any of the other
     * types of services) would use to connect to the Config service
     *
     * @param args  Provides a means to pass in arguments into the method
     * @return a Configuration service that the client would use to talk to the config service server
     */
    protected ConfigurationService createConfigServiceForClients(final String[] args) {
        return new ProxyRestConfigService("http://localhost:8085/config");
    }

    /**
     * A method for creating a user service as it would be configured as a standalone micro-service (server)
     *
     * @param args  Provides a means to pass in arguments into the method
     * @return a user service as it would be configured as a standalone micro-service (server)
     */
    protected UserService createUserServiceForServer(final String[] args) {
        return new SimpleUserService().cacheService(createCacheService(args));
    }

    /**
     * A method for creating the user service that a client (any of the other
     * types of services) would use to connect to the user service
     *
     * @param args  Provides a means to pass in arguments into the method
     * @return a user service that the client would use to talk to the user service server
     */
    protected UserService createUserServiceForClients(final String[] args) {
        return new ProxyRestUserService("http://localhost:8083/user");
    }

    /**
     * A method for creating a resource service as it would be configured as a standalone micro-service (server)
     *
     * @param args  Provides a means to pass in arguments into the method
     * @return a resource service as it would be configured as a standalone micro-service (server)
     */
    protected ResourceService createResourceServiceForServer(final String[] args) {
        try {
            Configuration conf = createHadoopConfiguration();
            HadoopResourceService resource = new HadoopResourceService().conf(conf).cacheService(createCacheService(args));
            final Map<String, ConnectionDetail> dataType = new HashMap<>();
            dataType.put(RESOURCE_TYPE, new ProxyRestConnectionDetail().url("http://localhost:8084/data").serviceClass(ProxyRestDataService.class));
            resource.connectionDetail(null, dataType);
            return resource;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a Hadoop configuration object. This loads the default Hadoop configuration and then checks the environment
     * variable {@link ServicesConfigurator#HADOOP_CONF_PATH} for a list of paths. Each one in the environment variable is
     * loaded into the configuration.
     *
     * @return a Hadoop configuration object
     */
    protected Configuration createHadoopConfiguration() {
        Configuration ret = new Configuration();
        String extraFiles = System.getenv(HADOOP_CONF_PATH);
        if (nonNull(extraFiles)) {
            String[] parts = extraFiles.split(File.pathSeparator);
            for (String part : parts) {
                try {
                    if (Files.exists(Paths.get(part))) {
                        LOGGER.debug("Loading extra configuration from {}", part);
                        ret.addResource(new File(part).toURI().toURL());
                    } else {
                        LOGGER.warn("No such file {}", part);
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }
        return ret;
    }

    /**
     * A method for creating the resource service that a client (any of the other
     * types of services) would use to connect to the resource service
     *
     * @param args  Provides a means to pass in arguments into the method
     * @return a resource service that the client would use to talk to the resource service server
     */
    protected ResourceService createResourceServiceForClients(final String[] args) {
        return new ProxyRestResourceService("http://localhost:8082/resource");
    }

    /**
     * A method for creating a policy service as it would be configured as a standalone micro-service (server)
     *
     * @param args  Provides a means to pass in arguments into the method
     * @return a policy service as it would be configured as a standalone micro-service (server)
     */
    protected PolicyService createPolicyServiceForServer(final String[] args) {
        return new HierarchicalPolicyService().cacheService(createCacheService(args));
    }

    /**
     * A method for creating the policy service that a client (any of the other
     * types of services) would use to connect to the policy service
     *
     * @param args  Provides a means to pass in arguments into the method
     * @return a policy service that the client would use to talk to the policy service server
     */
    protected PolicyService createPolicyServiceForClients(final String[] args) {
        return new ProxyRestPolicyService("http://localhost:8081/policy");
    }

    /**
     * A method for creating a palisade service as it would be configured as a standalone micro-service (server)
     *
     * @param args  Provides a means to pass in arguments into the method
     * @return a palisade service as it would be configured as a standalone micro-service (server)
     */
    protected PalisadeService createPalisadeServiceForServer(final String[] args) {
        return new SimplePalisadeService()
                .cacheService(createCacheService(args))
                .policyService(createPolicyServiceForClients(args))
                .resourceService(createResourceServiceForClients(args))
                .userService(createUserServiceForClients(args))
                .auditService(createAuditService(args));
    }

    /**
     * A method for creating the palisade service that a client (any of the other
     * types of services) would use to connect to the palisade service
     *
     * @param args  Provides a means to pass in arguments into the method
     * @return a palisade service that the client would use to talk to the palisade service server
     */
    protected PalisadeService createPalisadeServiceForClients(final String[] args) {
        return new ProxyRestPalisadeService("http://localhost:8080/palisade");
    }

    /**
     * A method for creating a data service as it would be configured as a standalone micro-service (server)
     *
     * @param args  Provides a means to pass in arguments into the method
     * @return a data service as it would be configured as a standalone micro-service (server)
     */
    protected DataService createDataServiceForServer(final String[] args) {
        try {
            Configuration conf = createHadoopConfiguration();
            HadoopDataReader reader = new HadoopDataReader().conf(conf);
            reader.addSerialiser(RESOURCE_TYPE, new ExampleObjSerialiser());
            return new SimpleDataService().reader(reader).palisadeService(createPalisadeServiceForClients(args));
        } catch (final IOException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            return null;
        }
    }

    /**
     * A method for creating the data service that a client (any of the other
     * types of services) would use to connect to the data service
     *
     * @param args  Provides a means to pass in arguments into the method
     * @return a data service that the client would use to talk to the data service server
     */
    protected DataService createDataServiceForClients(final String[] args) {
        return new ProxyRestDataService("http://localhost:8084/data");
    }
}
