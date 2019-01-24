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

import org.junit.Test;
import org.mockito.Mockito;

import uk.gov.gchq.palisade.redirect.RedirectionMarshall;
import uk.gov.gchq.palisade.redirect.Redirector;
import uk.gov.gchq.palisade.redirect.result.StringRedirectResult;
import uk.gov.gchq.palisade.service.Service;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

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

interface SimpleService extends Service {
    boolean method(int a);

    boolean method(int a, double b);
}

interface NotAService {
}

public class RedirectionMarshallTest {

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
        when(mock.redirectionFor(any(), any())).thenReturn(new StringRedirectResult("test_destination"));

        RedirectionMarshall<String> marshall = new RedirectionMarshall<>(mock);
        PrimitiveService service = marshall.createProxyFor(PrimitiveService.class);

        //When
        //Then
        assertEquals("test_destination", marshall.redirect(() -> service.voidMethod()));
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

    @Test
    public void shouldAccuratelyRedirect() {
        //Given
        Redirector<String> mock = Mockito.mock(Redirector.class);
        when(mock.redirectionFor(any(), anyInt())).thenReturn(new StringRedirectResult("single_param_dest"));
        when(mock.redirectionFor(any(), anyInt(), anyDouble())).thenReturn(new StringRedirectResult("double_param_dest"));

        RedirectionMarshall<String> marshall = new RedirectionMarshall<>(mock);
        SimpleService service = marshall.createProxyFor(SimpleService.class);

        //When
        //Then
        assertEquals("single_param_dest", marshall.redirect(service.method(5)));
        assertEquals("double_param_dest", marshall.redirect(service.method(5, 6.0d)));
    }
}
