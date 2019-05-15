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
import uk.gov.gchq.palisade.redirect.RedirectionUtils;

import static org.junit.Assert.assertEquals;

public class RedirectionUtilsTest {

    @Test
    public void shouldReturnSafeInstances() throws Exception {
        //Given - nothing
        //When
        //Then
        assertEquals(null, RedirectionUtils.safeReturnTypeFor(PrimitiveService.class.getDeclaredMethod("voidMethod")));
        assertEquals(Boolean.FALSE, RedirectionUtils.safeReturnTypeFor(PrimitiveService.class.getDeclaredMethod("booleanMethod")));
        assertEquals(Byte.valueOf((byte) 0), RedirectionUtils.safeReturnTypeFor(PrimitiveService.class.getDeclaredMethod("byteMethod")));
        assertEquals(Character.valueOf((char) 0), RedirectionUtils.safeReturnTypeFor(PrimitiveService.class.getDeclaredMethod("charMethod")));
        assertEquals(Short.valueOf((short) 0), RedirectionUtils.safeReturnTypeFor(PrimitiveService.class.getDeclaredMethod("shortMethod")));
        assertEquals(Integer.valueOf(0), RedirectionUtils.safeReturnTypeFor(PrimitiveService.class.getDeclaredMethod("intMethod")));
        assertEquals(Long.valueOf(0), RedirectionUtils.safeReturnTypeFor(PrimitiveService.class.getDeclaredMethod("longMethod")));
        assertEquals(Double.valueOf(0), RedirectionUtils.safeReturnTypeFor(PrimitiveService.class.getDeclaredMethod("doubleMethod")));
        assertEquals(Float.valueOf(0), RedirectionUtils.safeReturnTypeFor(PrimitiveService.class.getDeclaredMethod("floatMethod")));
        assertEquals(null, RedirectionUtils.safeReturnTypeFor(PrimitiveService.class.getDeclaredMethod("objectMethod")));
    }
}
