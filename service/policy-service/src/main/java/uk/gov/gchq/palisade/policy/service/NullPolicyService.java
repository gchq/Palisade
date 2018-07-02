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

package uk.gov.gchq.palisade.policy.service;

import uk.gov.gchq.palisade.policy.service.request.CanAccessRequest;
import uk.gov.gchq.palisade.policy.service.request.GetPolicyRequest;
import uk.gov.gchq.palisade.policy.service.request.SetPolicyRequest;

import java.util.concurrent.CompletableFuture;

/**
 * A null implementation of the {@link PolicyService} that prevents hitting
 * {@link NullPointerException}s if your deployment does not require a
 * {@link PolicyService}, but one is expected.
 */
public class NullPolicyService implements PolicyService {
    @Override
    public CompletableFuture<Boolean> canAccess(final CanAccessRequest futureRequest) {
        return CompletableFuture.completedFuture(false);
    }

    @Override
    public CompletableFuture<MultiPolicy> getPolicy(final GetPolicyRequest futureRequest) {
        return CompletableFuture.completedFuture(new MultiPolicy());
    }

    @Override
    public CompletableFuture<Boolean> setPolicy(final SetPolicyRequest futureRequest) {
        return CompletableFuture.completedFuture(false);
    }
}
