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

import org.junit.Test;

import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.cache.service.impl.HashMapBackingStore;
import uk.gov.gchq.palisade.cache.service.impl.SimpleCacheService;
import uk.gov.gchq.palisade.service.ServiceState;
import uk.gov.gchq.palisade.user.service.exception.NoSuchUserIdException;
import uk.gov.gchq.palisade.user.service.request.AddUserRequest;
import uk.gov.gchq.palisade.user.service.request.GetUserRequest;

import java.util.concurrent.CompletionException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class SimpleUserServiceTest {

    @Test
    public void shouldConfigureAndUseSharedData() {
        //Given
        User user = new User().userId("uid1").auths("test", "test2").roles("test_role");
        User user2 = new User().userId("uid2").auths("other_test").roles("role");
        SimpleUserService hms = new SimpleUserService();
        hms.cacheService(new SimpleCacheService().backingStore(new HashMapBackingStore(true)));
        hms.addUser(new AddUserRequest().user(user)).join();

        ServiceState con = new ServiceState();
        hms.recordCurrentConfigTo(con);

        //When
        SimpleUserService test = new SimpleUserService();
        test.applyConfigFrom(con);
        //add a user to the first service
        hms.addUser(new AddUserRequest().user(user2)).join();
        //both should be in the second service
        GetUserRequest getUserRequest1 = new GetUserRequest().userId(new UserId().id("uid1"));
        getUserRequest1.setOriginalRequestId("user1");
        GetUserRequest getUserRequest2 = new GetUserRequest().userId(new UserId().id("uid2"));
        getUserRequest2.setOriginalRequestId("user2");
        User actual1 = test.getUser(getUserRequest1).join();
        User actual2 = test.getUser(getUserRequest2).join();

        //Then
        assertThat(user, equalTo(actual1));
        assertThat(user2, equalTo(actual2));
    }

    @Test
    public void shouldSaveToCache() {
        //Given
        User user = new User().userId("uid1").auths("test", "test2").roles("test_role");
        SimpleUserService hms = new SimpleUserService();
        hms.cacheService(new SimpleCacheService().backingStore(new HashMapBackingStore(true)));
        hms.addUser(new AddUserRequest().user(user)).join();

        ServiceState con = new ServiceState();
        hms.recordCurrentConfigTo(con);

        //When
        SimpleUserService test = new SimpleUserService();
        test.applyConfigFrom(con);
        GetUserRequest getUserRequest = new GetUserRequest().userId(new UserId().id("uid1"));
        getUserRequest.setOriginalRequestId("uid1");
        User actual1 = test.getUser(getUserRequest).join();

        //Then
        assertThat(actual1, equalTo(user));
    }

    @Test(expected = NoSuchUserIdException.class)
    public void throwOnNonExistantUser() throws Throwable {
        //Given
        SimpleUserService hms = new SimpleUserService();
        hms.cacheService(new SimpleCacheService().backingStore(new HashMapBackingStore(false)));

        ServiceState con = new ServiceState();
        hms.recordCurrentConfigTo(con);

        //When
        SimpleUserService test = new SimpleUserService();
        test.applyConfigFrom(con);
        try {
            GetUserRequest getUserRequest = new GetUserRequest().userId(new UserId().id("uid1"));
            getUserRequest.setOriginalRequestId("uid1");
            User actual1 = test.getUser(getUserRequest).join();
        } catch (CompletionException e) {
            throw e.getCause();
        }
        //Then
        fail("exception expected");
    }
}