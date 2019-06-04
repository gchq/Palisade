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

import java.time.Duration;

/**
 * Class constants.
 */
public final class ConfigConsts {

    private ConfigConsts() {
    }

    /**
     * The environment variable name which should contain the path of the JSON file used to instantiate
     * a {@link ConfigurationService} proxy.
     */
    public static final String CONFIG_SERVICE_PATH = "PALISADE_REST_CONFIG_PATH";

    /**
     * The delay between sending requests to the configuration service. In milliseconds.
     */
    public static final long DELAY = 500;

    /**
     * Timeout for retrieving a configuration from the configuration service.
     */
    public static final Duration CONFIG_TIMEOUT = Duration.ofSeconds(10);
}
