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

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import org.glassfish.jersey.server.ResourceConfig;

import uk.gov.gchq.palisade.rest.FactoriesBinder;
import uk.gov.gchq.palisade.rest.SystemProperty;
import uk.gov.gchq.palisade.rest.mapper.GenericExceptionMapper;
import uk.gov.gchq.palisade.rest.mapper.PalisadeCheckedExceptionMapper;
import uk.gov.gchq.palisade.rest.mapper.PalisadeRuntimeExceptionMapper;
import uk.gov.gchq.palisade.rest.mapper.ProcessingExceptionMapper;
import uk.gov.gchq.palisade.rest.mapper.WebApplicationExceptionMapper;
import uk.gov.gchq.palisade.rest.serialisation.RestJsonProvider;
import uk.gov.gchq.palisade.rest.serialisation.TextMessageBodyWriter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * An {@code ApplicationConfig} sets up the application resources,
 * and any other application-specific configuration.
 */
public abstract class AbstractApplicationConfig extends ResourceConfig {
    protected final Set<Class<?>> resources = new HashSet<>();
    private final String version;

    protected AbstractApplicationConfig(final String version) {
        this(version, new Class[0]);
    }

    protected AbstractApplicationConfig(final String version, final Class<?>... resourcesToRegister) {
        this.version = version;
        addSystemResources();
        addExceptionMappers();
        addCoreServices();
        Collections.addAll(resources, resourcesToRegister);
        setupBeanConfig();
        registerClasses(resources);
        register(new FactoriesBinder());
    }

    protected void addSystemResources() {
        resources.add(ApiListingResource.class);
        resources.add(SwaggerSerializers.class);
        resources.add(RestJsonProvider.class);
        resources.add(TextMessageBodyWriter.class);
    }

    protected void addExceptionMappers() {
        resources.add(PalisadeCheckedExceptionMapper.class);
        resources.add(PalisadeRuntimeExceptionMapper.class);
        resources.add(ProcessingExceptionMapper.class);
        resources.add(WebApplicationExceptionMapper.class);
        resources.add(GenericExceptionMapper.class);
    }

    /**
     * Should add version-specific classes to the collection of resources.
     */
    protected void addCoreServices() {
    }

    /**
     * Should set various properties for Swagger's initialization.
     */
    protected void setupBeanConfig() {
        final BeanConfig beanConfig = new BeanConfig();

        String basePath = System.getProperty(SystemProperty.BASE_PATH, SystemProperty.BASE_PATH_DEFAULT);
        if (!basePath.startsWith("/")) {
            basePath = "/" + basePath;
        }

        beanConfig.setBasePath(basePath + '/' + version);
        beanConfig.setConfigId(version);
        beanConfig.setScannerId(version);
        beanConfig.setResourcePackage("uk.gov.gchq.palisade");
        beanConfig.setScan(true);
    }
}
