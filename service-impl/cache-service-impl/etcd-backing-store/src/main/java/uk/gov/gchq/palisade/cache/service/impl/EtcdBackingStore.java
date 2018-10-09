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
package uk.gov.gchq.palisade.cache.service.impl;

import com.coreos.jetcd.Client;
import com.coreos.jetcd.KV;
import com.coreos.jetcd.data.ByteSequence;
import com.coreos.jetcd.kv.GetResponse;
import com.coreos.jetcd.kv.PutResponse;
import com.coreos.jetcd.options.GetOption;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class EtcdBackingStore implements BackingStore {

    private Client etcdClient;
    @JsonIgnore
    private KV keyValueClient;

    public EtcdBackingStore() {

    }

    public EtcdBackingStore etcdClient(final String... connectionDetails) {
        this.etcdClient = Client.builder().endpoints(connectionDetails).build();
        this.keyValueClient = etcdClient.getKVClient();
        return this;
    }

    public void setEtcdClient(final String... connectionDetails) {
        etcdClient(connectionDetails);
    }

    public Client getEtcdClient() {
        requireNonNull(etcdClient, "The etcd client has not been set.");
        return etcdClient;
    }

    @JsonIgnore
    public KV getKeyValueClient() {
        requireNonNull(keyValueClient, "The etcd client has not been set.");
        return keyValueClient;
    }

    @Override
    public boolean add(final String key, final Class<?> valueClass, final byte[] value, final Optional<Duration> timeToLive) {
        BackingStore.validateAddParameters(key,valueClass,value,timeToLive);
        CompletableFuture<PutResponse> response1 = getKeyValueClient().put(ByteSequence.fromString(key + ".class"), ByteSequence.fromString(valueClass.getCanonicalName()));
        CompletableFuture<PutResponse> response2 = getKeyValueClient().put(ByteSequence.fromString(key + ".value"), ByteSequence.fromBytes(value));
        response1.join();
        response2.join();
        return true;
    }

    @Override
    public SimpleCacheObject get(final String key) {
        BackingStore.keyCheck(key);
        CompletableFuture<GetResponse> futureValueClass = getKeyValueClient().get(ByteSequence.fromString(key + ".class"));
        CompletableFuture<GetResponse> futureValue = getKeyValueClient().get(ByteSequence.fromString(key + ".value"));
        try {
            return new SimpleCacheObject(Class.forName(futureValueClass.join().getKvs().get(0).getValue().toStringUtf8()), Optional.of(futureValue.join().getKvs().get(0).getValue().getBytes()));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Get request failed due to the class of the value not being found.", e);
        }
    }

    @Override
    public Stream<String> list(final String prefix) {
        requireNonNull(prefix, "prefix");
        return getKeyValueClient().get(ByteSequence.fromString("\0"), GetOption.newBuilder().withPrefix(ByteSequence.fromString(prefix)).build())
                .join()
                .getKvs()
                .stream()
                .map(keyValue -> keyValue.getKey().toStringUtf8());
    }
}
