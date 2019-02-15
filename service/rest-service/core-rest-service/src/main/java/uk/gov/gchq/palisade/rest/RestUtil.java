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
package uk.gov.gchq.palisade.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.ConfigConsts;
import uk.gov.gchq.palisade.config.service.ConfigurationService;
import uk.gov.gchq.palisade.config.service.Configurator;
import uk.gov.gchq.palisade.exception.NoConfigException;
import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.palisade.service.Service;
import uk.gov.gchq.palisade.service.ServiceState;
import uk.gov.gchq.palisade.util.StreamUtil;

import java.io.InputStream;
import java.util.Optional;

/**
 * Utility class that includes functionality to create a {@link Service} from a given class name.
 */
public final class RestUtil {
    public static final String CONFIG_SERVICE_PATH = "palisade.rest.config.path";
    private static final Logger LOGGER = LoggerFactory.getLogger(RestUtil.class);

    private RestUtil() {
    }

    /**
     * Attempts to create the configured {@link Service}. This method provides the boot strapping for the REST services
     * to instantiate their services to which they delegate the actual implementations. This method will attempt to load
     * the {@link ConfigurationService} class details from the given path. Once this has been instantiated, it
     * will repeatedly call the configuration service trying to get the name of the actual implementing class for the
     * given service and then configure it.
     *
     * @param resolverClass     the class to resolve the {@code configDetailsPath} against
     * @param configDetailsPath the path to the configuration JSON, either on the file system or in a JAR
     * @param serviceClass      the type of service to create and configure
     * @param overridable       list of regular expressions for keys that can be overridden from system properties
     * @param <S>               type of service being returned
     * @return an instantiated configured service
     * @see Configurator#createFromConfig(Class, uk.gov.gchq.palisade.service.ServiceState, String...)
     */
    public static <S extends Service> S createService(final Class<?> resolverClass, final String configDetailsPath, final Class<S> serviceClass, final String... overridable) {
        ConfigurationService service = createConfigServiceFromPath(resolverClass, configDetailsPath);
        //get the config for this service, try repeatedly until we get a valid configuration
        while (true) {
            try {
                return new Configurator(service).retrieveConfigAndCreate(serviceClass, overridable);
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
