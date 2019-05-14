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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;

import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.user.service.request.GetUserRequest;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class AbstractLdapUserServiceTest {

    @Before
    public void before() {
        AbstractLdapUserService.clearCache();
    }

    @Test
    public void shouldFetchUserDetailsFromLdap() throws NamingException {
        // Given
        final AbstractLdapUserService mock = mock(AbstractLdapUserService.class);
        final UserId userId = new UserId().id("user#01");
        final LdapContext context = mock(LdapContext.class);

        final String[] attrNames = {"roles", "auths"};
        final Set<String> auths = Sets.newHashSet("auth1", "auth2");
        final Set<String> roles = Sets.newHashSet("role1", "role2");

        final Attributes requestAttrs = new BasicAttributes();
        requestAttrs.put("auths", auths);
        requestAttrs.put("roles", roles);

        final Map<String, Object> userAttrs = new HashMap<>();
        userAttrs.put("auths", auths);
        userAttrs.put("roles", roles);

        given(mock.getAttributeNames()).willReturn(attrNames);
        given(context.getAttributes("user\\#01", attrNames)).willReturn(requestAttrs);
        given(mock.getAuths(userId, userAttrs, context)).willReturn(auths);
        given(mock.getRoles(userId, userAttrs, context)).willReturn(roles);

        final MockLdapUserService service = new MockLdapUserService(context);
        service.setMock(mock);

        // When
        GetUserRequest getUserRequest = new GetUserRequest().userId(userId);
        getUserRequest.setOriginalRequestId(new RequestId().id("TEST shouldFetchUserDetailsFromLdap"));
        final User user = service.getUser(getUserRequest).join();

        // Then
        verify(context, times(1)).getAttributes("user\\#01", attrNames);
        assertEquals(userId, user.getUserId());
        assertEquals(auths, user.getAuths());
        assertEquals(roles, user.getRoles());
    }

    @Test
    public void shouldFetchUserDetailsFromCache() throws NamingException {
        // Given
        final AbstractLdapUserService mock = mock(AbstractLdapUserService.class);
        final UserId userId = new UserId().id("user01");
        final LdapContext context = mock(LdapContext.class);

        final String[] attrNames = {"roles", "auths"};
        final Set<String> auths = Sets.newHashSet("auth1", "auth2");
        final Set<String> roles = Sets.newHashSet("role1", "role2");

        final Attributes requestAttrs = new BasicAttributes();
        requestAttrs.put("auths", auths);
        requestAttrs.put("roles", roles);

        final Map<String, Object> userAttrs = new HashMap<>();
        userAttrs.put("auths", auths);
        userAttrs.put("roles", roles);

        given(mock.getAttributeNames()).willReturn(attrNames);
        given(context.getAttributes("user01", attrNames)).willReturn(requestAttrs);
        given(mock.getAuths(userId, userAttrs, context)).willReturn(auths);
        given(mock.getRoles(userId, userAttrs, context)).willReturn(roles);

        final MockLdapUserService service = new MockLdapUserService(context);
        service.setMock(mock);

        // When
        GetUserRequest getUserRequest1 = new GetUserRequest().userId(userId);
        getUserRequest1.setOriginalRequestId(new RequestId().id("test user1"));
        GetUserRequest getUserRequest2 = new GetUserRequest().userId(userId);
        getUserRequest2.setOriginalRequestId(new RequestId().id("test user2"));
        final User user1 = service.getUser(getUserRequest1).join();
        final User user2 = service.getUser(getUserRequest2).join();

        // Then
        assertEquals(userId, user1.getUserId());
        assertEquals(auths, user1.getAuths());
        assertEquals(roles, user1.getRoles());
        assertEquals(user1, user2);
        verify(context, times(1)).getAttributes("user01", attrNames);
        // Check user objects have been cloned
        assertNotSame(user1, user2);
    }

    @Test
    public void shouldPerformABasicSearch() throws NamingException {
        // Given
        final AbstractLdapUserService mock = mock(AbstractLdapUserService.class);
        final UserId userId = new UserId().id("user01");
        final LdapContext context = mock(LdapContext.class);

        final MockLdapUserService service = new MockLdapUserService(context);
        final String searchBase = "base";
        final String attrIdForUserId = "userId";
        final String[] requestAttrs = {"attr1", "attr2"};
        service.setMock(mock);

        final Attributes searchResult1Attrs = new BasicAttributes();
        final Set<String> attr1_1 = Sets.newHashSet("auth1", "auth2");
        final int attr1_2 = 10;
        searchResult1Attrs.put("attr1_1", attr1_1);
        searchResult1Attrs.put("attr1_2", attr1_2);

        final Attributes searchResult2Attrs = new BasicAttributes();
        final Long attr2_1 = 5L;
        final String attr2_2 = "attr2_2";
        searchResult1Attrs.put("attr2_1", attr2_1);
        searchResult1Attrs.put("attr2_2", attr2_2);

        final SearchResult searchResult1 = new SearchResult("key1", "value1", searchResult1Attrs);
        final SearchResult searchResult2 = new SearchResult("key2", "value2", searchResult2Attrs);

        Iterator<SearchResult> itr = Arrays.asList(searchResult1, searchResult2).iterator();
        final NamingEnumeration<SearchResult> responseAttrs = new NamingEnumeration<SearchResult>() {
            @Override
            public SearchResult next() throws NamingException {
                return itr.next();
            }

            @Override
            public boolean hasMore() throws NamingException {
                return itr.hasNext();
            }

            @Override
            public void close() throws NamingException {
            }

            @Override
            public boolean hasMoreElements() {
                return itr.hasNext();
            }

            @Override
            public SearchResult nextElement() {
                return itr.next();
            }
        };

        given(context.search(searchBase,
                new BasicAttributes(attrIdForUserId, userId.getId()),
                requestAttrs)
        ).willReturn(responseAttrs);

        // When
        final Set<Object> results = service.basicSearch(userId, searchBase, attrIdForUserId, requestAttrs);

        // Then
        verify(context, times(1)).search(searchBase,
                new BasicAttributes(attrIdForUserId, userId.getId()),
                requestAttrs);
        final Set<Object> expectedResults = Sets.newHashSet(attr1_1, attr1_2, attr2_1, attr2_2);
        assertEquals(expectedResults, results);
    }

    @Test
    public void shouldEscapeCharacters() throws NamingException {
        // Given
        final AbstractLdapUserService mock = mock(AbstractLdapUserService.class);
        final LdapContext context = mock(LdapContext.class);

        final MockLdapUserService service = new MockLdapUserService(context);
        service.setMock(mock);

        final String input = "test input: " + Stream.of(AbstractLdapUserService.ESCAPED_CHARS)
                .collect(Collectors.joining());

        // When
        final String result = service.formatInput(input);

        // Then
        final String expectedResult = "test input: " + Stream.of(AbstractLdapUserService.ESCAPED_CHARS)
                .map(t -> "\\" + t)
                .collect(Collectors.joining());
        assertEquals(expectedResult, result);
    }

    public static final class MockLdapUserService extends AbstractLdapUserService {
        private AbstractLdapUserService mock;

        public MockLdapUserService(final LdapContext context) {
            super(context);
        }

        public MockLdapUserService(final LdapContext context, final Long cacheTtlHours) {
            super(context, cacheTtlHours);
        }

        public MockLdapUserService(final String ldapConfigPath) throws IOException, NamingException {
            super(ldapConfigPath);
        }

        public MockLdapUserService(@JsonProperty("ldapConfigPath") final String ldapConfigPath, @JsonProperty("cacheTtlHours") final Long cacheTtlHours) throws IOException, NamingException {
            super(ldapConfigPath, cacheTtlHours);
        }

        @Override
        protected String[] getAttributeNames() {
            return mock.getAttributeNames();
        }

        @Override
        protected Set<String> getAuths(final UserId userId, final Map<String, Object> userAttrs, final LdapContext context) throws NamingException {
            return mock.getAuths(userId, userAttrs, context);
        }

        @Override
        protected Set<String> getRoles(final UserId userId, final Map<String, Object> userAttrs, final LdapContext context) throws NamingException {
            return mock.getRoles(userId, userAttrs, context);
        }

        public void setMock(final AbstractLdapUserService mock) {
            this.mock = mock;
        }
    }
}
