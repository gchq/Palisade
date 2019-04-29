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
import uk.gov.gchq.palisade.cache.service.CacheService;
import uk.gov.gchq.palisade.config.service.ConfigurationService;
import uk.gov.gchq.palisade.config.service.request.AddConfigRequest;
import uk.gov.gchq.palisade.data.serialise.AvroSerialiser;
import uk.gov.gchq.palisade.data.service.DataService;
import uk.gov.gchq.palisade.data.service.impl.SimpleDataService;
import uk.gov.gchq.palisade.data.service.impl.reader.HadoopDataReader;
import uk.gov.gchq.palisade.example.hrdatagenerator.types.Employee;
import uk.gov.gchq.palisade.policy.service.PolicyService;
import uk.gov.gchq.palisade.policy.service.impl.HierarchicalPolicyService;
import uk.gov.gchq.palisade.redirect.RESTRedirector;
import uk.gov.gchq.palisade.redirect.impl.SimpleRandomRedirector;
import uk.gov.gchq.palisade.resource.service.ResourceService;
import uk.gov.gchq.palisade.resource.service.impl.HadoopResourceService;
import uk.gov.gchq.palisade.service.ConnectionDetail;
import uk.gov.gchq.palisade.service.PalisadeService;
import uk.gov.gchq.palisade.service.Service;
import uk.gov.gchq.palisade.service.ServiceState;
import uk.gov.gchq.palisade.service.impl.SimplePalisadeService;
import uk.gov.gchq.palisade.user.service.UserService;
import uk.gov.gchq.palisade.user.service.impl.SimpleUserService;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

/**
 * Convenience class for setting the default config for the various Palisade micro-services
 * which assumes all services are being deployed locally using the standard port.
 * <p>
 * It is expected to be run after the config service has been started but before
 * the other services are started.
 */
public class ServicesConfigurator {
    protected static final Logger LOGGER = LoggerFactory.getLogger(ServicesConfigurator.class);
    protected static final String HADOOP_CONF_PATH = "HADOOP_CONF_PATH";
    public static final String RESOURCE_TYPE = "exampleEmployee";

    private final ProxyServicesFactory clientServices;

    public ServicesConfigurator(final ProxyServicesFactory clientServices) {
        requireNonNull(clientServices, "clientServices");
        LOGGER.info("Starting to set the service configurations.");
        this.clientServices = clientServices;

        // create the client config service
        ConfigurationService configClient = clientServices.createConfigService();

        // create the other client service
        AuditService auditService = clientServices.createAuditService();
        UserService userClient = clientServices.createUserService();
        ResourceService resourceClient = clientServices.createResourceService();
        PolicyService policyClient = clientServices.createPolicyService();
        PalisadeService palisadeClient = clientServices.createPalisadeService();
        DataService dataClient = clientServices.createDataService();
        CacheService cacheClient = clientServices.createCacheService();

        // add the config for the clients to the config service
        Collection<Service> services = Stream.of(auditService, configClient, userClient, resourceClient, policyClient, palisadeClient, dataClient, cacheClient).collect(Collectors.toList());
        writeClientConfiguration(configClient, services);

        // write the config for the user service to the config service
        writeServerConfiguration(configClient, createUserServiceForServer(), UserService.class);

        // write the config for the resource service to the config service
        writeServerConfiguration(configClient, createResourceServiceForServer(), ResourceService.class);

        // write the config for the policy service to the config service
        writeServerConfiguration(configClient, createPolicyServiceForServer(), PolicyService.class);

        // write the config for the palisade service to the config service
        writeServerConfiguration(configClient, createPalisadeServiceForServer(), PalisadeService.class);

        // write the config for the data service to the config service
        writeServerConfiguration(configClient, createDataServiceForServer(), DataService.class);

        // write the rest redirection for the data service redirection to the config service
        writeServerConfiguration(configClient, createRESTRedirectorForServer(), RESTRedirector.class);

        LOGGER.info("Finished setting the service configurations.");
    }

    /**
     * This will write each of the client service configs into the config service ready
     * for any of the services to get the details of how to contact those different services as required.
     *
     * @param configService the configuration service
     * @param services      collection of services to write to the configuration service
     */
    private void writeClientConfiguration(final ConfigurationService configService, final Collection<Service> services) {
        ServiceState initial = new ServiceState();

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
        ServiceState config = new ServiceState();
        service.recordCurrentConfigTo(config);
        configService.add((AddConfigRequest) new AddConfigRequest()
                .config(config)
                .service(Optional.of(serviceClass))).join();
    }

    /**
     * A method for creating a user service as it would be configured as a standalone micro-service (server)
     *
     * @return a user service as it would be configured as a standalone micro-service (server)
     */
    protected UserService createUserServiceForServer() {
        return new SimpleUserService().cacheService(clientServices.createCacheService());
    }

    /**
     * A method for creating a resource service as it would be configured as a standalone micro-service (server)
     *
     * @return a resource service as it would be configured as a standalone micro-service (server)
     */
    protected ResourceService createResourceServiceForServer() {
        try {
            Configuration conf = createHadoopConfiguration();
            HadoopResourceService resource = new HadoopResourceService().conf(conf).cacheService(clientServices.createCacheService());
            final Map<String, ConnectionDetail> dataType = new HashMap<>();
            dataType.put(RESOURCE_TYPE, clientServices.createDataServiceConnectionDetail());
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
     * A method for creating a policy service as it would be configured as a standalone micro-service (server)
     *
     * @return a policy service as it would be configured as a standalone micro-service (server)
     */
    protected PolicyService createPolicyServiceForServer() {
        return new HierarchicalPolicyService().cacheService(clientServices.createCacheService());
    }

    /**
     * A method for creating a palisade service as it would be configured as a standalone micro-service (server)
     *
     * @return a palisade service as it would be configured as a standalone micro-service (server)
     */
    protected PalisadeService createPalisadeServiceForServer() {
        return new SimplePalisadeService()
                .cacheService(clientServices.createCacheService())
                .policyService(clientServices.createPolicyService())
                .resourceService(clientServices.createResourceService())
                .userService(clientServices.createUserService())
                .auditService(clientServices.createAuditService());
    }

    /**
     * A method for creating a data service as it would be configured as a standalone micro-service (server)
     *
     * @return a data service as it would be configured as a standalone micro-service (server)
     */
    protected DataService createDataServiceForServer() {
        try {
            Configuration conf = createHadoopConfiguration();
            HadoopDataReader reader = new HadoopDataReader().conf(conf);
            reader.addSerialiser(RESOURCE_TYPE, new AvroSerialiser<>(Employee.class));
            return new SimpleDataService().reader(reader).palisadeService(clientServices.createPalisadeService()).cacheService(clientServices.createCacheService());
        } catch (final IOException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            return null;
        }
    }

    /**
     * A method for creating a user service as it would be configured as a standalone micro-service (server)
     *
     * @return a user service as it would be configured as a standalone micro-service (server)
     */
    protected RESTRedirector createRESTRedirectorForServer() {
        return new RESTRedirector(DataService.class.getTypeName(), "uk.gov.gchq.palisade.data.service.impl.RestDataServiceV1", new SimpleRandomRedirector().redirectionClass(SimpleDataService.class).cacheService(clientServices.createCacheService()),false);
    }
}
