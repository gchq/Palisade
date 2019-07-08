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

package uk.gov.gchq.palisade.config.service.impl;

import uk.gov.gchq.palisade.config.service.ConfigUtils;
import uk.gov.gchq.palisade.config.service.ConfigurationService;
import uk.gov.gchq.palisade.rest.ServiceBinder;
import uk.gov.gchq.palisade.rest.application.AbstractApplicationConfigV1;

public class ApplicationConfigV1 extends AbstractApplicationConfigV1 {
    private static final Class<?>[] RESOURCES = new Class<?>[]{
            RestConfigServiceV1.class
    };

    public ApplicationConfigV1() {
        super(RESOURCES);
        String bootStrapLocation = ConfigUtils.retrieveBootstrapPath();

        //make sure we can inject the service instance
        ConfigurationService delegate = RestConfigServiceV1.createService(bootStrapLocation);
        register(new ServiceBinder(delegate, ConfigurationService.class));
    }


}
