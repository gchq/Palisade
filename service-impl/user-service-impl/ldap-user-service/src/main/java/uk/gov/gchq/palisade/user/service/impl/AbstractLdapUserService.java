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
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.user.service.UserService;
import uk.gov.gchq.palisade.user.service.request.AddUserRequest;
import uk.gov.gchq.palisade.user.service.request.GetUserRequest;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

/**
 * <p>
 * An {@code AbstractLdapUserService} is an implementation of a {@link UserService} that
 * connects to LDAP to lookup users.
 * </p>
 * <p>
 * To use this LDAP user service you will
 * need to extend this class and implement 3 methods:
 * <ul>
 * <li>
 * {@link #getAttributeNames()}
 * </li>
 * <li>
 * {@link #getAuths(UserId, Map, LdapContext)}
 * </li>
 * <li>
 * {@link #getRoles(UserId, Map, LdapContext)}
 * </li>
 * </ul>
 * </p>
 * <p>
 * This abstract implementation also includes a basic cache to reduce the number
 * of calls to LDAP. The default time to live for the cache is 24 hours. You
 * can change the time to live hours using the cacheTtlHours parameter. Please
 * note the cache and time to live value is static so it will be shared for
 * all instances in the same JVM.
 * </p>
 * <p>
 * This implementation does not allow you to add users.
 * </p>
 */
public abstract class AbstractLdapUserService implements UserService {
    public static final long CACHE_TTL_HOURS = 24L;
    public static final String[] ESCAPED_CHARS = new String[]{"\\", "#", "+", "<", ">", ";", "\"", "@"};
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractLdapUserService.class);
    private static Cache<UserId, User> userCache;

    private static long cacheTtlHours;
    private final String ldapConfigPath;

    protected final LdapContext context;

    /**
     * Constructs a {@link AbstractLdapUserService} with a given {@link LdapContext}.
     * The cache time to live with be set the default or previously set value.
     *
     * @param context the {@link LdapContext} for making calls to LDAP.
     */
    public AbstractLdapUserService(final LdapContext context) {
        this(context, null);
    }

    /**
     * Constructs a {@link AbstractLdapUserService} with a given {@link LdapContext} and cache time to live.
     * NOTE: if the cache has already been initialised then the time to live will NOT be updated.
     *
     * @param context       the {@link LdapContext} for making calls to LDAP.
     * @param cacheTtlHours the time to live in hours for the cache.
     */
    public AbstractLdapUserService(final LdapContext context, final Long cacheTtlHours) {
        requireNonNull(context, "ldap context is required");
        this.context = context;
        this.ldapConfigPath = null;
        initialiseCache(cacheTtlHours);
    }

    /**
     * <p>
     * Constructs a {@link AbstractLdapUserService} with a given path to {@link LdapContext} and cache time to live.
     * The cache time to live with be set the default or previously set value.
     * </p>
     *
     * @param ldapConfigPath the path to config for initialising {@link LdapContext} for making calls to LDAP. This can be a path to a file or a resource.
     * @throws IOException     if IO issues occur whilst loading the LDAP config.
     * @throws NamingException if a naming exception is encountered whilst constructing the LDAP context
     */
    public AbstractLdapUserService(final String ldapConfigPath) throws IOException, NamingException {
        this(ldapConfigPath, null);
    }

    /**
     * <p>
     * Constructs a {@link AbstractLdapUserService} with a given path to {@link LdapContext} and cache time to live.
     * NOTE: if the cache has already been initialised then the time to live will NOT be updated.
     * </p>
     *
     * @param ldapConfigPath the path to config for initialising {@link LdapContext} for making calls to LDAP. This can be a path to a file or a resource.
     * @param cacheTtlHours  the time to live in hours for the cache.
     * @throws IOException     if IO issues occur whilst loading the LDAP config.
     * @throws NamingException if a naming exception is encountered whilst constructing the LDAP context
     */
    public AbstractLdapUserService(
            @JsonProperty("ldapConfigPath") final String ldapConfigPath,
            @JsonProperty("cacheTtlHours") final Long cacheTtlHours)
            throws IOException, NamingException {
        requireNonNull(ldapConfigPath, "ldapConfigPath is required");
        this.ldapConfigPath = ldapConfigPath;
        this.context = createContext(ldapConfigPath);
        requireNonNull(context, "Unable to construct ldap context from: " + ldapConfigPath);

        initialiseCache(cacheTtlHours);
    }

    public static void clearCache() {
        if (null != userCache) {
            userCache.invalidateAll();
        }
    }

    @Override
    public CompletableFuture<User> getUser(final GetUserRequest request) {
        requireNonNull(request);

        final UserId userId = request.getUserId();
        requireNonNull(userId, "userId has not been set");
        requireNonNull(userId.getId(), "userId has not been set");

        final User cachedUser = userCache.getIfPresent(userId);
        if (null != cachedUser) {
            LOGGER.debug("User {} was in the cache.", userId);
            return CompletableFuture.completedFuture(cachedUser.clone());
        }

        LOGGER.debug("User {} was not in the cache. Fetching details from LDAP.", userId);

        return CompletableFuture.supplyAsync(() -> {
            final Set<String> auths;
            final Set<String> roles;
            try {
                final Map<String, Object> userAttrs = getAttributes(userId);
                auths = getAuths(userId, userAttrs, context);
                roles = getRoles(userId, userAttrs, context);
            } catch (final NamingException e) {
                throw new RuntimeException("Unable to get user from LDAP", e);
            }

            final User user = new User().userId(userId).auths(auths).roles(roles);
            userCache.put(userId.clone(), user.clone());
            return user;
        });
    }

    @Override
    public CompletableFuture<Boolean> addUser(final AddUserRequest request) {
        throw new UnsupportedOperationException("Adding users is not supported in this user service: " + getClass().getSimpleName());
    }

    /**
     * Returns an array of attribute names to be fetched from LDAP. This could contain
     * user auth attributes and user role attributes. This will avoid having
     * to make multiple calls to LDAP to look up different attributes.
     * If this returns null or an empty array then the LDAP request will not be made.
     *
     * @return the attributes to be fetched from LDAP.
     */
    protected abstract String[] getAttributeNames();

    /**
     * <p>
     * Gets the user auths from LDAP.
     * </p>
     * <p>
     * If possible the user auths should be extracted from the userAttrs parameter rather than making another call to LDAP.
     * </p>
     *
     * @param userId    the user ID
     * @param userAttrs the user attributes fetched from LDAP. This is populated with the attributes listed from {@link #getAttributeNames()}
     * @param context   the {@link LdapContext} for querying LDAP if required
     * @return the {@link Set} of user auths
     * @throws NamingException if a naming exception is encountered whilst interacting with LDAP
     */
    protected abstract Set<String> getAuths(final UserId userId, final Map<String, Object> userAttrs, final LdapContext context) throws NamingException;

    /**
     * <p>
     * Gets the user roles from LDAP.
     * </p>
     * <p>
     * If possible the user roles should be extracted from the userAttrs parameter rather than making another call to LDAP.
     * </p>
     *
     * @param userId    the user ID
     * @param userAttrs the user attributes fetched from LDAP. This is populated with the attributes listed from {@link #getAttributeNames()}
     * @param context   the {@link LdapContext} for querying LDAP if required
     * @return the {@link Set} of user roles
     * @throws NamingException if a naming exception is encountered whilst interacting with LDAP
     */
    protected abstract Set<String> getRoles(final UserId userId, final Map<String, Object> userAttrs, final LdapContext context) throws NamingException;

    protected LdapContext createContext(final String ldapConfigPath) throws IOException, NamingException {
        final Properties config = new Properties();
        if (new File(ldapConfigPath).exists()) {
            config.load(Files.newInputStream(Paths.get(ldapConfigPath)));
        } else {
            config.load(getClass().getResourceAsStream(ldapConfigPath));
        }
        return new InitialLdapContext(config, null);
    }

    protected void initialiseCache(final Long cacheTtlHours) {
        if (null == userCache) {
            AbstractLdapUserService.cacheTtlHours = null != cacheTtlHours ? cacheTtlHours : CACHE_TTL_HOURS;
            userCache = CacheBuilder.newBuilder()
                    .maximumSize(10000)
                    .expireAfterWrite(AbstractLdapUserService.cacheTtlHours, TimeUnit.HOURS)
                    .build();
        }
    }

    protected Map<String, Object> getAttributes(final UserId userId) throws NamingException {
        final Map<String, Object> attributes = new HashMap<>();
        final String[] requestAttrs = getAttributeNames();
        if (null != requestAttrs && requestAttrs.length > 0) {
            final Attributes userAttrs = context.getAttributes(escapeLdapSearchFilter(userId.getId()), requestAttrs);
            if (null != userAttrs) {
                for (final String requestAttr : requestAttrs) {
                    final Attribute attribute = userAttrs.get(requestAttr);
                    if (null != attribute) {
                        final NamingEnumeration<?> all = attribute.getAll();
                        if (all.hasMore()) {
                            attributes.put(requestAttr, all.next());
                        }
                    }
                }
            }
        }
        return attributes;
    }

    /**
     * Performs a basic search on LDAP using the userId.
     *
     * @param userId          the userId to search for
     * @param name            the name of the context to search
     * @param attrIdForUserId the attribute ID that is associated with the userId
     * @param attrs           the attributes to fetch from the LDAP search.
     * @return the attribute values
     * @throws NamingException if a naming exception is encountered
     */
    protected Set<Object> basicSearch(final UserId userId,
                                      final String name, final String attrIdForUserId,
                                      final String... attrs) throws NamingException {
        final NamingEnumeration<SearchResult> attrResults = context.search(
                name,
                new BasicAttributes(attrIdForUserId, escapeLdapSearchFilter(userId.getId())),
                attrs
        );

        final Set<Object> results = new HashSet<>();
        while (attrResults.hasMore()) {
            final SearchResult result = attrResults.next();
            final Attributes resultAttrs = result.getAttributes();
            if (null != resultAttrs) {
                final NamingEnumeration<? extends Attribute> all = resultAttrs.getAll();
                if (null != all) {
                    while (all.hasMore()) {
                        final Attribute next = all.next();
                        final Object nextValue = next.get();
                        if (null != nextValue) {
                            results.add(nextValue);
                        }
                    }
                }
            }
        }
        return results;
    }

    protected String escapeLdapSearchFilter(final String input) {
        String result = input;
        for (final String escapedChar : ESCAPED_CHARS) {
            result = result.replace(escapedChar, "\\" + escapedChar);
        }
        return result;
    }


    public long getCacheTTlHours() {
        return cacheTtlHours;
    }

    public String getLdapConfigPath() {
        return ldapConfigPath;
    }
}
