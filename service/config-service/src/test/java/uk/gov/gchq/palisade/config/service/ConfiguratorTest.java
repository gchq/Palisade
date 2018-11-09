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

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import sun.security.krb5.Config;
import uk.gov.gchq.palisade.config.service.request.GetConfigRequest;
import uk.gov.gchq.palisade.exception.NoConfigException;
import uk.gov.gchq.palisade.service.Service;
import uk.gov.gchq.palisade.service.request.ServiceConfiguration;

import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.PatternSyntaxException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConfiguratorTest {

    public interface TestService extends Service {
    }

    public static class TestServiceImpl implements TestService {
    }

    public static class NoConfigureTestService implements TestService {
        @Override
        public void applyConfigFrom(ServiceConfiguration config) throws NoConfigException {
            throw new NoSuchElementException("test");
        }
    }

    private static ServiceConfiguration testConfig;
    private static final String KEY1 = "key1";
    private static final String KEY2 = "key2";
    private static final String VALUE1 = "value1";
    private static final String VALUE2 = "value2";
    private static final String VALUE3 = "value3";


    @BeforeClass
    public static void setupConfig() {
        testConfig = new ServiceConfiguration();
        testConfig.put(KEY1, VALUE1);
        testConfig.put(KEY2, VALUE2);
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
        ServiceConfiguration conf = new ServiceConfiguration()
                .put(TestService.class.getTypeName(), NoConfigureTestService.class.getTypeName());

        //When
        TestService t = Configurator.createFromConfig(TestService.class, conf);

        //Then
        fail("exception expected");
    }

    @Test(expected = IllegalStateException.class)
    public void throwNoClassSpecified() {
        //Given
        ServiceConfiguration conf = new ServiceConfiguration();

        //When
        TestService t = Configurator.createFromConfig(TestService.class, conf);

        //Then
        fail("exception expected");
    }

    @Test
    public void shouldCreateInstance() {
        //Given
        ServiceConfiguration conf = new ServiceConfiguration()
                .put(TestService.class.getTypeName(), TestServiceImpl.class.getTypeName());

        //When
        TestService t = Configurator.createFromConfig(TestService.class, conf);

        //Then
        assertThat(t, is(instanceOf(TestServiceImpl.class)));
    }

    @Test(expected = NoConfigException.class)
    public void throwOnNoConfig() {
        //Given
        ConfigurationService mock = Mockito.mock(ConfigurationService.class);
        when(mock.get(any(GetConfigRequest.class))).thenThrow(NoConfigException.class);

        //When
        ServiceConfiguration s = new Configurator(mock).retrieveConfig(Optional.of(TestService.class));

        //Then
        fail("exception expected");
    }

    @Test
    public void shouldGetConfig() {
        //Given
        ServiceConfiguration cfg = new ServiceConfiguration()
                .put(TestService.class.getTypeName(), TestServiceImpl.class.getTypeName());

        ConfigurationService mock = Mockito.mock(ConfigurationService.class);
        when(mock.get(any(GetConfigRequest.class))).thenReturn(CompletableFuture.completedFuture(cfg));

        //When
        ServiceConfiguration ret = new Configurator(mock).retrieveConfig(Optional.of(TestService.class));

        //Then
        assertThat(ret, is(equalTo(cfg)));
        verify(mock).get(any(GetConfigRequest.class));
    }

    @Test(expected = NoConfigException.class)
    public void throwOnTimeout() {
        //Given
        ConfigurationService mock = Mockito.mock(ConfigurationService.class);
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

    @Test(expected = PatternSyntaxException.class)
    public void throwOnIllegalPattern() {
        //Given - nothing
        //When
        Configurator.applyOverrides(testConfig, "[[[["); //illegal regex
        //Then
        fail("exception expected");
    }

    @Test
    public void shouldBeSameWhenNoneMatch() {
        //Given - nothing
        //When
        ServiceConfiguration actual = Configurator.applyOverrides(testConfig, "no match");
        //Then
        assertEquals(testConfig.getConfig().size(), actual.getConfig().size());
        assertEquals(testConfig, actual);
    }

    @Test
    public void shouldBeSameWhenPropNotPresent() {
        //Given
        System.clearProperty(KEY1);
        //When
        ServiceConfiguration actual = Configurator.applyOverrides(testConfig, KEY1);
        //Then
        assertEquals(testConfig.getConfig().size(), actual.getConfig().size());
        assertEquals(testConfig, actual);
    }

    @Test
    public void shouldChangeWhenPresent() {
        //Given
        System.setProperty(KEY2, VALUE3);
        ServiceConfiguration expected = new ServiceConfiguration();
        expected.put(KEY1, VALUE1);
        expected.put(KEY2, VALUE3);
        //When
        ServiceConfiguration actual = Configurator.applyOverrides(testConfig, KEY2);
        System.clearProperty(KEY2);
        //Then
        assertEquals(expected.getConfig().size(), actual.getConfig().size());
        assertEquals(expected, actual);
    }

    @Test
    public void shouldBeSameWhenPropPresentNoneMatch() {
        //Given
        System.setProperty(KEY2, VALUE3);
        //When
        ServiceConfiguration actual = Configurator.applyOverrides(testConfig, KEY1);
        System.clearProperty(KEY2);
        //Then
        assertEquals(testConfig.getConfig().size(), actual.getConfig().size());
        assertEquals(testConfig, actual);
    }

    @Test
    public void shouldBeSameWithEmptyOverrides() {
        //Given
        System.setProperty(KEY1, VALUE3);
        //When
        ServiceConfiguration actual = Configurator.applyOverrides(testConfig, "");
        System.clearProperty(KEY1);
        //Then
        assertEquals(testConfig.getConfig().size(), actual.getConfig().size());
        assertEquals(testConfig, actual);
    }

    @Test
    public void shouldChangeOnWildcard() {
        //Given
        System.setProperty(KEY2, VALUE3);
        ServiceConfiguration expected = new ServiceConfiguration();
        expected.put(KEY1, VALUE1);
        expected.put(KEY2, VALUE3);
        //When
        ServiceConfiguration actual = Configurator.applyOverrides(testConfig, KEY2);
        System.clearProperty(KEY2);
        //Then
        assertEquals(expected.getConfig().size(), actual.getConfig().size());
        assertEquals(expected, actual);
    }
}
