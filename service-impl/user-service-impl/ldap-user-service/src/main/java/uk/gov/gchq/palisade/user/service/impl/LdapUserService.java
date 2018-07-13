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

import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.user.service.UserService;
import uk.gov.gchq.palisade.user.service.request.AddUserRequest;
import uk.gov.gchq.palisade.user.service.request.GetUserRequest;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static java.util.Objects.requireNonNull;

/**
 * A LdapUserService is an implementation of a {@link UserService} that
 * connects to LDAP to lookup users. This implementation does not allow you
 * to add users.
 */
public class LdapUserService implements UserService {
    private final LdapContext context;
    private final String authsKey;
    private final String rolesKey;

    public LdapUserService(
            @JsonProperty("ldapConfigPath") final String ldapConfigPath,
            @JsonProperty("authsKey") final String authsKey,
            @JsonProperty("rolesKey") final String rolesKey)
            throws IOException, NamingException {
        requireNonNull(ldapConfigPath, "ldapConfigPath is required");
        requireNonNull(authsKey, "authsKey is required");
        requireNonNull(rolesKey, "rolesKey is required");
        this.context = createContext(ldapConfigPath);
        this.authsKey = authsKey;
        this.rolesKey = rolesKey;
    }

    public LdapUserService(final LdapContext context, final String authsKey, final String rolesKey) {
        requireNonNull(context, "LdapContext is required");
        requireNonNull(authsKey, "authsKey is required");
        requireNonNull(rolesKey, "rolesKey is required");
        this.context = context;
        this.authsKey = authsKey;
        this.rolesKey = rolesKey;
    }

    @Override
    public CompletableFuture<User> getUser(final GetUserRequest request) {
        requireNonNull(request);
        requireNonNull(request.getUserId());
        requireNonNull(request.getUserId().getId());

        return CompletableFuture.supplyAsync(() -> {
            final Attributes responseAttrs = fetchAttrs(request);
            return new User()
                    .userId(request.getUserId())
                    .auths(extractAttr(responseAttrs, authsKey))
                    .roles(extractAttr(responseAttrs, rolesKey));
        });
    }

    @Override
    public CompletableFuture<Boolean> addUser(final AddUserRequest request) {
        throw new UnsupportedOperationException("Users cannot be added to this " + getClass().getSimpleName());
    }

    protected Attributes fetchAttrs(final GetUserRequest request) {
        return fetchAttrs(request.getUserId().getId());
    }

    protected Attributes fetchAttrs(final String userId) {
        return fetchAttrs(userId, new String[]{authsKey, rolesKey});
    }

    protected Attributes fetchAttrs(final String userId, final String[] requestAttrs) {
        final Attributes responseAttrs;
        try {
            responseAttrs = context.getAttributes(userId, requestAttrs);
        } catch (NamingException e) {
            throw new RuntimeException("Unable to fetch attribute: " + Arrays.toString(requestAttrs), e);
        }
        if (null == responseAttrs) {
            throw new RuntimeException("Failed to fetch attributes: " + Arrays.toString(requestAttrs) + ". A null value was returned from LDAP.");
        }
        return responseAttrs;
    }

    protected Set<String> extractAttr(final Attributes attrs, final String name) {
        final Attribute attr = attrs.get(name);
        Set<String> results = new HashSet<>();
        NamingEnumeration<?> itr = null;
        try {
            itr = attr.getAll();
            while (itr.hasMore()) {
                final Object val = itr.next();
                if (val instanceof String) {
                    results.add(((String) val));
                } else if (val instanceof String[]) {
                    Collections.addAll(results, ((String[]) val));
                } else if (val instanceof Collection) {
                    for (Object valItem : ((Collection) val)) {
                        if (valItem instanceof String) {
                            results.add(((String) valItem));
                        } else {
                            throw new RuntimeException("Attribute " + name + " value is of an unknown type: " + val);
                        }
                    }
                } else {
                    throw new RuntimeException("Attribute " + name + " value is of an unknown type: " + val);
                }
            }
        } catch (final NamingException e) {
            throw new RuntimeException("Unable to process " + name + ": " + attr, e);
        } finally {
            if (null != itr) {
                try {
                    itr.close();
                } catch (final NamingException e) {
                    // ignore
                }
            }
        }
        return results;
    }

    protected LdapContext createContext(final String ldapConfigPath) throws IOException, NamingException {
        final Properties config = new Properties();
        config.load(Files.newInputStream(Paths.get(ldapConfigPath)));
        return new InitialLdapContext(config, null);
    }
}
