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

package uk.gov.gchq.palisade.policy.tuple;

import uk.gov.gchq.koryphe.tuple.Tuple;
import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.Util;
import uk.gov.gchq.palisade.util.FieldGetter;
import uk.gov.gchq.palisade.util.FieldSetter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A {@code PalisadeTuple} is a {@link Tuple} that wraps a record, {@link User}
 * and {@link Context} in a record and allows users to select fields
 * based on field names. The field names have namespaces, such as:
 * User.auths, User.userId.id, Context.context, Record.timestamp
 */
public class PalisadeTuple implements Tuple<String> {
    public static final String RECORD_NAMESPACE = "Record";
    public static final String NAMESPACE_MSG =
            User.NAMESPACE + ", " + Context.NAMESPACE + " or " + RECORD_NAMESPACE + ". "
                    + "For example: " + User.NAMESPACE + "." + User.AUTHS;

    private static final Map<String, FieldGetter<PalisadeTuple>> FIELD_GETTERS = createFieldGetters();
    private static final Map<String, FieldSetter<PalisadeTuple>> FIELD_SETTERS = createFieldSetters();

    private final Tuple<String> record;
    private final User user;
    private final Context context;

    /**
     * Constructs a {@link PalisadeTuple} with a record, user and context.
     *
     * @param record        the record tuple
     * @param user          the user
     * @param context the query context
     */
    public PalisadeTuple(final Tuple<String> record,
                         final User user,
                         final Context context) {
        this.record = record;
        this.user = user;
        this.context = context;
    }

    @Override
    public Object get(final String reference) {
        return Util.getField(this, FIELD_GETTERS, reference, (namespace) -> {
            throw new IllegalArgumentException("Unknown namespace: " + namespace
                    + ". Choose from: " + NAMESPACE_MSG);
        });
    }

    @Override
    public void put(final String reference, final Object value) {
        Util.setField(this, FIELD_SETTERS, reference, value, (namespace) -> {
            throw new IllegalArgumentException("Unknown namespace: " + namespace
                    + ". Choose from: " + NAMESPACE_MSG);
        });
    }

    @Override
    public Iterable<Object> values() {
        throw new UnsupportedOperationException("This " + getClass().getSimpleName() + " does not support listing all values.");
    }

    public Tuple<String> getRecord() {
        return record;
    }

    public User getUser() {
        return user;
    }

    public Context getContext() {
        return context;
    }

    private static Map<String, FieldGetter<PalisadeTuple>> createFieldGetters() {
        Map<String, FieldGetter<PalisadeTuple>> map = new HashMap<>();
        map.put(User.NAMESPACE, (tuple, field) -> tuple.user.getField(field));
        map.put(Context.NAMESPACE, (tuple, field) -> tuple.context.get(field));
        map.put(RECORD_NAMESPACE, (tuple, field) -> tuple.record.get(field));
        map.put(null, (tuple, field) -> {
            throw new IllegalArgumentException("Reference must contain a namespace: " + NAMESPACE_MSG);
        });
        return Collections.unmodifiableMap(map);
    }

    private static Map<String, FieldSetter<PalisadeTuple>> createFieldSetters() {
        Map<String, FieldSetter<PalisadeTuple>> map = new HashMap<>();
        map.put(User.NAMESPACE, (tuple, field, value) -> tuple.user.setField(field, value));
        map.put(Context.NAMESPACE, (tuple, field, value) -> tuple.context.put(field, value));
        map.put(RECORD_NAMESPACE, (tuple, field, value) -> tuple.record.put(field, value));
        map.put(null, (tuple, field, value) -> {
            throw new IllegalArgumentException("Reference must contain a namespace: " + NAMESPACE_MSG);
        });
        return Collections.unmodifiableMap(map);
    }
}
