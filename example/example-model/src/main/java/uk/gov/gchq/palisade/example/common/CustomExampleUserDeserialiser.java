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

import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class CustomExampleUserDeserialiser extends StdDeserializer<ExampleUser> {

    private static final long serialVersionUID = 1L;

    public CustomExampleUserDeserialiser() {
        this(ExampleUser.class);
    }

    @Override
    public ExampleUser deserialize(final com.fasterxml.jackson.core.JsonParser jp, final DeserializationContext ctxt) throws IOException, com.fasterxml.jackson.core.JsonProcessingException {
        System.out.println("In CustomExampleUserDeserialiser.deserialise");
        ExampleUser exampleUser = new ExampleUser();

        long id = 0;
        String name = null;
        String[] languages = null;
        JsonToken currentToken = null;
        while ((currentToken = jp.nextValue()) != null) {
            switch (currentToken) {
                case VALUE_STRING:
                    switch (jp.getCurrentName()) {
                        case "name":
                            name = jp.getText();
                            break;
                        case "languages":


                            return exampleUser;
                    }
            }
        }

    protected CustomExampleUserDeserialiser( final Class<?> vc){
            super(vc);
        }

    }

//    @Override
//    public void serialize(final ExampleUser value, final JsonGenerator gen, final SerializerProvider provider) throws IOException {
//        //serialize Example User
//        gen.writeStartObject();
////        private UserId userId;
//        gen.writeStringField("id", value.getUserId().getId());
////        private Set<String> roles = new HashSet<>();
//        gen.writeFieldName("roles");
//        {
//            gen.writeStartArray();
//            for (String role : value.getRoles()) {
//                gen.writeObject(role);
//            }
//            gen.writeEndArray();
//        }
////        private Set<String> auths = new HashSet<>();
//        gen.writeFieldName("auths");
//        {
//            gen.writeStartArray();
//            for (String role : value.getAuths()) {
//                gen.writeObject(role);
//            }
//            gen.writeEndArray();
//        }
//
//        gen.writeFieldName("trainingCompleted");
//        {
//            gen.writeStartArray();
//            for (TrainingCourse trainingCourse : value.getTrainingCompleted()) {
//                gen.writeObject(trainingCourse.name());
//            }
//            gen.writeEndArray();
//        }
//        gen.writeEndObject();
//    }
