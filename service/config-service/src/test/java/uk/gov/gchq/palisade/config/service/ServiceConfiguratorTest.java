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
package uk.gov.gchq.palisade.config.service;

import org.junit.Test;
import org.mockito.Mockito;

import uk.gov.gchq.palisade.config.service.request.GetConfigRequest;
import uk.gov.gchq.palisade.exception.NoConfigException;
import uk.gov.gchq.palisade.service.Service;
import uk.gov.gchq.palisade.service.request.InitialConfig;

import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ServiceConfiguratorTest {

    public interface TestService extends Service {

    }

    public static class TestServiceImpl implements TestService {
    }

    public static class NoConfigureTestService implements TestService {
        @Override
        public void configure(InitialConfig config) throws NoConfigException {
            throw new NoSuchElementException("test");
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwOnNegativeTimeout() {
        //When
        new Configurator(new MockConfigurationService()).retrieveConfig(Optional.empty(), Duration.ofSeconds(-1));
        //Then
        fail("exception expected");
    }

    @Test(expected = IllegalStateException.class)
    public void throwClassCantBeConfigured() {
        //Given
        InitialConfig conf = new InitialConfig()
                .put(TestService.class.getCanonicalName(), NoConfigureTestService.class.getTypeName());

        //When
        TestService t = Configurator.createFromConfig(TestService.class, conf);

        //Then
        fail("exception expected");
    }

    @Test(expected = IllegalStateException.class)
    public void throwNoClassSpecified() {
        //Given
        InitialConfig conf = new InitialConfig();

        //When
        TestService t = Configurator.createFromConfig(TestService.class, conf);

        //Then
        fail("exception expected");
    }

    @Test
    public void shouldCreateInstance() {
        //Given
        InitialConfig conf = new InitialConfig()
                .put(TestService.class.getCanonicalName(), TestServiceImpl.class.getTypeName());

        //When
        TestService t = Configurator.createFromConfig(TestService.class, conf);

        //Then
        assertThat(t, is(instanceOf(TestServiceImpl.class)));
    }

    @Test(expected = NoConfigException.class)
    public void throwOnNoConfig() {
        //Given
        InitialConfigurationService mock = Mockito.mock(InitialConfigurationService.class);
        when(mock.get(any(GetConfigRequest.class))).thenThrow(NoConfigException.class);

        //When
        InitialConfig s = new Configurator(mock).retrieveConfig(Optional.of(TestService.class));

        //Then
        fail("exception expected");
    }

    @Test
    public void shouldGetConfig() {
        //Given
        InitialConfig cfg = new InitialConfig()
                .put(TestService.class.getTypeName(), TestServiceImpl.class.getTypeName());

        InitialConfigurationService mock = Mockito.mock(InitialConfigurationService.class);
        when(mock.get(any(GetConfigRequest.class))).thenReturn(CompletableFuture.completedFuture(cfg));

        //When
        InitialConfig ret = new Configurator(mock).retrieveConfig(Optional.of(TestService.class));

        //Then
        assertThat(ret, is(equalTo(cfg)));
        verify(mock).get(any(GetConfigRequest.class));
    }

    @Test(expected = NoConfigException.class)
    public void throwOnTimeout() {
        //Given
        InitialConfigurationService mock = Mockito.mock(InitialConfigurationService.class);
        //make a deliberately slow request
        when(mock.get(any(GetConfigRequest.class))).thenReturn(CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
            }
            throw new RuntimeException("deliberate fail");
        }));

        //When
        new Configurator(mock).retrieveConfig(Optional.empty(), Duration.ofMillis(10));

        //Then
        fail("exception expected");
    }
}
