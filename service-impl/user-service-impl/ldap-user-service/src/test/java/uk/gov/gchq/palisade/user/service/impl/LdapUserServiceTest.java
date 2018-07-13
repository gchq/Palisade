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

import com.google.common.collect.Sets;
import org.junit.Test;

import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.user.service.request.GetUserRequest;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.ldap.LdapContext;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class LdapUserServiceTest {
    @Test
    public void shouldFetchUserDetailsFromLdap() throws NamingException {
        // Given
        final UserId userId = new UserId("user01");
        final LdapContext ldapContext = mock(LdapContext.class);
        final String authsKey = "auths";
        final String rolesKey = "roles";
        final LdapUserService service = new LdapUserService(ldapContext, authsKey, rolesKey);
        final Attributes attributes = new BasicAttributes();
        attributes.put("auths", Sets.newHashSet("auth1", "auth2"));
        attributes.put("roles", Sets.newHashSet("role1", "role2"));

        given(ldapContext.getAttributes(userId.getId(), new String[]{authsKey, rolesKey})).willReturn(attributes);

        // When
        final User user = service.getUser(new GetUserRequest(userId)).join();

        // Then
        assertEquals(userId, user.getUserId());
        assertEquals(Sets.newHashSet("auth1", "auth2"), user.getAuths());
        assertEquals(Sets.newHashSet("role1", "role2"), user.getRoles());
    }
}
