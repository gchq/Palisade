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
import uk.gov.gchq.palisade.service.request.ConfigConsts;
import uk.gov.gchq.palisade.service.request.InitialConfig;

import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.Objects.requireNonNull;

/**
 * A {@code Configurator} is responsible for creating Palisade {@link Service}s either from a supplied {@link
 * InitialConfig} or by connecting to a {@link InitialConfigurationService} and retrieving the configuration from
 * there.
 * <p>
 * No parameter may be {@code null}.
 */
public class Configurator {
    private static final Logger LOGGER = LoggerFactory.getLogger(Configurator.class);

    /**
     * The configuration service.
     */
    private final InitialConfigurationService service;

    /**
     * Create a {@code Configurator} object that retrieves configuration from the given service.
     *
     * @param service the configuration service
     */
    public Configurator(final InitialConfigurationService service) {
        requireNonNull(service, "service");
        this.service = service;
    }

    /**
     * Attempts to connect to the configuration service to get the configuration for the named service class. It will
     * then create the class and configure it based on the given information. This method will repeatedly try to connect
     * to the configuration service until a call succeeds.
     *
     * @param serviceClass the service class type to create
     * @param <S>          the type of service
     * @return a created service
     * @throws NoConfigException if no configuration for the service type could be found
     */
    public <S extends Service> S retrieveConfigAndCreate(final Class<S> serviceClass) throws NoConfigException {
        requireNonNull(serviceClass, "serviceClass");
        InitialConfig config = retrieveConfig(Optional.of(serviceClass));
        return createFromConfig(serviceClass, config);
    }

    /**
     * Attempts to connect to the configuration service to get the configuration for the named service class. It will
     * then create the class and configure it based on the given information. This method will repeatedly try to connect
     * to the configuration service until a call succeeds or the given duration expires.
     *
     * @param serviceClass the service class type to create
     * @param timeout      the length of time to try to retrieve a configuration before failing
     * @param <S>          the type of service
     * @return a created service
     * @throws NoConfigException if no configuration for the service type could be found, or the operation timed out
     */
    public <S extends Service> S retrieveConfigAndCreate(final Class<S> serviceClass, final Duration timeout) throws NoConfigException {
        requireNonNull(serviceClass, "serviceClass");
        requireNonNull(timeout, "timeout");
        InitialConfig config = retrieveConfig(Optional.of(serviceClass), timeout);
        return createFromConfig(serviceClass, config);
    }

    /**
     * Create a Palisade service from the given configuration. This method will look up the implementing class name for
     * the given service class from the configuration, attempt to create it and then call {@link
     * Service#applyConfigFrom(InitialConfig)} on the object.
     *
     * @param serviceClass the type of service class to create
     * @param config       the configuration to create the service from
     * @param <S>          the type of service
     * @return a created service
     * @throws IllegalStateException if the service class could not be created, if the implementing class name couldn't
     *                               be looked up, or the instance can't be configured properly from the given
     *                               configuration
     */
    public static <S extends Service> S createFromConfig(final Class<? extends Service> serviceClass, final InitialConfig config) throws IllegalStateException {
        requireNonNull(serviceClass, "serviceClass");
        requireNonNull(config, "config");
        try {
            String servClass = config.get(serviceClass.getTypeName());

            //try to create class type
            Class<S> classImpl = (Class<S>) Class.forName(servClass).asSubclass(Service.class);

            //create it
            S instance = classImpl.newInstance();

            //configure it
            instance.applyConfigFrom(config);
            return instance;
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchElementException e) {
            throw new IllegalStateException("couldn't create service class " + serviceClass, e);
        }
    }

    /**
     * Retrieves the initial configuration for a given service class. This will communicate with the configuration
     * service to try and get the configuration details for the given service class. If no service class is provided
     * (empty optional) then the client configuration will be retrieved. This method waits indefinitely for the
     * configuration to respond.
     *
     * @param serviceClass the optional service class to retrieve data for
     * @return the configuration data
     * @throws NoConfigException if no configuration data can be retrieved
     * @implNote This is equivalent to calling {@code retrieveConfig(serviceClass, 0)}.
     */
    public InitialConfig retrieveConfig(final Optional<Class<? extends Service>> serviceClass) throws NoConfigException {
        return retrieveConfig(serviceClass, 0);
    }

    /**
     * Retrieves the initial configuration for a given service class. This will communicate with the configuration
     * service to try and get the configuration details for the given service class. If no service class is provided
     * (empty optional) then the client configuration will be retrieved. This method throws an exception if the
     * configuration could not be retrieved in the given time specified.
     *
     * @param serviceClass the optional service class to retrieve data for
     * @param timeout      the amount of time to wait for the configuration service to respond
     * @return the configuration data
     * @throws NoConfigException if no configuration data can be retrieved, or the operation timed out
     */
    public InitialConfig retrieveConfig(final Optional<Class<? extends Service>> serviceClass, final Duration timeout) throws NoConfigException {
        requireNonNull(timeout, "timeout");
        return retrieveConfig(serviceClass, timeout.toMillis());
    }

    /**
     * Implements the logic to talk to a configuration service. Requests are repeatedly made in the case of failure (for
     * whatever reason, e.g. communication failure) until the time out expires. If the time out is zero, then this
     * method will try indefinitely.
     *
     * @param serviceClass the service class type to retrieve the configuration for
     * @param timeout      the time to wait for a successful response, 0 indicates try indefinitely
     * @return the configuration
     * @throws NoConfigException if the time out expires or no configuration could be found
     */
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
                LOGGER.warn("Error while retrieving configuration for {}", serviceClass, e);
                //keep trying after short delay
                try {
                    Thread.sleep(ConfigConsts.DELAY);
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
