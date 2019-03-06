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
package uk.gov.gchq.palisade;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;

public class ContextTest {

    private Context testObject;

    @Before
    public void setUp() throws Exception {
        testObject = new Context().purpose("purpose1");
    }

    @Test
    public void shouldJsonSerialise() throws Exception {
        //when
        final byte[] serialise = JSONSerialiser.serialise(testObject, true);
        final Context deserialise = JSONSerialiser.deserialise(serialise, Context.class);
        //then
        Assert.assertEquals(testObject, deserialise);
    }

    @Test
    public void shouldHistoricalJsonSerialise() throws Exception {
        //when
        final byte[] serialise = JSONSerialiser.serialise(testObject, true);
        Context deserialise = JSONSerialiser.deserialise(
                "{\n" +
                "  \"contents\" : {\n" +
                "    \"purpose\" : \"purpose1\"\n" +
                "  }\n" +
                "}", Context.class);
        //then
        Assert.assertEquals(new String(serialise), testObject, deserialise);
    }
}