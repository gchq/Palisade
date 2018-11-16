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

package uk.gov.gchq.palisade.jsonserialisation;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdKeySerializer;

import uk.gov.gchq.palisade.UserId;

import java.io.IOException;

public class UserIdKeySerialiser extends StdKeySerializer {
    public static SimpleModule getModule() {
        final SimpleModule module = new SimpleModule();
        module.addKeyDeserializer(UserId.class, new UserIdKeyDeserialiser());
        module.addKeySerializer(UserId.class, new UserIdKeySerialiser());
        return module;
    }

    @Override
    public void serialize(final Object value, final JsonGenerator g, final SerializerProvider provider) throws IOException {
        g.writeFieldName(new String(JSONSerialiser.serialise(value)));
    }
}
