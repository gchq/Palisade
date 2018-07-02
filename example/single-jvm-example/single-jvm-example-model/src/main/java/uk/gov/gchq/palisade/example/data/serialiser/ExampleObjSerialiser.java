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

import uk.gov.gchq.palisade.data.serialise.BytesSerialiser;
import uk.gov.gchq.palisade.example.ExampleObj;

public class ExampleObjSerialiser implements BytesSerialiser<ExampleObj> {
    @Override
    public byte[] serialise(final ExampleObj object) {
        return (object.getProperty() + "," + object.getVisibility() + "," + object.getTimestamp()).getBytes();
    }

    @Override
    public ExampleObj deserialise(final byte[] bytes) {
        final String str = new String(bytes);
        final String[] parts = str.split(",");
        return new ExampleObj(parts[0], parts[1], Long.parseLong(parts[2]));
    }
}
