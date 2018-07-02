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

package uk.gov.gchq.palisade.policy.service.impl;


import org.mockito.Mockito;

import uk.gov.gchq.palisade.policy.service.MultiPolicy;
import uk.gov.gchq.palisade.policy.service.PolicyService;
import uk.gov.gchq.palisade.policy.service.request.CanAccessRequest;
import uk.gov.gchq.palisade.policy.service.request.GetPolicyRequest;
import uk.gov.gchq.palisade.policy.service.request.SetPolicyRequest;

import java.util.concurrent.CompletableFuture;

public class MockPolicyService implements PolicyService {
    private static PolicyService mock = Mockito.mock(PolicyService.class);

    public static PolicyService getMock() {
        return mock;
    }

    public static void setMock(final PolicyService mock) {
        if (null == mock) {
            MockPolicyService.mock = Mockito.mock(PolicyService.class);
        }
        MockPolicyService.mock = mock;
    }


    @Override
    public CompletableFuture<Boolean> canAccess(final CanAccessRequest request) {
        return mock.canAccess(request);
    }

    @Override
    public CompletableFuture<MultiPolicy> getPolicy(final GetPolicyRequest request) {
        return mock.getPolicy(request);
    }

    @Override
    public CompletableFuture<Boolean> setPolicy(final SetPolicyRequest request) {
        return mock.setPolicy(request);
    }
}
