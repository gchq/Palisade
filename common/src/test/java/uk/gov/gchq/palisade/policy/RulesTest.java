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

import org.junit.Before;
import org.junit.Test;

import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.palisade.util.JsonAssert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RulesTest {

    private Rules<String> rules;
    private byte[] json;

    @Before
    public void setUp() throws Exception {
        rules = new Rules<String>()
                .message("Age off and visibility filtering")
                .rule("ageOffRule", new TestRule()
                );
        json = JSONSerialiser.serialise(rules, true);
    }

    @Test
    public void shouldNotEquals() throws Exception {
        //given
        final Rules<String> one = new Rules<>();
        one.rule("one", new TestRule());

        final Rules<String> two = new Rules<>();
        two.rule("two", new TestRule());

        //then
        assertFalse(one.equals(two));
    }

    @Test
    public void shouldEquals() throws Exception {
        final Rules<String> one1 = new Rules<>();
        one1.rule("one", new TestRule());

        final Rules<String> one2 = new Rules<>();
        one2.rule("one", new TestRule());

        assertTrue(one1.equals(one2));
    }

    @Test
    public void shouldSerialiseToEqualObject() throws Exception {
        Rules deserialise = JSONSerialiser.deserialise(json, Rules.class);
        final String thisSerialise = new String(JSONSerialiser.serialise(this.rules, true));
        final String thatSerialise = new String(JSONSerialiser.serialise(deserialise, true));

        assertEquals(thisSerialise, thatSerialise);

        JsonAssert.assertEquals(rules, deserialise);
    }

    @Test
    public void shouldSerialiseTo() throws Exception {
        final String text = String.format("{%n" +
                "  \"message\" : \"Age off and visibility filtering\",%n" +
                "  \"rules\" : {%n" +
                "    \"ageOffRule\" : {%n" +
                "      \"class\" : \"uk.gov.gchq.palisade.policy.TestRule\"%n" +
                "    }%n" +
                "  }%n" +
                "}");

        assertEquals(text, new String(json));
    }

    @Test
    public void shouldDeserialiseText() throws Exception {
        final String text = String.format("{%n" +
                "  \"message\" : \"Age off and visibility filtering\",%n" +
                "  \"rules\" : {%n" +
                "    \"ageOffRule\" : {%n" +
                "      \"class\" : \"uk.gov.gchq.palisade.policy.TestRule\"%n" +
                "    }%n" +
                "  }%n" +
                "}");

        final Rules deserialise = JSONSerialiser.deserialise(text, Rules.class);
        JsonAssert.assertEquals(rules, deserialise);

    }
}
