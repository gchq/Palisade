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
package uk.gov.gchq.palisade.config.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.exception.NoConfigException;
import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.palisade.service.Service;
import uk.gov.gchq.palisade.service.ServiceState;
import uk.gov.gchq.palisade.util.StreamUtil;

import java.io.InputStream;
import java.util.Optional;

import static java.util.Objects.isNull;

/**
 * Utility class that includes functionality to create a {@link Service} from a given class name.
 */
public final class ConfigUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigUtils.class);

    private ConfigUtils() {
    }

    /**
     * Retrieves the path to the file that describes how to connect to the Palisade configuration service. This should be
     * in an environment variable named {@link ConfigConsts#CONFIG_SERVICE_PATH}.
     *
     * @return the configuration service file path
     * @throws IllegalStateException if the environment variable is not set
     */
    public static String retrieveConfigurationPath() {
        String path = System.getenv(ConfigConsts.CONFIG_SERVICE_PATH);
        if (isNull(path)) {
            path = System.getProperty(ConfigConsts.CONFIG_SERVICE_PATH);
        } else if (isNull(path)) {
            LOGGER.error("Couldn't find path for file describing how to connect to the Palisade configuration service. You need to set an environment " +
                    "variable named {} to the path, e.g. {}=/home/user/myConfig.json", ConfigConsts.CONFIG_SERVICE_PATH, ConfigConsts.CONFIG_SERVICE_PATH);
            throw new IllegalStateException(String.format("No configuration path due to environment variable %s not being set", ConfigConsts.CONFIG_SERVICE_PATH));
        }
        return path;
    }

    /**
     * Retrieves the path to the file that describes how to connect to the Palisade cache service back-end. This should be in an environment variable
     * OR Java system property named {@link ConfigConsts#BOOTSTRAP_CONFIG}. This method should only be used by the internal {@link ConfigurationService},
     * not by clients. Clients are directed to the {@link ConfigUtils#retrieveConfigurationPath()} method.
     *
     * @return the cache service file path
     * @throws IllegalStateException if neither the environment variable or system property are set
     */
    public static String retrieveBootstrapPath() {
        //check environment variable, if that doesn't work then check system property
        String bootStrapLocation = System.getenv(ConfigConsts.BOOTSTRAP_CONFIG);

        if (isNull(bootStrapLocation)) {
            bootStrapLocation = System.getProperty(ConfigConsts.BOOTSTRAP_CONFIG);
        }

        //if this still fails then throw an error
        if (isNull(bootStrapLocation)) {
            LOGGER.error("Couldn't find path for file describing how to connect to the Palisade cache service. You need to set an environment OR Java system property " +
                    "variable named {} to the path, e.g. {}=/home/user/bootstrap.json", ConfigConsts.BOOTSTRAP_CONFIG, ConfigConsts.BOOTSTRAP_CONFIG);
            throw new IllegalStateException(String.format("No configuration path due to environment variable or Java system property %s not being set", ConfigConsts.BOOTSTRAP_CONFIG));
        }

        return bootStrapLocation;
    }

    /**
     * Attempts to create the configured {@link Service}. This method provides the boot strapping for the REST services
     * to instantiate their services to which they delegate the actual implementations. This method will attempt to load
     * the {@link ConfigurationService} class details from the given path. Once this has been instantiated, it
     * will call the configuration service trying to get the name of the actual implementing class for the
     * given service and then configure it.
     *
     * @param resolverClass     the class to resolve the {@code configDetailsPath} against
     * @param configDetailsPath the path to the configuration JSON, either on the file system or in a JAR
     * @param serviceClass      the type of service to create and configure
     * @param overridable       list of regular expressions for keys that can be overridden from system properties
     * @param <S>               type of service being returned
     * @return an instantiated configured service
     * @throws NoConfigException if the configuration service could not find any configuration
     * @see Configurator#createFromConfig(Class, uk.gov.gchq.palisade.service.ServiceState, String...)
     */
    public static <S extends Service> S createService(final Class<?> resolverClass, final String configDetailsPath, final Class<S> serviceClass, final String... overridable) throws NoConfigException {
        ConfigurationService service = createConfigServiceFromPath(resolverClass, configDetailsPath);
        //get the config for this service, try repeatedly until we get a valid configuration
        return new Configurator(service).retrieveConfigAndCreate(serviceClass, ConfigConsts.CONFIG_TIMEOUT, overridable);
    }

    /**
     * Create a {@link ConfigurationService} from the JSON serialised form. Attempts to create the service by loading the given
     * path either from the classpath or from a file system path. If this succeeds then the service instance is created by
     * de-serialising it from the JSON in the opened file.
     *
     * @param resolverClass     the class to resolve the file against when searching in the classpath
     * @param configDetailsPath the path to the configuration file
     * @return a configuration service instance
     * @see Class#getResourceAsStream(String)
     * @see StreamUtil#openStream(Class, String)
     */
    public static ConfigurationService createConfigServiceFromPath(final Class<?> resolverClass, final String configDetailsPath) {
        //create config service object
        final InputStream stream = StreamUtil.openStream(resolverClass, configDetailsPath);
        return JSONSerialiser.deserialise(stream, ConfigurationService.class);
    }

    /**
     * Retrieve the {@link uk.gov.gchq.palisade.service.ServiceState} for the named service class. This method will attempt to load
     * the {@link ConfigurationService} class details from the given path. Once this has been instantiated, it
     * will repeatedly call the configuration service trying to get the details for the given service.
     *
     * @param resolverClass     the class to resolve the {@code configDetailsPath} against
     * @param configDetailsPath the path to the configuration JSON, either on the file system or in a JAR
     * @param serviceClass      the type of service to retrieve the configuration for
     * @return the service configuration for a class
     * @see Configurator#retrieveConfig(Optional)
     */
    public static ServiceState retrieveConfig(final Class<?> resolverClass, final String configDetailsPath, final Class<? extends Service> serviceClass) {
        //create config service object
        ConfigurationService service = createConfigServiceFromPath(resolverClass, configDetailsPath);
        //get the config for this service, try repeatedly until we get a valid configuration
        while (true) {
            try {
                return new Configurator(service).retrieveConfig(Optional.of(serviceClass));
            } catch (NoConfigException e) {
                LOGGER.warn("Failed to get valid configuration for {}", serviceClass.getTypeName(), e);
                try {
                    Thread.sleep(ConfigConsts.DELAY);
                } catch (InterruptedException ignore) {
                    //ignore
                }
            }
        }
    }
}
