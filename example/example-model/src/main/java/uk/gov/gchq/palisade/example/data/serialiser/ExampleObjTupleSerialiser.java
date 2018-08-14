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
package uk.gov.gchq.palisade.example.data.serialiser;

import uk.gov.gchq.palisade.data.serialise.LineSerialiser;
import uk.gov.gchq.palisade.example.ExampleObjTuple;

public class ExampleObjTupleSerialiser extends LineSerialiser<ExampleObjTuple> {
    private final ExampleObjSerialiser serialiser = new ExampleObjSerialiser();

    @Override
    public String serialiseLine(final ExampleObjTuple obj) {
        return serialiser.serialiseLine(obj.getObj());
    }

    @Override
    public ExampleObjTuple deserialiseLine(final String line) {
        return new ExampleObjTuple(serialiser.deserialiseLine(line));
    }
}
