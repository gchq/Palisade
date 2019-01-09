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
package uk.gov.gchq.palisade.example.client;

import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.audit.service.AuditService;
import uk.gov.gchq.palisade.audit.service.impl.LoggerAuditService;
import uk.gov.gchq.palisade.cache.service.CacheService;
import uk.gov.gchq.palisade.cache.service.impl.BackingStore;
import uk.gov.gchq.palisade.cache.service.impl.HashMapBackingStore;
import uk.gov.gchq.palisade.cache.service.impl.SimpleCacheService;
import uk.gov.gchq.palisade.config.service.ConfigurationService;
import uk.gov.gchq.palisade.config.service.impl.SimpleConfigService;
import uk.gov.gchq.palisade.config.service.request.AddConfigRequest;
import uk.gov.gchq.palisade.data.service.DataService;
import uk.gov.gchq.palisade.data.service.impl.SimpleDataService;
import uk.gov.gchq.palisade.data.service.impl.reader.HadoopDataReader;
import uk.gov.gchq.palisade.data.service.reader.DataReader;
import uk.gov.gchq.palisade.example.data.serialiser.ExampleObjSerialiser;
import uk.gov.gchq.palisade.policy.service.PolicyService;
import uk.gov.gchq.palisade.policy.service.impl.HierarchicalPolicyService;
import uk.gov.gchq.palisade.resource.service.ResourceService;
import uk.gov.gchq.palisade.resource.service.impl.HadoopResourceService;
import uk.gov.gchq.palisade.service.PalisadeService;
import uk.gov.gchq.palisade.service.Service;
import uk.gov.gchq.palisade.service.impl.SimplePalisadeService;
import uk.gov.gchq.palisade.service.request.ConnectionDetail;
import uk.gov.gchq.palisade.service.request.ServiceConfiguration;
import uk.gov.gchq.palisade.service.request.SimpleConnectionDetail;
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
 * Convenience class for the examples to configure the config creation service. This is used to create an entire
 * configuration for Palisade for the examples from scratch.
 */
public final class ExampleConfigurator {

    protected static final String RESOURCE_TYPE = "exampleObj";
    private static final String HADOOP_CONF_PATH = "HADOOP_CONF_PATH";
    private static final Logger LOGGER = LoggerFactory.getLogger(ExampleConfigurator.class);

    private ExampleConfigurator() {
    }

    /**
     * Allows the creation of some configuration data for the single JVM example.
     *
     * @return the configuration service that provides the entry to Palisade
     */
    public static ConfigurationService setupSingleJVMConfigurationService() {
        CacheService cache = createCacheService(new HashMapBackingStore(true));
        ConfigurationService configService = new SimpleConfigService(cache);
        //configure the single JVM settings
        PolicyService policy = createPolicyService(configService, cache);
        UserService user = createUserService(configService, cache);
        AuditService audit = createAuditService(configService, cache);
        SimplePalisadeService palisade = createPalisadeService(configService, cache, policy, user, audit);

        HadoopResourceService resource;
        try {
            resource = createResourceService(configService, cache);
            HadoopDataReader reader = createDataReader(configService, cache);
            palisade.resourceService(resource);
            configureResourceConnectionDetails(resource, new SimpleConnectionDetail().service(createDataService(configService, cache, palisade, reader)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //write each of these to the initial config
        Collection<Service> services = Stream.of(audit, user, resource, policy, palisade, cache).collect(Collectors.toList());
        return writeClientConfiguration(configService, services);
    }

    /**
     * Allows the bootstrapping of some configuration data for the multi-JVM examples. This can be used by the multi JVM
     * examples.
     *
     * @param policyService              the {@link PolicyService}  that services should be told to use once they start
     * @param resourceService            the {@link ResourceService}  that services should be told to use once they start
     * @param userService                the {@link UserService}  that services should be told to use once they start
     * @param palisadeService            the {@link PalisadeService} that service should be told to use once they start
     * @param cacheService               the {@link CacheService} that services should be told to use once they start
     * @param configService              the {@link ConfigurationService} that services should use
     * @param dataServerConnectionDetail the details on where the data server is located
     * @return the configuration service to provide the Palisade entry point
     */
    public static ConfigurationService setupMultiJVMConfigurationService(final PolicyService policyService,
                                                                         final UserService userService,
                                                                         final ResourceService resourceService,
                                                                         final PalisadeService palisadeService,
                                                                         final CacheService cacheService,
                                                                         final ConfigurationService configService,
                                                                         final ConnectionDetail dataServerConnectionDetail) {
        requireNonNull(policyService, "policyService can not be null");
        requireNonNull(userService, "userService can not be null");
        requireNonNull(resourceService, "resourceService can not be null");
        requireNonNull(palisadeService, "palisadeService can not be null");
        requireNonNull(cacheService, "cacheService can not be null");
        requireNonNull(configService, "configService can not be null");
        requireNonNull(dataServerConnectionDetail, "dataServerConnectionDetail can not be null");

        AuditService audit = createAuditService(configService, cacheService);
        Collection<Service> services = Stream.of(audit, userService, resourceService, policyService, palisadeService, cacheService).collect(Collectors.toList());
        writeClientConfiguration(configService, services);

        //now populate cache with details for services to start up
        SimpleUserService remoteUser = createUserService(configService, cacheService);
        writeConfiguration(configService, remoteUser, UserService.class);

        SimplePalisadeService remotePalisade = createPalisadeService(configService, cacheService, policyService, userService, audit);
        remotePalisade.resourceService(resourceService);
        writeConfiguration(configService, remotePalisade, PalisadeService.class);

        HierarchicalPolicyService remotePolicy = createPolicyService(configService, cacheService);
        writeConfiguration(configService, remotePolicy, PolicyService.class);

        HadoopResourceService remoteResource = createResourceService(configService, cacheService);
        configureResourceConnectionDetails(remoteResource, dataServerConnectionDetail);
        writeConfiguration(configService, remoteResource, ResourceService.class);

        try {
            HadoopDataReader remoteReader = createDataReader(configService, cacheService);
            SimpleDataService remoteDataService = createDataService(configService, cacheService, palisadeService, remoteReader);
            //write configuration for the specific data service sub class
            writeConfiguration(configService, remoteDataService, DataService.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //since the ConfigurationService will have already started, there is no extra configuration to
        //write for it, so there is no writeConfiguration call for the config. service
        return configService;
    }

    /**
     * Create an {@link ServiceConfiguration} for the client and insert have each of the services write their configuration
     * into the service.
     *
     * @param configService the configuration service
     * @param services      collection of services to write to the configuration service
     * @return the {@code configService} argument
     */
    private static ConfigurationService writeClientConfiguration(final ConfigurationService configService, final Collection<Service> services) {
        ServiceConfiguration initial = new ServiceConfiguration();

        //each service to write their configuration into the initial configuration
        services.forEach(service -> service.recordCurrentConfigTo(initial));

        //insert this into the cache manually so it can be created later
        configService.add((AddConfigRequest) new AddConfigRequest()
                .config(initial)
                .service(Optional.empty())).join();
        return configService;
    }

    /**
     * Write the given configuration to the configuration manager from the service.
     *
     * @param configService the configuration service to write to
     * @param service       the service that will write its configuration
     * @param serviceClass  the type of Palisade service
     */
    private static void writeConfiguration(final ConfigurationService configService, final Service service, final Class<? extends Service> serviceClass) {
        ServiceConfiguration config = new ServiceConfiguration();
        service.recordCurrentConfigTo(config);
        configService.add((AddConfigRequest) new AddConfigRequest()
                .config(config)
                .service(Optional.of(serviceClass))).join();
    }

    /**
     * Makes a data reader.
     *
     * @param configService the configuration service
     * @param cacheService  the cache service the data reader should use
     * @return the data reader
     * @throws IOException if a failure occurs creating the reader
     */
    private static HadoopDataReader createDataReader(final ConfigurationService configService, final CacheService cacheService) throws IOException {
        Configuration conf = createHadoopConfiguration();
        HadoopDataReader reader = new HadoopDataReader().conf(conf);
        reader.addSerialiser(RESOURCE_TYPE, new ExampleObjSerialiser());
        return reader;
    }

    /**
     * Makes a {@link uk.gov.gchq.palisade.data.service.DataService}.
     *
     * @param configurationService the configuration service
     * @param cache                the cache service this service should use
     * @param palisade             the palisade service the data service should connect to
     * @param reader               the associated data reader
     * @return a data service
     */
    private static SimpleDataService createDataService(final ConfigurationService configurationService, final CacheService cache, final PalisadeService palisade, final DataReader reader) {
        return new SimpleDataService().palisadeService(palisade).reader(reader);
    }

    /**
     * Set up the example connection details in the resource service.
     *
     * @param resource             the resource service
     * @param exampleObjConnection connection details for the example resource
     */
    private static void configureResourceConnectionDetails(final HadoopResourceService resource, final ConnectionDetail exampleObjConnection) {
        final Map<String, ConnectionDetail> dataType = new HashMap<>();
        dataType.put(RESOURCE_TYPE, exampleObjConnection);
        resource.connectionDetail(null, dataType);
    }

    /**
     * Makes a user service.
     *
     * @param configService the configuration service
     * @param cacheService  the cache service the user service should use
     * @return the user service
     */
    private static SimpleUserService createUserService(final ConfigurationService configService, final CacheService cacheService) {
        return new SimpleUserService().cacheService(cacheService);
    }

    /**
     * Makes a Palisade service.
     *
     * @param configService the configuration service this service can use
     * @param cache         the cache service this service should use
     * @param policy        the policy service this service should use
     * @param user          the user service this service should use
     * @param audit         the audit service this service should use
     * @return the Palisade service
     */
    private static SimplePalisadeService createPalisadeService(final ConfigurationService configService, final CacheService cache,
                                                               final PolicyService policy, final UserService user, final AuditService audit) {
        return new SimplePalisadeService()
                .auditService(audit)
                .policyService(policy)
                .userService(user)
                .cacheService(cache);
    }

    /**
     * Makes a policy service.
     *
     * @param configService the configuration service
     * @param cacheService  the cache service the user service should use
     * @return the policy service
     */
    private static HierarchicalPolicyService createPolicyService(final ConfigurationService configService, final CacheService cacheService) {
        return new HierarchicalPolicyService().cacheService(cacheService);
    }

    /**
     * Makes an audit service.
     *
     * @param configService the configuration service
     * @param cacheService  the cache service
     * @return the audit service
     */
    private static LoggerAuditService createAuditService(final ConfigurationService configService, final CacheService cacheService) {
        return new LoggerAuditService();
    }

    /**
     * Makes a cache service.
     *
     * @param store the backing store
     * @return the cache service
     */
    private static SimpleCacheService createCacheService(final BackingStore store) {
        return new SimpleCacheService().backingStore(store);
    }

    /**
     * Creates a Hadoop configuration object. This loads the default Hadoop configuration and then checks the environment
     * variable {@link ExampleConfigurator#HADOOP_CONF_PATH} for a list of paths. Each one in the environment variable is
     * loaded into the configuration.
     *
     * @return a Hadoop configuration object
     */
    private static Configuration createHadoopConfiguration() {
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
     * Makes a resource service. A Hadoop configuration is created that is provided to the resource service. The configuration
     * is created via the {@link ExampleConfigurator#createHadoopConfiguration()} method.
     *
     * @param configService the Palisade configuration service
     * @param cache         the Palisade cache service
     * @return a configured resource service
     */
    private static HadoopResourceService createResourceService(final ConfigurationService configService, final CacheService cache) {
        try {
            Configuration conf = createHadoopConfiguration();
            HadoopResourceService resource = new HadoopResourceService().conf(conf).cacheService(cache);
            return resource;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
