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
import uk.gov.gchq.palisade.service.request.ServicesConfig;
import uk.gov.gchq.palisade.user.service.UserService;

import static java.util.Objects.requireNonNull;

public class ConfiguredServices implements ServicesFactory {

    private final ServicesConfig config;

    public ConfiguredServices(final ServicesConfig config) {
        requireNonNull(config, "config");
        this.config = config;
    }

    @Override
    public ResourceService getResourceService() {
        return null; //TODO
    }

    @Override
    public AuditService getAuditService() {
        return null; //TODO
    }

    @Override
    public PolicyService getPolicyService() {
        return null; //TODO
    }

    @Override
    public UserService getUserService() {
        return null; //TODO
    }

    @Override
    public CacheService getCacheService() {
        return null; //TODO
    }

    @Override
    public PalisadeService getPalisadeService() {
        return null; //TODO
    }
}
