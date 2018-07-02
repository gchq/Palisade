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

package uk.gov.gchq.palisade.rest.application;

import com.google.common.collect.ObjectArrays;

import uk.gov.gchq.palisade.rest.service.v1.StatusServiceV1;

import javax.ws.rs.Path;

import static uk.gov.gchq.palisade.rest.application.AbstractApplicationConfigV1.VERSION;

/**
 * An implementation of {@code ApplicationConfig}, containing v1-specific configuration for the application.
 */
@Path(VERSION)
public abstract class AbstractApplicationConfigV1 extends AbstractApplicationConfig {
    public static final String VERSION = "v1";

    private static final Class<?>[] RESOURCES = new Class<?>[]{
            StatusServiceV1.class
    };

    protected AbstractApplicationConfigV1() {
        super(VERSION, RESOURCES);
    }

    protected AbstractApplicationConfigV1(final Class<?>... resourcesToRegister) {
        super(VERSION, ObjectArrays.concat(RESOURCES, resourcesToRegister, Class.class));
    }
}
