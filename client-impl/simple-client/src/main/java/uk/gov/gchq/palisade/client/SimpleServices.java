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
import uk.gov.gchq.palisade.audit.service.impl.LoggerAuditService;
import uk.gov.gchq.palisade.cache.service.CacheService;
import uk.gov.gchq.palisade.cache.service.impl.HashMapCacheService;
import uk.gov.gchq.palisade.policy.service.PolicyService;
import uk.gov.gchq.palisade.policy.service.impl.HashMapPolicyService;
import uk.gov.gchq.palisade.resource.service.HashMapResourceService;
import uk.gov.gchq.palisade.resource.service.ResourceService;
import uk.gov.gchq.palisade.service.PalisadeService;
import uk.gov.gchq.palisade.service.impl.SimplePalisadeService;
import uk.gov.gchq.palisade.user.service.UserService;
import uk.gov.gchq.palisade.user.service.impl.HashMapUserService;

public class SimpleServices implements ServicesFactory {

    private final ResourceService resourceService;
    private final AuditService auditService;
    private final PolicyService policyService;
    private final UserService userService;
    private final CacheService cacheService;
    private final PalisadeService palisadeService;

    public SimpleServices() {
        this.resourceService = createResourceService();
        this.auditService = createAuditService();
        this.policyService = createPolicyService();
        this.userService = createUserService();
        this.cacheService = createCacheService();
        this.palisadeService = createPalisadeService();
    }

    @Override
    public ResourceService getResourceService() {
        return resourceService;
    }

    @Override
    public AuditService getAuditService() {
        return auditService;
    }

    @Override
    public PolicyService getPolicyService() {
        return policyService;
    }

    @Override
    public UserService getUserService() {
        return userService;
    }

    @Override
    public CacheService getCacheService() {
        return cacheService;
    }

    @Override
    public PalisadeService getPalisadeService() {
        return palisadeService;
    }

    protected CacheService createCacheService() {
        return new HashMapCacheService();
    }

    protected ResourceService createResourceService() {
        return new HashMapResourceService();
    }

    protected AuditService createAuditService() {
        return new LoggerAuditService();
    }

    protected PolicyService createPolicyService() {
        return new HashMapPolicyService();
    }

    protected UserService createUserService() {
        return new HashMapUserService();
    }

    protected PalisadeService createPalisadeService() {
        return new SimplePalisadeService(
                getResourceService(),
                getAuditService(),
                getPolicyService(),
                getUserService(),
                getCacheService()
        );
    }
}
