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

import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.policy.TestRule;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.impl.SystemResource;
import uk.gov.gchq.palisade.rest.EmbeddedHttpServer;
import uk.gov.gchq.palisade.rule.Rules;
import uk.gov.gchq.palisade.service.PalisadeService;
import uk.gov.gchq.palisade.service.request.DataRequestConfig;
import uk.gov.gchq.palisade.service.request.DataRequestResponse;
import uk.gov.gchq.palisade.service.request.GetDataRequestConfig;
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

    private static final SystemResource sysResource = new SystemResource().id("file");
    private static final FileResource fileResource1 = new FileResource().id("file1").type("type1").serialisedFormat("format1").parent(sysResource);
    private static final User user = new User().userId("user01").roles("role1", "role2").auths("auth1", "auth2");
    private static final Context context = new Context().justification("justification1");
    private static final RequestId requestId = new RequestId().id("id1");
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

        final RegisterDataRequest request = new RegisterDataRequest().resourceId("file1").userId(user.getUserId()).context(context);

        final DataRequestResponse expectedResult = new DataRequestResponse().requestId(requestId).resource(fileResource1, new SimpleConnectionDetail().service(new MockPalisadeService()));
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

        final GetDataRequestConfig getDataRequestConfig = new GetDataRequestConfig().requestId(requestId).resource(fileResource1);
        final Map<LeafResource, Rules> rulesMap = new HashMap<>();
        rulesMap.put(fileResource1, new Rules().rule("testRule", new TestRule()));
        final DataRequestConfig expectedResult = new DataRequestConfig()
                .user(user)
                .context(context)
                .rules(rulesMap);
        given(palisadeService.getDataRequestConfig(getDataRequestConfig))
                .willReturn(CompletableFuture.completedFuture(expectedResult));

        // When
        final DataRequestConfig result = proxy.getDataRequestConfig(getDataRequestConfig).join();

        // Then
        assertEquals(expectedResult, result);
        verify(palisadeService).getDataRequestConfig(getDataRequestConfig);
    }
}
