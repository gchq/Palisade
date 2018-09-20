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

package uk.gov.gchq.palisade.config.service.impl;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.mockito.Mockito;

import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.config.service.InitialConfigurationService;
import uk.gov.gchq.palisade.config.service.MockConfigurationService;
import uk.gov.gchq.palisade.config.service.exception.NoConfigException;
import uk.gov.gchq.palisade.config.service.request.GetConfigRequest;
import uk.gov.gchq.palisade.exception.PalisadeWrappedErrorRuntimeException;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.impl.SystemResource;
import uk.gov.gchq.palisade.rest.EmbeddedHttpServer;
import uk.gov.gchq.palisade.service.request.InitialConfig;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

public class RestConfigServiceV1IT {

    private static final SystemResource sysResource = new SystemResource().id("file");
    private static final FileResource fileResource1 = new FileResource().id("file1").type("type1").serialisedFormat("format1").parent(sysResource);
    private static final User user = new User().userId("user01").roles("role1", "role2").auths("auth1", "auth2");
    private static final Context context = new Context().justification("justification1");
    private static final RequestId requestId = new RequestId().id("id1");
    private static ProxyRestConfigService proxy;
    private static EmbeddedHttpServer server;

    @BeforeClass
    public static void beforeClass() throws IOException {
        System.setProperty(RestConfigServiceV1.SERVICE_CONFIG, "mockConfig.json");
        proxy = new ProxyRestConfigService("http://localhost:8080/palisade");
        server = new EmbeddedHttpServer(proxy.getBaseUrlWithVersion(), new ApplicationConfigV1());
        server.startServer();
    }

    @AfterClass
    public static void afterClass() {
        if (null != server) {
            server.stopServer();
        }
    }

    @Test(expected = PalisadeWrappedErrorRuntimeException.class)
    public void throwWhenNoConfig() throws Throwable {
        // Given
        final InitialConfigurationService configService = Mockito.mock(InitialConfigurationService.class);
        MockConfigurationService.setMock(configService);

        final GetConfigRequest request = new GetConfigRequest().service(Optional.empty());

        given(configService.get(request)).willThrow(new NoConfigException("No configuration has been set!"));
        // When
        try {
            final InitialConfig result = proxy.get(request).join();
        } catch (CompletionException e) {
            throw e.getCause();
        }
        // Then
        fail("exception expected");
    }

    @Test
    public void shouldGetConfigClient() throws IOException {
        // Given
        final InitialConfigurationService configService = Mockito.mock(InitialConfigurationService.class);
        MockConfigurationService.setMock(configService);

        final GetConfigRequest request = new GetConfigRequest().service(Optional.empty());

        InitialConfig expected = new InitialConfig().put("test_key", "test_value");

        given(configService.get(request)).willReturn(CompletableFuture.completedFuture(expected));

        // When
        final InitialConfig result = proxy.get(request).join();

        // Then
        assertEquals(expected, result);
        verify(configService).get(request);
    }

    @Test
    public void shouldGetConfigService() throws IOException {
        // Given
        final InitialConfigurationService configService = Mockito.mock(InitialConfigurationService.class);
        MockConfigurationService.setMock(configService);

        final GetConfigRequest request = new GetConfigRequest().service(Optional.empty());

        InitialConfig expected = new InitialConfig().put("some_important_key", "some_important_value");

        given(configService.get(request)).willReturn(CompletableFuture.completedFuture(expected));

        // When
        final InitialConfig result = proxy.get(request).join();

        // Then
        assertEquals(expected, result);
        verify(configService).get(request);
    }
}
