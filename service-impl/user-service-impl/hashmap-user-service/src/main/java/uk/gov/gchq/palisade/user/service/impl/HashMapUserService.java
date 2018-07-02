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

import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.user.service.UserService;
import uk.gov.gchq.palisade.user.service.exception.NoSuchUserIdException;
import uk.gov.gchq.palisade.user.service.request.AddUserRequest;
import uk.gov.gchq.palisade.user.service.request.GetUserRequest;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A HashMapUserService is a simple implementation of a {@link UserService} that simply stores the users in a {@link
 * ConcurrentHashMap}. By default the map is static so it will be shared across the same JVM.
 */
public class HashMapUserService implements UserService {
    private static final Map<UserId, User> USERS = new ConcurrentHashMap<>();

    private final Map<UserId, User> users;

    public HashMapUserService() {
        this(true);
    }

    public HashMapUserService(final boolean useStatic) {
        if (useStatic) {
            users = USERS;
        } else {
            users = new ConcurrentHashMap<>();
        }
    }

    @Override
    public CompletableFuture<User> getUser(final GetUserRequest request) {
        Objects.requireNonNull(request);
        Objects.requireNonNull(request.getUserId());
        Objects.requireNonNull(request.getUserId().getId());
        User user = users.get(request.getUserId());

        CompletableFuture<User> userCompletion = CompletableFuture.completedFuture(user);
        if (user == null) {
            userCompletion.obtrudeException(new NoSuchUserIdException(request.getUserId().getId()));
        }
        return userCompletion;
    }

    @Override
    public CompletableFuture<Boolean> addUser(final AddUserRequest request) {
        Objects.requireNonNull(request);
        Objects.requireNonNull(request.getUser());
        Objects.requireNonNull(request.getUser().getUserId());
        Objects.requireNonNull(request.getUser().getUserId().getId());
        users.put(request.getUser().getUserId(), request.getUser());
        return CompletableFuture.completedFuture(true);
    }
}
