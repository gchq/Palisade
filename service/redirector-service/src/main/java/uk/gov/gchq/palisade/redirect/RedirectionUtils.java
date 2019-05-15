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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static java.util.Objects.requireNonNull;

/**
 * Utility methods.
 */
public final class RedirectionUtils {
    private RedirectionUtils() {
    }

    /**
     * Generate a dummy return object for the given method. If the return type is void or an object, then {@code null} is returned,
     * otherwise for primitive types, the appropriate wrapper type is generated and initialised with a 0 value. This is primarily used
     * by {@link RedirectionMarshall} to allow proxy methods to complete safely.
     *
     * @param method the method to find a return type for
     * @return a dummy object
     */
    public static Object safeReturnTypeFor(final Method method) {
        requireNonNull(method, "method");
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
}
