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
package uk.gov.gchq.palisade.client;

import uk.gov.gchq.palisade.audit.service.AuditService;
import uk.gov.gchq.palisade.cache.service.CacheService;
import uk.gov.gchq.palisade.policy.service.PolicyService;
import uk.gov.gchq.palisade.resource.service.ResourceService;
import uk.gov.gchq.palisade.service.PalisadeService;
import uk.gov.gchq.palisade.service.Service;
import uk.gov.gchq.palisade.service.request.InitialConfig;
import uk.gov.gchq.palisade.user.service.UserService;

import static java.util.Objects.requireNonNull;

public class ConfiguredServices implements ServicesFactory {

    private final InitialConfig config;

    public ConfiguredServices(final InitialConfig config) {
        requireNonNull(config, "config");
        this.config = config;
    }

    @Override
    public ResourceService getResourceService() {
        String implClass = config.get(ResourceService.class.getCanonicalName());
        return createAndConfigure(implClass);
    }

    @Override
    public AuditService getAuditService() {
        String implClass = config.get(AuditService.class.getCanonicalName());
        return createAndConfigure(implClass);
    }

    @Override
    public PolicyService getPolicyService() {
        String implClass = config.get(PolicyService.class.getCanonicalName());
        return createAndConfigure(implClass);
    }

    @Override
    public UserService getUserService() {
        String implClass = config.get(UserService.class.getCanonicalName());
        return createAndConfigure(implClass);
    }

    @Override
    public CacheService getCacheService() {
        String implClass = config.get(CacheService.class.getCanonicalName());
        return createAndConfigure(implClass);
    }

    @Override
    public PalisadeService getPalisadeService() {
        String implClass = config.get(PalisadeService.class.getCanonicalName());
        return createAndConfigure(implClass);
    }

    protected <S extends Service> S createAndConfigure(final String serviceClass) {
        requireNonNull(serviceClass, "serviceClass");
        try {
            //try to create an instance
            Class<S> classImpl = (Class<S>) Class.forName(serviceClass).asSubclass(Service.class);
            S instance = classImpl.newInstance();
            //configure it
            instance.configure(config);
            return instance;
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new IllegalStateException("couldn't create service class " + serviceClass, e);
        }
    }
}
