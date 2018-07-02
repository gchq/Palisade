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

package uk.gov.gchq.palisade.user.service;


import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.service.Service;
import uk.gov.gchq.palisade.service.request.Request;
import uk.gov.gchq.palisade.user.service.exception.NoSuchUserIdException;
import uk.gov.gchq.palisade.user.service.request.AddUserRequest;
import uk.gov.gchq.palisade.user.service.request.GetUserRequest;

import java.util.concurrent.CompletableFuture;

/**
 * <p>
 * The core API for the user service.
 * </p>
 * <p>
 * The responsibility of the user service is to maintain the mapping between currently active user IDs and the users
 * they correspond to. Each user has a given ID which can be added to the user service and retrieved later by the
 * client.
 * </p>
 * <p>
 * <strong>Please note that it is not the responsibility of any {@link UserService} implementation to provide the
 * authentication of individual users, or to maintain a database of 'Palisade' users.</strong> The actual authentication
 * of users should provided by an external service outside of Palisade. For example, this could be via a centralised PKI
 * service or by a SASL/Kerberos implementation.
 * </p>
 */
public interface UserService extends Service {
    /**
     * Look up a user by their ID. The request contains the {@link uk.gov.gchq.palisade.UserId} to lookup from the
     * {@link UserService}. If the requested {@link uk.gov.gchq.palisade.UserId} doesn't exist in this {@link
     * UserService} then an exception will be thrown.
     *
     * @param request specifying the user ID to look up
     * @return a {@link CompletableFuture} which will be fulfilled with the user details
     * @throws NoSuchUserIdException if the contained {@link uk.gov.gchq.palisade.UserId} could not be found
     */
    CompletableFuture<User> getUser(final GetUserRequest request) throws NoSuchUserIdException;

    /**
     * Adds the contained user to the {@link UserService}. The given request will contain the {@link User} which
     * should be fully populated with all the necessary roles and justifications.
     *
     * @param request the request specifying the user with details to add
     * @return a {@link CompletableFuture} which will complete as ${@code true} once the user has been added
     */
    CompletableFuture<Boolean> addUser(final AddUserRequest request);

    @Override
    default CompletableFuture<?> process(final Request request) {
        if (request instanceof GetUserRequest) {
            return getUser((GetUserRequest) request);
        }

        if (request instanceof AddUserRequest) {
            addUser((AddUserRequest) request);
            return null;
        }

        return Service.super.process(request);
    }
}
