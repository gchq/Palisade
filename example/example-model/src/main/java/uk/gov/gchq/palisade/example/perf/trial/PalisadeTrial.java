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

package uk.gov.gchq.palisade.example.perf.trial;

import uk.gov.gchq.palisade.client.ClientConfiguredServices;
import uk.gov.gchq.palisade.config.service.ConfigUtils;
import uk.gov.gchq.palisade.config.service.ConfigurationService;
import uk.gov.gchq.palisade.example.perf.PerfTrial;
import uk.gov.gchq.palisade.example.perf.actions.SetPolicyAction;
import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.palisade.util.StreamUtil;

import java.io.InputStream;

/**
 * Base class for Palisade reading tests. This allows sub-classes to change which part of Palisade they are performance
 * testing, e.g. the setup cost, the entire request or just the data read part.
 */
public abstract class PalisadeTrial implements PerfTrial {

    /**
     * Get the Palisade client services.
     *
     * @return client services provider
     */
    protected ClientConfiguredServices getPalisadeClientServices() {
        //attempt to connect to Palisade
        final InputStream stream = StreamUtil.openStream(SetPolicyAction.class, System.getProperty(ConfigUtils.CONFIG_SERVICE_PATH));
        ConfigurationService configService = JSONSerialiser.deserialise(stream, ConfigurationService.class);
        ClientConfiguredServices cs = new ClientConfiguredServices(configService);
        return cs;
    }
}
