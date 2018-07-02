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

package uk.gov.gchq.palisade.service.impl;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import uk.gov.gchq.palisade.Justification;
import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.policy.Rules;
import uk.gov.gchq.palisade.policy.TestRule;
import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.rest.EmbeddedHttpServer;
import uk.gov.gchq.palisade.service.PalisadeService;
import uk.gov.gchq.palisade.service.request.ConnectionDetail;
import uk.gov.gchq.palisade.service.request.DataRequestConfig;
import uk.gov.gchq.palisade.service.request.DataRequestResponse;
import uk.gov.gchq.palisade.service.request.RegisterDataRequest;
import uk.gov.gchq.palisade.service.request.SimpleConnectionDetail;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

public class RestPalisadeServiceV1IT {

    private static ProxyRestPalisadeService proxy;
    private static EmbeddedHttpServer server;

    @BeforeClass
    public static void beforeClass() throws IOException {
        System.setProperty(RestPalisadeServiceV1.SERVICE_CONFIG, "mockConfig.json");
        proxy = new ProxyRestPalisadeService("http://localhost:8080/palisade");
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
    public void shouldRegisterDataRequest() throws IOException {
        // Given
        final PalisadeService palisadeService = Mockito.mock(PalisadeService.class);
        MockPalisadeService.setMock(palisadeService);

        final FileResource resource1 = new FileResource("file1");
        final FileResource resource2 = new FileResource("file1");
        final UserId userId = new UserId("user01");
        final Justification justification = new Justification("justification1");
        final RegisterDataRequest request = new RegisterDataRequest("file1", userId, justification);

        final Map<Resource, ConnectionDetail> resources = new HashMap<>();
        resources.put(resource1, new SimpleConnectionDetail("details1"));
        resources.put(resource2, new SimpleConnectionDetail("details2"));
        final DataRequestResponse expectedResult = new DataRequestResponse(new RequestId("id1"), resources);
        given(palisadeService.registerDataRequest(request)).willReturn(CompletableFuture.completedFuture(expectedResult));

        // When
        final DataRequestResponse result = proxy.registerDataRequest(request).join();

        // Then
        assertEquals(expectedResult, result);
        verify(palisadeService).registerDataRequest(request);
    }

    @Test
    public void shouldGetDataRequestConfig() throws IOException {
        // Given
        final PalisadeService palisadeService = Mockito.mock(PalisadeService.class);
        MockPalisadeService.setMock(palisadeService);

        final FileResource resource1 = new FileResource("file1");
        final FileResource resource2 = new FileResource("file1");
        final Map<Resource, ConnectionDetail> resources = new HashMap<>();
        resources.put(resource1, new SimpleConnectionDetail("details1"));
        resources.put(resource2, new SimpleConnectionDetail("details2"));
        final DataRequestResponse request = new DataRequestResponse(new RequestId("id1"), resources);

        final UserId userId = new UserId("user01");
        final User user = new User().userId(userId).roles("role1", "role2").auths("auth1", "auth2");
        final Justification justification = new Justification("justification1");
        final Map<Resource, Rules> rules = new HashMap<>();
        rules.put(resource1, new Rules<String>().rule("testRule", new TestRule()));
        final DataRequestConfig expectedResult = new DataRequestConfig(user, justification, rules);
        given(palisadeService.getDataRequestConfig(request)).willReturn(CompletableFuture.completedFuture(expectedResult));

        // When
        final DataRequestConfig result = proxy.getDataRequestConfig(request).join();

        // Then
        assertEquals(expectedResult, result);
        verify(palisadeService).getDataRequestConfig(request);
    }
}
