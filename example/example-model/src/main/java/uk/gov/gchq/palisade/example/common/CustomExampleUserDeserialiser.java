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
package uk.gov.gchq.palisade.example.common;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class CustomExampleUserDeserialiser extends StdDeserializer<ExampleUser> {

    private static final long serialVersionUID = 1L;

    public CustomExampleUserDeserialiser() {
        this(ExampleUser.class);
    }

    @Override
    public ExampleUser deserialize(final com.fasterxml.jackson.core.JsonParser p, final DeserializationContext ctxt) throws IOException, com.fasterxml.jackson.core.JsonProcessingException {
        System.out.println("In CustomExampleUserDeserialiser.deserialise");
        return new ExampleUser();
    }

    protected CustomExampleUserDeserialiser(final Class<?> vc) {
        super(vc);
    }

}
