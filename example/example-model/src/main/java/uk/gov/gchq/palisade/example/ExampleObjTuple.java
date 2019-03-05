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

package uk.gov.gchq.palisade.example;

import uk.gov.gchq.koryphe.tuple.Tuple;
import uk.gov.gchq.palisade.Util;
import uk.gov.gchq.palisade.util.FieldGetter;
import uk.gov.gchq.palisade.util.FieldSetter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ExampleObjTuple implements Tuple<String> {
    public static final String TIMESTAMP = "timestamp";
    public static final String VISIBILITY = "visibility";
    public static final String PROPERTY = "property";
    private static final Map<String, FieldGetter<ExampleObj>> FIELD_GETTERS = createFieldGetters();
    private static final Map<String, FieldSetter<ExampleObj>> FIELD_SETTERS = createFieldSetters();

    private ExampleObj obj;

    public ExampleObjTuple() {
    }

    public ExampleObjTuple(final ExampleObj obj) {
        this.obj = obj;
    }

    @Override
    public Object get(final String field) {
        return Util.getField(obj, FIELD_GETTERS, field);
    }

    @Override
    public void put(final String field, final Object value) {
        Util.setField(obj, FIELD_SETTERS, field, value);
    }

    @Override
    public Iterable<Object> values() {
        throw new UnsupportedOperationException();
    }

    public ExampleObj getObj() {
        return obj;
    }

    private static Map<String, FieldGetter<ExampleObj>> createFieldGetters() {
        Map<String, FieldGetter<ExampleObj>> map = new HashMap<>();
        map.put(TIMESTAMP, (obj, subfield) -> obj.getTimestamp());
        map.put(VISIBILITY, (obj, subfield) -> obj.getVisibility());
        map.put(PROPERTY, (obj, subfield) -> obj.getProperty());
        return Collections.unmodifiableMap(map);
    }

    private static Map<String, FieldSetter<ExampleObj>> createFieldSetters() {
        Map<String, FieldSetter<ExampleObj>> map = new HashMap<>();
        map.put(TIMESTAMP, (obj, subfield, value) -> obj.setTimestamp((long) value));
        map.put(VISIBILITY, (obj, subfield, value) -> obj.setVisibility((String) value));
        map.put(PROPERTY, (obj, subfield, value) -> obj.setProperty((String) value));
        return Collections.unmodifiableMap(map);
    }
}
