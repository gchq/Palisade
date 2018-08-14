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

import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.audit.service.AuditService;
import uk.gov.gchq.palisade.audit.service.impl.LoggerAuditService;
import uk.gov.gchq.palisade.cache.service.CacheService;
import uk.gov.gchq.palisade.cache.service.impl.HashMapCacheService;
import uk.gov.gchq.palisade.data.serialise.NullSerialiser;
import uk.gov.gchq.palisade.data.serialise.Serialiser;
import uk.gov.gchq.palisade.data.service.DataService;
import uk.gov.gchq.palisade.data.service.request.ReadRequest;
import uk.gov.gchq.palisade.data.service.request.ReadResponse;
import uk.gov.gchq.palisade.policy.service.PolicyService;
import uk.gov.gchq.palisade.policy.service.impl.HashMapPolicyService;
import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.resource.service.HashMapResourceService;
import uk.gov.gchq.palisade.resource.service.ResourceService;
import uk.gov.gchq.palisade.service.PalisadeService;
import uk.gov.gchq.palisade.service.impl.SimplePalisadeService;
import uk.gov.gchq.palisade.service.request.ConnectionDetail;
import uk.gov.gchq.palisade.service.request.DataRequestResponse;
import uk.gov.gchq.palisade.service.request.RegisterDataRequest;
import uk.gov.gchq.palisade.user.service.UserService;
import uk.gov.gchq.palisade.user.service.impl.HashMapUserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class SimpleClient<T> implements SimpleServices {
    private final Serialiser<T> serialiser;
    private final ResourceService resourceService;
    private final AuditService auditService;
    private final PolicyService policyService;
    private final UserService userService;
    private final CacheService cacheService;
    private final PalisadeService palisadeService;

    public SimpleClient() {
        this.serialiser = createSerialiser();
        this.resourceService = createResourceService();
        this.auditService = createAuditService();
        this.policyService = createPolicyService();
        this.userService = createUserService();
        this.cacheService = createCacheService();
        this.palisadeService = createPalisadeService();
    }

    public Stream<T> read(final String filename, final String resourceType, final String userId, final String justification) {
        Objects.requireNonNull(getPalisadeService());
        final RegisterDataRequest dataRequest = new RegisterDataRequest().resourceId(filename).userId(new UserId().id(userId)).context(new Context().justification(justification));
        final DataRequestResponse dataRequestResponse = getPalisadeService().registerDataRequest(dataRequest).join();
        final List<CompletableFuture<Stream<T>>> futureResults = new ArrayList<>(dataRequestResponse.getResources().size());

        for (final Entry<Resource, ConnectionDetail> entry : dataRequestResponse.getResources().entrySet()) {
            final ConnectionDetail connectionDetail = entry.getValue();
            final DataService dataService = connectionDetail.createService();

            final ReadRequest readRequest = new ReadRequest()
                    .requestId(dataRequestResponse.getRequestId())
                    .resource(entry.getKey());

            final CompletableFuture<ReadResponse> futureResponse = dataService.read(readRequest);
            final CompletableFuture<Stream<T>> futureResult = futureResponse.thenApply(
                    response -> getSerialiser().deserialise(response.getData())
            );
            futureResults.add(futureResult);
        }

        return futureResults.stream().flatMap(CompletableFuture::join);
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

    protected Serialiser<T> createSerialiser() {
        return new NullSerialiser<>();
    }

    public Serialiser<T> getSerialiser() {
        return serialiser;
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
}
