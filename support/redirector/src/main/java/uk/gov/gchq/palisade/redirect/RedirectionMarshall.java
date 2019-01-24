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

package uk.gov.gchq.palisade.redirect;

import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.service.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

public class RedirectionMarshall<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedirectionMarshall.class);

    private final Redirector<T> redirector;

    private final ThreadLocal<RedirectionResult<T>> recentRedirect = new ThreadLocal<>();

    public RedirectionMarshall(final Redirector<T> redirector) {
        requireNonNull(redirector, "redirector");
        this.redirector = redirector;
    }

    public Redirector<?> getRedirector() {
        return redirector;
    }

    public <S> T redirect(final S call) {
        //result is ignored
        //we should be able to retrieve the redirection result
        try {
            RedirectionResult<T> result = recentRedirect.get();
            if (isNull(result)) {
                throw new IllegalStateException("no redirection result is present, was a valid method call made via the object returned from createProxyFor() from this instance?");
            }
            LOGGER.debug("Redirection destination {}", result.get());
            return result.get();
        } finally {
            recentRedirect.remove();
        }
    }

    public T redirect(final Runnable call) {
        call.run();
        return redirect((Object) null);
    }

    private Object delegateRedirection(final Object proxy, final Method method, final Object... args) throws Throwable {
        //work out where to send this request
        RedirectionResult<T> result = redirector.redirectionFor(method, args);
        //stash this result
        recentRedirect.set(result);
        //Don't care about the actual method result
        return safeReturnTypeFor(method);
    }

    public static Object safeReturnTypeFor(final Method method) {
        Class<?> type = method.getReturnType();

        if (type.isPrimitive() && !type.equals(Void.TYPE)) {
            //default construct something from the string constructor present in all primitives except Void
            try {
                Class<?> wrapper = ClassUtils.primitiveToWrapper(type);
                if (type.equals(Character.TYPE)) {
                    return Character.valueOf((char) 0);
                }
                return wrapper.getDeclaredConstructor(String.class).newInstance("0");
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException("proxy redirect error, couldn't create primitive return type", e);
            }
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public <S extends Service> S createProxyFor(final Class<S> redirectClass) {
        requireNonNull(redirectClass, "redirectClass");
        if (!Service.class.isAssignableFrom(redirectClass)) {
            throw new IllegalArgumentException("class does not implement Service interface");
        }
        return (S) Proxy.newProxyInstance(redirectClass.getClassLoader(), new Class[]{redirectClass}, this::delegateRedirection);
    }
}
