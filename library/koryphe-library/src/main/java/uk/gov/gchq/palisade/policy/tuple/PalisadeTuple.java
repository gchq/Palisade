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

import static java.util.Objects.requireNonNull;

/**
 * A {@code PalisadeTuple} is a {@link Tuple} that wraps a record, {@link User}
 * and {@link Context} in a record and allows users to select fields
 * based on field names. The field names have namespaces, such as:
 * User.auths, User.userId.id, Context.context, Record.timestamp
 */
public class PalisadeTuple implements Tuple<String> {
    public static final String RECORD_NAMESPACE = "Record";
    public static final String CONTEXT_NAMESPACE = "Context";
    public static final String NAMESPACE_MSG =
            User.NAMESPACE + ", " + CONTEXT_NAMESPACE + " or " + RECORD_NAMESPACE + ". "
                    + "For example: " + User.NAMESPACE + "." + User.AUTHS;

    private static final Map<String, FieldGetter<PalisadeTuple>> FIELD_GETTERS = createFieldGetters();
    private static final Map<String, FieldSetter<PalisadeTuple>> FIELD_SETTERS = createFieldSetters();

    private Tuple<String> record;
    private User user;
    private Context context;

    public PalisadeTuple() {
    }

    public PalisadeTuple record(final Tuple<String> record) {
        requireNonNull(record, "The record cannot be set to null.");
        this.record = record;
        return this;
    }

    public PalisadeTuple user(final User user) {
        requireNonNull(user, "The user cannot be set to null.");
        this.user = user;
        return this;
    }

    public PalisadeTuple context(final Context context) {
        requireNonNull(context, "The context cannot be set to null.");
        this.context = context;
        return this;
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
        requireNonNull(record, "The record has not been set.");
        return record;
    }

    public User getUser() {
        requireNonNull(user, "The user has not been set.");
        return user;
    }

    public Context getContext() {
        requireNonNull(context, "The context has not been set.");
        return context;
    }

    public void setRecord(final Tuple<String> record) {
        record(record);
    }

    public void setUser(final User user) {
        user(user);
    }

    public void setContext(final Context context) {
        context(context);
    }

    private static Map<String, FieldGetter<PalisadeTuple>> createFieldGetters() {
        Map<String, FieldGetter<PalisadeTuple>> map = new HashMap<>();
        map.put(User.NAMESPACE, (tuple, field) -> tuple.getUser().getField(field));
        map.put("Context", (tuple, field) -> tuple.getContext().get(field));
        map.put(RECORD_NAMESPACE, (tuple, field) -> tuple.getRecord().get(field));
        map.put(null, (tuple, field) -> {
            throw new IllegalArgumentException("Reference must contain a namespace: " + NAMESPACE_MSG);
        });
        return Collections.unmodifiableMap(map);
    }

    private static Map<String, FieldSetter<PalisadeTuple>> createFieldSetters() {
        Map<String, FieldSetter<PalisadeTuple>> map = new HashMap<>();
        map.put(User.NAMESPACE, (tuple, field, value) -> tuple.getUser().setField(field, value));
        map.put("Context", (tuple, field, value) -> tuple.getContext().put(field, value));
        map.put(RECORD_NAMESPACE, (tuple, field, value) -> tuple.getRecord().put(field, value));
        map.put(null, (tuple, field, value) -> {
            throw new IllegalArgumentException("Reference must contain a namespace: " + NAMESPACE_MSG);
        });
        return Collections.unmodifiableMap(map);
    }
}
