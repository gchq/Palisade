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

import uk.gov.gchq.palisade.config.service.request.GetConfigRequest;
import uk.gov.gchq.palisade.exception.NoConfigException;
import uk.gov.gchq.palisade.service.Service;
import uk.gov.gchq.palisade.service.request.InitialConfig;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.Objects.requireNonNull;

public class Configurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Configurator.class);
    private static final long DELAY = 500;

    private final InitialConfigurationService service;

    public Configurator(final InitialConfigurationService service) {
        requireNonNull(service, "service");
        this.service = service;
    }

    public <S extends Service> S configureAndConfigure(final Class<S> serviceClass) throws NoConfigException {
        requireNonNull(serviceClass, "serviceClass");
        InitialConfig config = retrieveConfig(Optional.of(serviceClass));
        return createAndConfigure(serviceClass, config);
    }

    public <S extends Service> S createAndConfigure(final Class<S> serviceClass, final Duration timeout) throws NoConfigException {
        requireNonNull(serviceClass, "serviceClass");
        requireNonNull(timeout, "timeout");
        InitialConfig config = retrieveConfig(Optional.of(serviceClass), timeout);
        return createAndConfigure(serviceClass, config);
    }

    public <S extends Service> S createAndConfigure(final Class<? extends Service> serviceClass, final InitialConfig config) {
        requireNonNull(serviceClass, "serviceClass");
        requireNonNull(config, "config");
        try {
            String servClass = config.get(serviceClass.getCanonicalName());

            //try to create class type
            Class<S> classImpl = (Class<S>) Class.forName(servClass).asSubclass(Service.class);

            //create it
            S instance = classImpl.newInstance();

            //configure it
            instance.configure(config);
            return instance;
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new IllegalStateException("couldn't create service class " + serviceClass, e);
        }
    }

    public InitialConfig retrieveConfig(final Optional<Class<? extends Service>> serviceClass) throws NoConfigException {
        return retrieveConfig(serviceClass, 0);
    }

    public InitialConfig retrieveConfig(final Optional<Class<? extends Service>> serviceClass, final Duration timeout) throws NoConfigException {
        requireNonNull(timeout, "timeout");
        return retrieveConfig(serviceClass, timeout.toMillis());
    }

    private InitialConfig retrieveConfig(final Optional<Class<? extends Service>> serviceClass, final long timeout) throws NoConfigException {
        requireNonNull(serviceClass, "serviceClass");
        if (timeout < 0) {
            throw new IllegalArgumentException("negative timeout not allowed");
        }

        //now loop until we can connect to the configuration service and get the configuration back
        long timeExpiry = System.currentTimeMillis() + timeout;

        GetConfigRequest request = new GetConfigRequest().service(serviceClass);
        InitialConfig config = null;
        LOGGER.debug("Getting configuration for {} with timeout {}", serviceClass, timeout);

        while (config == null && (timeout == 0 || System.currentTimeMillis() < timeExpiry)) {
            try {
                if (timeout > 0) {
                    config = service.get(request)
                            .get(Math.max(1, timeExpiry - System.currentTimeMillis()), TimeUnit.MILLISECONDS);
                } else {
                    //no timeout
                    config = service.get(request).join();
                }

            } catch (InterruptedException | ExecutionException | CancellationException e) {
                LOGGER.warn("Error while retrieving configuration for {}", serviceClass.getClass(), e);
                //keep trying after short delay
                try {
                    Thread.sleep(DELAY);
                } catch (InterruptedException ignore) {
                }
                continue;
            } catch (TimeoutException e) {
                LOGGER.debug("Timed out getting configuration for {}", serviceClass.getClass());
                continue;
            }
        }

        //if config is still null then we failed to get config
        if (config == null) {
            throw new NoConfigException("unable to retrieve configuration in specified time");
        } else {
            return config;
        }
    }
}
