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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.gov.gchq.palisade.policy.ReflectiveTuple.Cache;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ReflectiveTupleTest {
    private static final String FIELD_X = "fieldX";
    private static final String FIELD_B = "fieldB";
    private static final String FIELD_A = "fieldA";
    private static final String METHOD_B = "methodB";

    private ReflectiveTuple testObj;

    @Before
    public void setUp() throws Exception {
        testObj = new ReflectiveTuple(new ExampleObj());
    }

    @Test
    public void shouldNotFindMissingField() throws Exception {
        try {
            testObj.get(FIELD_X);
            fail("Exception expected");
        } catch (RuntimeException e) {
            assertEquals(String.format(ReflectiveTuple.SELECTION_S_DOES_NOT_EXIST, FIELD_X), e.getMessage());
        }
    }

    @Test
    public void shouldNotFindMissingMethod() throws Exception {
        try {
            testObj.get("get" + FIELD_X);
            fail("Exception expected");
        } catch (RuntimeException e) {
            assertEquals(String.format(ReflectiveTuple.SELECTION_S_DOES_NOT_EXIST, "get" + FIELD_X), e.getMessage());
        }
    }

    @Test
    public void shouldFindPublicField() throws Exception {
        assertEquals("fa", testObj.get(FIELD_A));
    }

    @Test
    public void shouldNotFindPrivateField() throws Exception {
        try {
            testObj.get(FIELD_B);
            fail("Exception expected");
        } catch (RuntimeException e) {
            assertEquals(String.format(ReflectiveTuple.SELECTION_S_DOES_NOT_EXIST, FIELD_B), e.getMessage());
        }
    }

    @Test
    public void shouldFindPublicMethod() throws Exception {
        assertEquals("ma", testObj.get("methodA"));
    }

    @Test
    public void shouldNotFindPrivateMethod() throws Exception {
        try {
            testObj.get(METHOD_B);
            fail("Exception expected");
        } catch (RuntimeException e) {
            assertEquals(String.format(ReflectiveTuple.SELECTION_S_DOES_NOT_EXIST, METHOD_B), e.getMessage());
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotPut() throws Exception {
        testObj.put("", "");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotValues() throws Exception {
        testObj.values();
    }

    @Test
    public void shouldGetInOrderMethodGetIs() throws Exception {
        testObj = new ReflectiveTuple(new ExampleObj2());
        assertEquals("fa", testObj.get("valueA"));
        assertEquals("mb", testObj.get("valueB"));
        assertEquals("isc", testObj.get("valueC"));
    }

    @Test
    public void shouldUseCache() throws Exception {
        // Given
        final Cache<Field> fieldCache = mock(Cache.class);
        final Cache<Method> methodCache = mock(Cache.class);
        when(fieldCache.get(ExampleObj2.class, "valueA")).thenReturn(null);
        when(methodCache.get(ExampleObj2.class, "getValueB")).thenReturn(null);

        testObj = new ReflectiveTuple(new ExampleObj2(), fieldCache, methodCache);

        // When
        final Object valueA1 = testObj.get("valueA");
        final Object valueB1 = testObj.get("valueB");

        // Then
        assertEquals("fa", valueA1);
        assertEquals("mb", valueB1);
        verify(fieldCache, times(1)).put(eq(ExampleObj2.class), eq("valueA"), any(Field.class));
        verify(methodCache, times(1)).put(eq(ExampleObj2.class), eq("getValueB"), any(Method.class));

        // Given
        when(fieldCache.get(ExampleObj2.class, "valueA")).thenReturn(ExampleObj2.class.getField("valueAlt"));
        when(methodCache.get(ExampleObj2.class, "getValueB")).thenReturn(ExampleObj2.class.getMethod("getValueBAlt"));

        // When
        final Object valueA2 = testObj.get("valueA");
        final Object valueB2 = testObj.get("valueB");

        // Then
        assertEquals("falt", valueA2);
        assertEquals("mbAlt", valueB2);
        verify(fieldCache, times(2)).get(ExampleObj2.class, "valueA");
        verify(methodCache, times(2)).get(ExampleObj2.class, "getValueB");
    }

    @Test
    public void shouldPutAndGetFromCache() throws Exception {
        Cache cache = new Cache();

        final AccessibleObject get = cache.get(String.class, "string");
        Assert.assertNull(get);
        final Method toString = String.class.getMethod("toString");
        cache.put(String.class, "string", toString);
        final AccessibleObject get2 = cache.get(String.class, "string");
        Assert.assertEquals(toString, get2);
    }

    private class ExampleObj {
        public String fieldA = "fa";
        private String fieldB = "fb";
        private String methodA = "ma";
        private String methodB = "mb";

        public String getMethodA() {
            return methodA;
        }

        private String getMethodB() {
            return methodB;
        }

        public String deleteAll() throws IllegalAccessException {
            throw new IllegalAccessException("Should not invoke");
        }
    }

    private class ExampleObj2 {
        public String valueA = "fa";
        public String valueAlt = "falt";
        private String valueB = "fb";
        private String valueC = "fc";

        private String getValueA() {
            return "mA";
        }

        public String getValueB() {
            return "mb";
        }

        public String getValueBAlt() {
            return "mbAlt";
        }

        private String getValueC() {
            return "mc";
        }

        private String isValueA() {
            return "isA";
        }

        public String isValueB() {
            return "isb";
        }

        public String isValueC() {
            return "isc";
        }
    }
}


