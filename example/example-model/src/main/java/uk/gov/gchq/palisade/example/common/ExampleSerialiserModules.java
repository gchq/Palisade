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

import com.fasterxml.jackson.databind.Module;

import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiserModules;

import java.util.Arrays;
import java.util.List;

public class ExampleSerialiserModules implements JSONSerialiserModules {
    @Override
    public List<Module> getModules() {
        System.out.println("In ExampleSerialiserModules");
        return Arrays.asList(CustomExampleUserSerializer.getModule());
    }
}

