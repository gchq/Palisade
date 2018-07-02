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


import org.mockito.Mockito;

import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.user.service.UserService;
import uk.gov.gchq.palisade.user.service.request.AddUserRequest;
import uk.gov.gchq.palisade.user.service.request.GetUserRequest;

import java.util.concurrent.CompletableFuture;

public class MockUserService implements UserService {
    private static UserService mock = Mockito.mock(UserService.class);

    public static UserService getMock() {
        return mock;
    }

    public static void setMock(final UserService mock) {
        if (null == mock) {
            MockUserService.mock = Mockito.mock(UserService.class);
        }
        MockUserService.mock = mock;
    }

    @Override
    public CompletableFuture<User> getUser(final GetUserRequest request) {
        return mock.getUser(request);
    }

    @Override
    public CompletableFuture<Boolean> addUser(final AddUserRequest request) {
        return mock.addUser(request);
    }
}
