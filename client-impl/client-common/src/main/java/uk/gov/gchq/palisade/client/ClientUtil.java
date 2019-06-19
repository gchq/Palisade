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

package uk.gov.gchq.palisade.client;

import uk.gov.gchq.palisade.config.service.ConfigUtils;
import uk.gov.gchq.palisade.config.service.ConfigurationService;
import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.palisade.service.PalisadeService;
import uk.gov.gchq.palisade.util.StreamUtil;

import java.io.InputStream;

/**
 * Client utilities.
 */
public final class ClientUtil {
    private ClientUtil() {
    }

    /**
     * Gets the entry point into the Palisade system. This should be the first method into the Palisade system that
     * client code should call. It will try to find the details for how to connect to Palisade's configuration service
     * by loading a configuration file referenced by system environment variable {@link uk.gov.gchq.palisade.config.service.ConfigConsts#CONFIG_SERVICE_PATH}.
     * It will then attempt to connect to this configuration service to find the main Palisade entry point which will
     * be an instance of {@link PalisadeService}. Clients should then call methods on this returned object.
     *
     * @return the Palisade system entry point
     * @apiNote This method should be regarded as the principal entry point to the Palisade system for clients.
     */
    public static PalisadeService getPalisadeClientEntryPoint() {
        //attempt to connect to Palisade
        final InputStream stream = StreamUtil.openStream(ClientUtil.class, ConfigUtils.retrieveConfigurationPath());
        ConfigurationService configService = JSONSerialiser.deserialise(stream, ConfigurationService.class);
        //get the client services
        ClientConfiguredServices configuredServices = new ClientConfiguredServices(configService);
        //create paliade service
        PalisadeService palisade = configuredServices.getPalisadeService();
        return palisade;
    }
}
