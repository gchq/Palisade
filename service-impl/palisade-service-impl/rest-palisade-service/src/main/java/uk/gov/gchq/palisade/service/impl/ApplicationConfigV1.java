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

package uk.gov.gchq.palisade.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.config.service.ConfigUtils;
import uk.gov.gchq.palisade.rest.ServiceBinder;
import uk.gov.gchq.palisade.rest.application.AbstractApplicationConfigV1;
import uk.gov.gchq.palisade.service.PalisadeMetricProvider;
import uk.gov.gchq.palisade.service.PalisadeService;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class ApplicationConfigV1 extends AbstractApplicationConfigV1 {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationConfigV1.class);
    private static final Class<?>[] RESOURCES = new Class<?>[]{
            RestPalisadeServiceV1.class
    };

    public ApplicationConfigV1() {
        super(RESOURCES);
        //make sure we can inject the service instance
        PalisadeService delegate = RestPalisadeServiceV1.createService(System.getProperty(ConfigUtils.CONFIG_SERVICE_PATH));
        ServiceBinder binder = new ServiceBinder(delegate, PalisadeService.class);
        register(binder);
        //optionally register a metric provider
        PalisadeMetricProvider metrics = null;
        try {
            //first we check to see if one has been created in the configuration for us to create
            metrics = RestPalisadeMetricProviderV1.createService(System.getProperty(ConfigUtils.CONFIG_SERVICE_PATH));
        } catch (Exception e) {
            LOGGER.info("No metric provider configured in configuration service");
        }
        //second we check to see if the PalisadeService instance above is also a metric provider
        if (isNull(metrics) && delegate instanceof PalisadeMetricProvider) {
            metrics = (PalisadeMetricProvider) delegate;
            LOGGER.info("Using metric provider in PalisadeService instead");
        }
        //if we have a valid provider register it
        if (nonNull(metrics)) {
            register(RestPalisadeMetricProviderV1.class);
            binder.register(metrics, PalisadeMetricProvider.class);
        } else {
            //else we can't create one
            LOGGER.warn("Can't instantiate metrics provider");
        }
    }
}
