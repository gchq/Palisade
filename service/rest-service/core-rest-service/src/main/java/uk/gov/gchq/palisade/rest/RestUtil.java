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

import uk.gov.gchq.palisade.config.service.Configurator;
import uk.gov.gchq.palisade.config.service.InitialConfigurationService;
import uk.gov.gchq.palisade.exception.NoConfigException;
import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.palisade.service.Service;
import uk.gov.gchq.palisade.util.StreamUtil;

import java.io.InputStream;

/**
 * Utility class that includes functionality to create a {@link Service} from a given class name.
 */
public final class RestUtil {
    public static final String CONFIG_SERVICE_PATH = "palisade.rest.config.path";
    private static final Logger LOGGER = LoggerFactory.getLogger(RestUtil.class);
    public static final int DELAY = 500;

    private RestUtil() {
    }

    /**
     * Attempts to create the configured {@link Service}. This method provides the boot strapping for the REST services
     * to instantiate their services to which they delegate the actual implementations. This method will attempt to load
     * the {@link InitialConfigurationService} class details from the given path. Once this has been instantiated, it
     * will repeatedly call the configuration service trying to ge the name of the actual implementing class for the
     * given service and then configure it.
     *
     * @param resolverClass     the class to resolve the {@code configDetailsPath} against
     * @param configDetailsPath the path to the configuration JSON, either on the file system or in a JAR
     * @param serviceClass      the type of service to create and configure
     * @param <S>               type of service being returned
     * @return an instantiated configured service
     */
    public static <S extends Service> S createService(final Class<?> resolverClass, final String configDetailsPath, final Class<S> serviceClass) {
        S ret = null;

        //create config service object
        final InputStream stream = StreamUtil.openStream(resolverClass, configDetailsPath);
        InitialConfigurationService service = JSONSerialiser.deserialise(stream, InitialConfigurationService.class);
        //get the config for this service, try repeatedly until we get a valid configuration
        while (true) {
            try {
                return new Configurator(service).retrieveConfigAndCreate(serviceClass);
            } catch (NoConfigException e) {
                LOGGER.warn("Failed to get valid configuration for {}", serviceClass.getTypeName(), e);
                try {
                    Thread.sleep(DELAY);
                } catch (InterruptedException ignore) {
                    //ignore
                }
            }
        }
    }
}
