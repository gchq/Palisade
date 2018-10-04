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

import uk.gov.gchq.palisade.config.service.exception.NoConfigException;
import uk.gov.gchq.palisade.config.service.request.GetConfigRequest;
import uk.gov.gchq.palisade.service.Service;
import uk.gov.gchq.palisade.service.request.InitialConfig;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.Objects.requireNonNull;

public class ServiceConfigurator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceConfigurator.class);
    private static final long DELAY = 500;

    private final InitialConfigurationService configurationService;

    public ServiceConfigurator(final InitialConfigurationService configService) {
        requireNonNull(configService, "configurationService");
        this.configurationService = configService;
    }

    public <S extends Service> S configure(final S service) {
        return configureImpl(service, 0);
    }

    public <S extends Service> S configure(final S service, final Duration timeout) {
        requireNonNull(timeout, "timeout");
        return configureImpl(service, timeout.toMillis());
    }

    private <S extends Service> S configureImpl(final S service, long timeout) {
        requireNonNull(service, "service");
        if (timeout < 0) {
            throw new IllegalArgumentException("negative timeout not allowed");
        }

        //now loop until we can connect to the configuration service and get the configuration back
        long timeExpiry = System.currentTimeMillis() + timeout;

        GetConfigRequest request = new GetConfigRequest().service(Optional.of(service.getClass()));
        InitialConfig config = null;
        LOGGER.debug("Getting configuration for {} with timeout {}", service.getClass(), timeout);

        while (timeout == 0 || System.currentTimeMillis() < timeExpiry) {
            try {
                if (timeout > 0) {
                    config = configurationService.get(request)
                            .get(Math.max(1, timeExpiry - System.currentTimeMillis()), TimeUnit.MILLISECONDS);
                } else {
                    //no timeout
                    config = configurationService.get(request).join();
                }

            } catch (InterruptedException | ExecutionException | CancellationException e) {
                LOGGER.warn("Error while retrieving configuration for {}", service.getClass(), e);
                //keep trying after short delay
                try {
                    Thread.sleep(DELAY);
                } catch (InterruptedException ignore) {
                }
                continue;
            } catch (TimeoutException e) {
                LOGGER.debug("Timed out getting configuration for {}", service.getClass());
                continue;
            }
        }

        //if config is still null then we failed to get config
        if (config == null) {
            throw new NoConfigException("unable to retrieve configuration in specified time");
        } else {
            service.configure(config);
            return service;
        }
    }
}
