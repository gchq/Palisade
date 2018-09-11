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
package uk.gov.gchq.palisade.cache.service.impl;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class BackingStoreStaticTest {

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowOnNullKey() {
        //Given null
        BackingStore.keyCheck(null);
        //Then - throw
        fail("exception expected");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowOnEmptyKey() {
        //Given empty
        BackingStore.keyCheck("");
        //Then - throw
        fail("exception expected");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowOnWhitespaceKey() {
        //Given whitespace
        BackingStore.keyCheck("    ");
        //Then - throw
        fail("exception expected");
    }

    @Test()
    public void shouldReturnSameKey() {
        //Given
        String key = "foo";
        //When
        String real = BackingStore.keyCheck(key);
        //Then
        assertEquals(key, real);
    }
}
