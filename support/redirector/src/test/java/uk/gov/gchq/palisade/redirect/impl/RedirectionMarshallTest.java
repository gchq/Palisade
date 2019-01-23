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

package uk.gov.gchq.palisade.redirect.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import uk.gov.gchq.palisade.cache.service.CacheService;
import uk.gov.gchq.palisade.cache.service.heart.HeartUtil;
import uk.gov.gchq.palisade.cache.service.request.AddCacheRequest;
import uk.gov.gchq.palisade.redirect.RedirectionMarshall;
import uk.gov.gchq.palisade.redirect.RedirectionResult;
import uk.gov.gchq.palisade.redirect.Redirector;
import uk.gov.gchq.palisade.redirect.exception.NoInstanceException;
import uk.gov.gchq.palisade.redirect.result.StringRedirectResult;
import uk.gov.gchq.palisade.service.Service;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

interface PrimitiveService extends Service {
    void voidMethod();

    boolean booleanMethod();

    byte byteMethod();

    char charMethod();

    short shortMethod();

    int intMethod();

    long longMethod();

    double doubleMethod();

    float floatMethod();

    Object objectMethod();
}

interface NotAService {
}

public class RedirectionMarshallTest {

    @Test
    public void shouldReturnSafeInstances() throws Exception {
        //Given - nothing
        //When
        //Then
        assertEquals(null, RedirectionMarshall.safeReturnTypeFor(PrimitiveService.class.getDeclaredMethod("voidMethod")));
        assertEquals(Boolean.FALSE, RedirectionMarshall.safeReturnTypeFor(PrimitiveService.class.getDeclaredMethod("booleanMethod")));
        assertEquals(Byte.valueOf((byte) 0), RedirectionMarshall.safeReturnTypeFor(PrimitiveService.class.getDeclaredMethod("byteMethod")));
        assertEquals(Character.valueOf((char) 0), RedirectionMarshall.safeReturnTypeFor(PrimitiveService.class.getDeclaredMethod("charMethod")));
        assertEquals(Short.valueOf((short) 0), RedirectionMarshall.safeReturnTypeFor(PrimitiveService.class.getDeclaredMethod("shortMethod")));
        assertEquals(Integer.valueOf(0), RedirectionMarshall.safeReturnTypeFor(PrimitiveService.class.getDeclaredMethod("intMethod")));
        assertEquals(Long.valueOf(0), RedirectionMarshall.safeReturnTypeFor(PrimitiveService.class.getDeclaredMethod("longMethod")));
        assertEquals(Double.valueOf(0), RedirectionMarshall.safeReturnTypeFor(PrimitiveService.class.getDeclaredMethod("doubleMethod")));
        assertEquals(Float.valueOf(0), RedirectionMarshall.safeReturnTypeFor(PrimitiveService.class.getDeclaredMethod("floatMethod")));
        assertEquals(null, RedirectionMarshall.safeReturnTypeFor(PrimitiveService.class.getDeclaredMethod("objectMethod")));
    }

    @Test(expected = IllegalStateException.class)
    public void throwOnNoResultPassed() {
        //Given - nothing
        RedirectionMarshall<?> test = new RedirectionMarshall<>(Mockito.mock(Redirector.class));
        //When
        test.redirect(new Object());
        //Then
        fail("exception expected");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwOnNonServiceInterface() {
        //Given
        RedirectionMarshall<?> test = new RedirectionMarshall<>(Mockito.mock(Redirector.class));
        //When
        test.createProxyFor((Class) NotAService.class);
        //Then
        fail("exception expected");
    }

    @Test
    public void shouldNotThrowErrorWhenRedirectingMethods() {
        //Given
        Redirector<String> mock = Mockito.mock(Redirector.class);
        when(mock.redirectionFor(any(),any())).thenReturn(new StringRedirectResult("test_destination"));

        RedirectionMarshall<String> marshall = new RedirectionMarshall<>(mock);
        PrimitiveService service = marshall.createProxyFor(PrimitiveService.class);

        //When
        //Then
        //FIXME assertEquals("test_destination", marshall.redirect(service.voidMethod()));
        assertEquals("test_destination", marshall.redirect(service.booleanMethod()));
        assertEquals("test_destination", marshall.redirect(service.byteMethod()));
        assertEquals("test_destination", marshall.redirect(service.charMethod()));
        assertEquals("test_destination", marshall.redirect(service.shortMethod()));
        assertEquals("test_destination", marshall.redirect(service.intMethod()));
        assertEquals("test_destination", marshall.redirect(service.longMethod()));
        assertEquals("test_destination", marshall.redirect(service.doubleMethod()));
        assertEquals("test_destination", marshall.redirect(service.floatMethod()));
        assertEquals("test_destination", marshall.redirect(service.objectMethod()));
    }

    //tests needed
    /*
      should give an accurate redirect
     */

}
