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

package uk.gov.gchq.palisade.user.service.impl;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.rest.EmbeddedHttpServer;
import uk.gov.gchq.palisade.user.service.UserService;
import uk.gov.gchq.palisade.user.service.request.AddUserRequest;
import uk.gov.gchq.palisade.user.service.request.GetUserRequest;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

public class RestUserServiceV1IT {

    private static ProxyRestUserService proxy;
    private static EmbeddedHttpServer server;

    @BeforeClass
    public static void beforeClass() throws IOException {
        System.setProperty(RestUserServiceV1.SERVICE_CONFIG, "mockConfig.json");
        proxy = new ProxyRestUserService("http://localhost:8083/user");
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
    public void shouldAddUser() throws IOException {
        // Given
        final UserService userService = Mockito.mock(UserService.class);
        MockUserService.setMock(userService);

        final UserId userId = new UserId("user01");
        final User user = new User().userId(userId).roles("role1", "role2").auths("auth1", "auth2");
        final AddUserRequest addUserRequest = new AddUserRequest(user);

        given(userService.addUser(addUserRequest)).willReturn(CompletableFuture.completedFuture(true));

        // When
        final Boolean result = proxy.addUser(addUserRequest).join();

        // Then
        assertTrue(result);
        verify(userService).addUser(addUserRequest);
    }

    @Test
    public void shouldGetUser() throws IOException {
        // Given
        final UserService userService = Mockito.mock(UserService.class);
        MockUserService.setMock(userService);

        final UserId userId = new UserId("user01");
        final User user = new User().userId(userId).roles("role1", "role2").auths("auth1", "auth2");
        final GetUserRequest getUserRequest = new GetUserRequest(user.getUserId());

        given(userService.getUser(getUserRequest)).willReturn(CompletableFuture.completedFuture(user));

        // When
        final User responseUser = proxy.getUser(getUserRequest).join();

        // Then
        assertEquals(user, responseUser);
    }
}
