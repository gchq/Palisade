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

package uk.gov.gchq.palisade.example.config;

/**
 * Convenience class for setting the default config for the various Palisade micro-services
 * which assumes all services are being deployed locally using the standard port.
 * <p>
 * It is expected to be run after the config service has been started but before
 * the other services are started.
 */
public class DistributedServices {

    public static void main(final String[] args) {
        new ServicesConfigurator(new ClientServices(args));
    }
}
