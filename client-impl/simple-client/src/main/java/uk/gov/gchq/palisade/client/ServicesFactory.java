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
import uk.gov.gchq.palisade.user.service.UserService;

/**
 * An interface that other consumers of the simple clients can use to consume the various Palisade services.
 */
public interface ServicesFactory {

    /**
     * Get the Palisade resource service.
     *
     * @return an instance of {@link ResourceService}
     */
    ResourceService getResourceService();

    /**
     * Get the Palisade audit service.
     *
     * @return an instance of {@link AuditService}
     */
    AuditService getAuditService();

    /**
     * Get the Palisade policy service.
     *
     * @return an instance of {@link PolicyService}
     */
    PolicyService getPolicyService();

    /**
     * Get the Palisade user service.
     *
     * @return an instance of {@link UserService}
     */
    UserService getUserService();

    /**
     * Get the Palisade cache service.
     *
     * @return an instance of {@link CacheService}
     */
    CacheService getCacheService();

    /**
     * Get the Palisade service itself.
     *
     * @return an instance of {@link PalisadeService}
     */
    PalisadeService getPalisadeService();
}
