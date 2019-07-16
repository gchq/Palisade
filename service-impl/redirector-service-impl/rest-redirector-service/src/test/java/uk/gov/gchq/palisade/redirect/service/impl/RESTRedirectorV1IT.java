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

package uk.gov.gchq.palisade.redirect.service.impl;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.gov.gchq.palisade.cache.service.CacheService;
import uk.gov.gchq.palisade.cache.service.heart.Heartbeat;
import uk.gov.gchq.palisade.cache.service.impl.HashMapBackingStore;
import uk.gov.gchq.palisade.cache.service.impl.SimpleCacheService;
import uk.gov.gchq.palisade.redirect.service.RedirectionResult;
import uk.gov.gchq.palisade.redirect.service.exception.NoInstanceException;
import uk.gov.gchq.palisade.rest.EmbeddedHttpServer;
import uk.gov.gchq.palisade.service.Service;
import uk.gov.gchq.palisade.service.ServiceState;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

import static java.util.Objects.nonNull;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class RESTRedirectorV1IT {
    /**
     * Dummy service interface.
     */
    public interface DummyService extends Service {
        void serviceMethod(int a);

        int anotherMethod(String a);
    }

    /**
     * REST endpoint for a dummy service.
     */
    @Path("/")
    public static class RestDummyService implements DummyService {

        private final DummyService delegate;

        @Inject
        public RestDummyService(final DummyService delegate) {
            this.delegate = delegate;
        }

        @GET
        @Path("/serviceMethod/{a}")
        @Override
        public void serviceMethod(@PathParam("a") int a) {
            delegate.serviceMethod(a);
        }

        @GET
        @Path("/anotherMethod/{a}")
        @Override
        public int anotherMethod(@PathParam("a") String a) {
            return delegate.anotherMethod(a);
        }
    }

    /**
     * Extension to the simple redirector that stores the host name the request came from so we can retrieve it afterwards.
     */
    public static class TestSimpleRandomRedirector extends SimpleRandomRedirector {

        @Override
        public RedirectionResult<String> redirectionFor(final String host, final Method method, final Object... args) throws NoInstanceException {
            RedirectionResult<String> ret = super.redirectionFor(host, method, args);
            this.host = host;
            return ret;
        }

        public String host;

    }

    private static final String BASE_URL;

    static {
        String host = "";
        try {
            host = String.format("http://%s:8080", InetAddress.getLocalHost().getCanonicalHostName());
        } catch (UnknownHostException e) { }

        BASE_URL = host;
    }

    private static CacheService cache;
    private static Heartbeat beat;
    private static TestSimpleRandomRedirector redirector;
    private static EmbeddedHttpServer server;
    private static RESTRedirector<DummyService> restInstance;

    @BeforeClass
    public static void setup() throws IOException {
        cache = new SimpleCacheService().backingStore(new HashMapBackingStore(false));
        //start a dummy heart beat
        beat = new Heartbeat().serviceClass(DummyService.class).instanceName("test-instance").cacheService(cache);
        beat.start();
        //create a redirector
        redirector = (TestSimpleRandomRedirector) new TestSimpleRandomRedirector().cacheService(cache).redirectionClass(DummyService.class);
        //make the redirection server
        restInstance = new RESTRedirector<>(DummyService.class.getTypeName(), RestDummyService.class.getTypeName(), redirector, true);
        server = new EmbeddedHttpServer(BASE_URL, restInstance);
        server.startServer();
    }

    @AfterClass
    public static void tearDown() {
        if (nonNull(beat)) {
            beat.stop();
        }
        if (nonNull(server)) {
            server.stopServer();
        }
    }

    @Test
    public void shouldRedirectCorrectlyVoid() throws Exception {
        HttpURLConnection url = (HttpURLConnection) new URL(BASE_URL + "/serviceMethod/45").openConnection();
        try {
            //Given
            url.setInstanceFollowRedirects(false);

            //When
            url.connect();
            int responseCode = url.getResponseCode();
            String locationHeader = url.getHeaderField("Location");

            //Then
            assertThat(locationHeader, is(notNullValue()));
            assertThat(responseCode, is(equalTo(307)));
            assertThat(locationHeader, is(equalTo("http://test-instance:8080/serviceMethod/45")));
        } finally {
            url.disconnect();
        }
    }

    @Test
    public void shouldRedirectCorrectlyInt() throws Exception {
        HttpURLConnection url = (HttpURLConnection) new URL(BASE_URL + "/anotherMethod/test").openConnection();
        try {
            //Given
            url.setInstanceFollowRedirects(false);

            //When
            url.connect();
            int responseCode = url.getResponseCode();
            String locationHeader = url.getHeaderField("Location");

            //Then
            assertThat(locationHeader, is(notNullValue()));
            assertThat(responseCode, is(equalTo(307)));
            assertThat(locationHeader, is(equalTo("http://test-instance:8080/anotherMethod/test")));
        } finally {
            url.disconnect();
        }
    }

    @Test
    public void shouldProduce404() throws Exception {
        HttpURLConnection url = (HttpURLConnection) new URL(BASE_URL + "/does/not/exist").openConnection();
        try {
            //Given
            url.setInstanceFollowRedirects(false);

            //When
            url.connect();
            int responseCode = url.getResponseCode();

            //Then
            assertThat(responseCode, is(equalTo(404)));
        } finally {
            url.disconnect();
        }
    }

    @Test
    public void shouldSetHostNameCorrectly() throws Exception {
        HttpURLConnection url = (HttpURLConnection) new URL(BASE_URL + "/does/not/exist").openConnection();
        try {
            //Given
            url.setInstanceFollowRedirects(false);

            //When
            url.connect();

            //Then
            assertThat(redirector.host, either(is(equalTo(InetAddress.getLocalHost().getCanonicalHostName()))).or(is(equalTo("localhost"))));
        } finally {
            url.disconnect();
        }
    }

    @Test
    public void shouldConfigureCorrectly() throws Exception {
        //Given
        ServiceState state = new ServiceState();

        //When
        restInstance.recordCurrentConfigTo(state);

        //deliberately configure badly
        RESTRedirector<?> recoveredInstance = new RESTRedirector(Service.class.getTypeName(), Service.class.getTypeName(), new SimpleRandomRedirector(), true);
        recoveredInstance.applyConfigFrom(state);

        //Then
        assertEquals(restInstance.getRedirectionClass(), recoveredInstance.getRedirectionClass());
        assertEquals(restInstance.getRestImplementationClass(), recoveredInstance.getRestImplementationClass());
    }
}
