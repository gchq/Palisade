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
package uk.gov.gchq.palisade.policy;

import uk.gov.gchq.koryphe.tuple.Tuple;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

/**
 * <p>
 * A {@code ReflectiveTuple} is a {@link Tuple} that wraps an object and treats
 * it as a {@link Tuple} by using reflection to call fields directly or via getters.
 * The field 'X' must either be public or have a public getter named getX or isX.
 * </p>
 * <p>
 * Reflection is expensive and this class should only be used for testing,
 * at small scales or as a temporary solution.
 * </p>
 */
// TODO this class has been copied from Koryphe and it should be deleted after the next Koryphe release.
public class ReflectiveTuple implements Tuple<String> {
    public static final String SELECTION_S_DOES_NOT_EXIST = "Selection: %s does not exist.";

    private static final Cache<Field> STATIC_FIELD_CACHE = new Cache<>();
    private static final Cache<Method> STATIC_METHOD_CACHE = new Cache<>();

    private final Object record;
    private final Cache<Field> fieldCache;
    private final Cache<Method> methodCache;

    /**
     * @param record object to wrap
     */
    public ReflectiveTuple(final Object record) {
        this(record, STATIC_FIELD_CACHE, STATIC_METHOD_CACHE);
    }

    /**
     * Construct a {@link ReflectiveTuple} with the given record and caches.
     *
     * @param record      the record to expose as a {@link Tuple}.
     * @param fieldCache  the cache to use for {@link Field}s.
     * @param methodCache the cache to use for {@link Method}s.
     */
    protected ReflectiveTuple(final Object record,
                              final Cache<Field> fieldCache,
                              final Cache<Method> methodCache) {
        this.record = record;
        this.fieldCache = fieldCache;
        this.methodCache = methodCache;
    }

    public Object getRecord() {
        return record;
    }

    @Override
    public Object get(final String reference) {
        Object selection;
        try {
            selection = invokeMethodGet(record, reference);
        } catch (final IllegalAccessException | NoSuchMethodException | InvocationTargetException ignored) {
            try {
                selection = invokeFieldGet(record, reference);
            } catch (final IllegalAccessException | NoSuchFieldException ignore) {
                throw new RuntimeException(String.format(SELECTION_S_DOES_NOT_EXIST, reference));
            }
        }
        return selection;
    }

    @Override
    public void put(final String reference, final Object value) {
        try {
            invokeMethodPut(record, reference, value);
        } catch (final IllegalAccessException | NoSuchMethodException | InvocationTargetException ignored) {
            try {
                invokeFieldPut(record, reference, value);
            } catch (final IllegalAccessException | NoSuchFieldException ignore) {
                throw new RuntimeException(String.format(SELECTION_S_DOES_NOT_EXIST, reference));
            }
        }
    }

    @Override
    public Iterable<Object> values() {
        throw new UnsupportedOperationException("This " + getClass().getSimpleName() + " does not support listing all values.");
    }

    private Object invokeFieldGet(final Object item, final String reference) throws IllegalAccessException, NoSuchFieldException {
        //invoked value can be null
        return getField(item.getClass(), reference).get(item);
    }

    private Object invokeMethodGet(final Object item, final String reference) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method;
        try {
            method = getGetMethod(item.getClass(), getPrefixRef("get", reference));
        } catch (final Exception e) {
            // ignore
            method = getGetMethod(item.getClass(), getPrefixRef("is", reference));
        }

        //invoked value can be null
        return method.invoke(item);
    }

    private void invokeFieldPut(final Object item, final String reference, final Object value) throws IllegalAccessException, NoSuchFieldException {
        getField(item.getClass(), reference).set(item, value);
    }

    private void invokeMethodPut(final Object item, final String reference, final Object value) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        getSetMethod(item.getClass(), getPrefixRef("set", reference)).invoke(item, value);
    }

    private Field getField(final Class<?> aClass, final String reference) throws NoSuchFieldException {
        Field rtn = fieldCache.get(aClass, reference);
        if (isNull(rtn)) {
            rtn = aClass.getField(reference);
            this.fieldCache.put(aClass, reference, rtn);
        }

        return rtn;
    }

    private Method getGetMethod(final Class<?> aClass, final String reference) throws NoSuchMethodException {
        Method rtn = methodCache.get(aClass, reference);
        if (isNull(rtn)) {
            rtn = aClass.getMethod(reference);
            if (0 != rtn.getParameterCount()) {
                throw new NoSuchMethodException("Getter " + reference + " should take no arguments but it takes " + rtn.getParameterCount());
            }
            this.methodCache.put(aClass, reference, rtn);
        }

        return rtn;
    }

    private Method getSetMethod(final Class<?> aClass, final String reference) throws NoSuchMethodException {
        Method rtn = methodCache.get(aClass, reference);
        if (isNull(rtn)) {
            rtn = aClass.getMethod(reference);
            if (1 != rtn.getParameterCount()) {
                throw new NoSuchMethodException("Setter " + reference + " should only take 1 argument but it takes " + rtn.getParameterCount());
            }
            this.methodCache.put(aClass, reference, rtn);
        }

        return rtn;
    }

    private String getPrefixRef(final String prefix, final String reference) {
        final boolean startsWithPrefix = reference.toLowerCase(Locale.getDefault()).startsWith(prefix);
        return startsWithPrefix ? reference : prefix + Character.toUpperCase(reference.charAt(0)) + reference.substring(1);
    }

    public static class Cache<T extends AccessibleObject> {
        private final Map<Class, Map<String, T>> cache = new HashMap<>();

        public T get(final Class<?> itemClass, final String reference) {
            requireNonNull(itemClass);
            requireNonNull(reference);

            T rtn = null;
            final Map<String, T> map = cache.get(itemClass);
            if (nonNull(map)) {
                rtn = map.get(reference);
            }
            return rtn;
        }

        public T put(final Class<?> itemClass, final String reference, final T accessibleObject) {
            Map<String, T> accessibleObjectMap = cache.get(itemClass);
            if (isNull(accessibleObjectMap)) {
                accessibleObjectMap = new HashMap<>();
                cache.put(itemClass, accessibleObjectMap);
            }
            return accessibleObjectMap.put(reference, accessibleObject);
        }
    }
}
