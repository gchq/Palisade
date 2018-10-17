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

public class RestUtil {
    private final static Logger LOGGER = LoggerFactory.getLogger(RestUtil.class);
    public static final int DELAY = 500;

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
                LOGGER.warn("Failed to get valid configuration for {}", serviceClass.getCanonicalName(), e);
                try {
                    Thread.sleep(DELAY);
                } catch (InterruptedException ignore) {
                    //ignore
                }
            }
        }
    }
}
