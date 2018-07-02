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

import uk.gov.gchq.palisade.Justification;
import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.audit.service.AuditService;
import uk.gov.gchq.palisade.audit.service.impl.LoggerAuditService;
import uk.gov.gchq.palisade.cache.service.CacheService;
import uk.gov.gchq.palisade.cache.service.impl.HashMapCacheService;
import uk.gov.gchq.palisade.data.serialise.NullSerialiser;
import uk.gov.gchq.palisade.data.serialise.Serialiser;
import uk.gov.gchq.palisade.data.service.DataService;
import uk.gov.gchq.palisade.data.service.impl.SimpleDataService;
import uk.gov.gchq.palisade.data.service.reader.DataReader;
import uk.gov.gchq.palisade.data.service.reader.SimpleDataReader;
import uk.gov.gchq.palisade.data.service.request.ReadRequest;
import uk.gov.gchq.palisade.data.service.request.ReadResponse;
import uk.gov.gchq.palisade.policy.service.PolicyService;
import uk.gov.gchq.palisade.policy.service.impl.HashMapPolicyService;
import uk.gov.gchq.palisade.resource.service.HashMapResourceService;
import uk.gov.gchq.palisade.resource.service.ResourceService;
import uk.gov.gchq.palisade.service.PalisadeService;
import uk.gov.gchq.palisade.service.impl.SimplePalisadeService;
import uk.gov.gchq.palisade.service.request.DataRequestResponse;
import uk.gov.gchq.palisade.service.request.RegisterDataRequest;
import uk.gov.gchq.palisade.user.service.UserService;
import uk.gov.gchq.palisade.user.service.impl.HashMapUserService;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class SimpleClient<T> {
    private final Serialiser<?, T> serialiser;
    private final PalisadeService palisadeService;
    private final DataService dataService;

    public SimpleClient() {
        this.serialiser = createSerialiser();
        this.palisadeService = createPalisadeService();
        this.dataService = createDataService();
    }

    public Stream<T> read(final String filename, final String resourceType, final String userId, final String justification) {
        Objects.requireNonNull(palisadeService);
        Objects.requireNonNull(dataService);
        final RegisterDataRequest dataRequest = new RegisterDataRequest(filename, new UserId(userId), new Justification(justification));
        final DataRequestResponse dataRequestResponse = palisadeService.registerDataRequest(dataRequest).join();
        final CompletableFuture<ReadResponse<Object>> futureResponse = dataService.read(new ReadRequest(dataRequestResponse));
        return futureResponse.thenApply(response -> response.getData().map(((Serialiser<Object, T>) serialiser)::deserialise)).join();
    }

    protected PalisadeService createPalisadeService() {
        return new SimplePalisadeService(
                createResourceService(),
                createAuditService(),
                createPolicyService(),
                createUserService(),
                createCacheService()
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

    protected DataService createDataService() {
        return new SimpleDataService(palisadeService, createDataReader());
    }

    protected DataReader createDataReader() {
        return new SimpleDataReader();
    }

    protected Serialiser<?, T> createSerialiser() {
        return new NullSerialiser<>();
    }
}
