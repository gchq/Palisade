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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.policy.service.MultiPolicy;
import uk.gov.gchq.palisade.policy.service.Policy;
import uk.gov.gchq.palisade.policy.service.PolicyService;
import uk.gov.gchq.palisade.policy.service.request.CanAccessRequest;
import uk.gov.gchq.palisade.policy.service.request.GetPolicyRequest;
import uk.gov.gchq.palisade.policy.service.request.SetPolicyRequest;
import uk.gov.gchq.palisade.policy.service.response.CanAccessResponse;
import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.rest.EmbeddedHttpServer;
import uk.gov.gchq.palisade.service.impl.ProxyRestPolicyService;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

public class RestPolicyServiceV1IT {

    private static ProxyRestPolicyService proxy;
    private static EmbeddedHttpServer server;

    @BeforeClass
    public static void beforeClass() throws IOException {
        System.setProperty(RestPolicyServiceV1.SERVICE_CONFIG, "mockConfig.json");
        proxy = new ProxyRestPolicyService("http://localhost:8081/policy");
        server = new EmbeddedHttpServer(proxy.getBaseUrlWithVersion(), new ApplicationConfigV1());
        server.startServer();
    }

    @AfterClass
    public static void afterClass() {
        if (null != server) {
            server.stopServer();
        }
    }

    @Test
    public void shouldCallCanAccess() throws IOException {
        // Given
        final PolicyService policyService = Mockito.mock(PolicyService.class);
        MockPolicyService.setMock(policyService);

        final FileResource resource = new FileResource().id("file1");
        final UserId userId = new UserId().id("user01");
        final User user = new User().userId(userId).roles("role1", "role2").auths("auth1", "auth2");
        final Context context = new Context().justification("justification1");
        final CanAccessRequest request = new CanAccessRequest().resources(Collections.singletonList(resource)).user(user).context(context);

        given(policyService.canAccess(request)).willReturn(CompletableFuture.completedFuture(new CanAccessResponse(Collections.singletonList(resource))));

        // When
        final CanAccessResponse result = proxy.canAccess(request).join();

        // Then
        assertEquals(result.getCanAccessResources(), Collections.singletonList(resource));
        verify(policyService).canAccess(request);
    }

    @Test
    public void shouldGetPolicy() throws IOException {
        // Given
        final PolicyService policyService = Mockito.mock(PolicyService.class);
        MockPolicyService.setMock(policyService);

        final FileResource resource1 = new FileResource().id("file1");
        final FileResource resource2 = new FileResource().id("file2");
        final UserId userId = new UserId().id("user01");
        final User user = new User().userId(userId).roles("role1", "role2").auths("auth1", "auth2");
        final Context context = new Context().justification("justification1");
        final GetPolicyRequest request = new GetPolicyRequest().user(user).context(context).resources(Arrays.asList(resource1, resource2));

        final Map<Resource, Policy> policies = new HashMap<>();
        policies.put(resource1, new Policy<>().owner(user));
        policies.put(resource2, new Policy<>().owner(user));
        final MultiPolicy expectedResult = new MultiPolicy().policies(policies);
        given(policyService.getPolicy(request)).willReturn(CompletableFuture.completedFuture(expectedResult));

        // When
        final MultiPolicy result = proxy.getPolicy(request).join();

        // Then
        assertEquals(expectedResult, result);
        verify(policyService).getPolicy(request);
    }

    @Test
    public void shouldSetPolicy() throws IOException {
        // Given
        final PolicyService policyService = Mockito.mock(PolicyService.class);
        MockPolicyService.setMock(policyService);

        final FileResource resource1 = new FileResource().id("file1");
        final SetPolicyRequest request = new SetPolicyRequest().resource(resource1).policy(new Policy<>().owner(new User()));

        given(policyService.setPolicy(request)).willReturn(CompletableFuture.completedFuture(true));

        // When
        final Boolean result = proxy.setPolicy(request).join();

        // Then
        assertTrue(result);
        verify(policyService).setPolicy(request);
    }
}
