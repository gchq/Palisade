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
import com.coreos.jetcd.Lease;
import com.coreos.jetcd.data.ByteSequence;
import com.coreos.jetcd.data.KeyValue;
import com.coreos.jetcd.kv.GetResponse;
import com.coreos.jetcd.kv.PutResponse;
import com.coreos.jetcd.options.GetOption;
import com.coreos.jetcd.options.PutOption;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class EtcdBackingStore implements BackingStore {

    private Collection<String> connectionDetails;
    private Client etcdClient;
    private KV keyValueClient;
    private Lease leaseClient;

    public EtcdBackingStore() {
    }

    public void close() {
        if (null != keyValueClient) {
            keyValueClient.close();
            keyValueClient = null;
        }
        if (null != leaseClient) {
            leaseClient.close();
            leaseClient = null;
        }
        etcdClient.close();
        etcdClient = null;
    }

    public Collection<String> getConnectionDetails() {
        requireNonNull(connectionDetails, "The etcd connection details have not been set.");
        return connectionDetails;
    }

    public void setConnectionDetails(final Collection<String> connectionDetails) {
        connectionDetails(connectionDetails);
    }

    public EtcdBackingStore connectionDetails(final Collection<String> connectionDetails) {
        requireNonNull(connectionDetails, "The etcd connection details have not been set.");
        this.connectionDetails = connectionDetails;
        this.etcdClient = Client.builder().endpoints(connectionDetails).build();
        this.keyValueClient = etcdClient.getKVClient();
        this.leaseClient = etcdClient.getLeaseClient();
        return this;
    }

    @JsonIgnore
    public void setEtcdClient(final Collection<String> connectionDetails) {
        connectionDetails(connectionDetails);
    }

    @JsonIgnore
    public Client getEtcdClient() {
        requireNonNull(etcdClient, "No connection is open to the etcd cluster.");
        return etcdClient;
    }

    @JsonIgnore
    private KV getKeyValueClient() {
        requireNonNull(keyValueClient, "No connection is open to the etcd cluster.");
        return keyValueClient;
    }

    @JsonIgnore
    private Lease getLeaseClient() {
        requireNonNull(leaseClient, "No connection is open to the etcd cluster.");
        return leaseClient;
    }

    @Override
    public boolean add(final String key, final Class<?> valueClass, final byte[] value, final Optional<Duration> timeToLive) {
        BackingStore.validateAddParameters(key, valueClass, value, timeToLive);
        long leaseID = 0;
        if (timeToLive.isPresent()) {
            long ttl = timeToLive.get().getSeconds();
            if (ttl > 0) {
                leaseID = getLeaseClient().grant(ttl).join().getID();
            } else {
                // if it has a TTL < 1 second then it is not worth inserting as etcd can not deal with a TTL < 1 second
                return true;
            }
        }
        CompletableFuture<PutResponse> response1 = getKeyValueClient().put(
                ByteSequence.fromString(key + ".class"),
                ByteSequence.fromString(valueClass.getTypeName()),
                PutOption.newBuilder().withLeaseId(leaseID).build());
        CompletableFuture<PutResponse> response2 = getKeyValueClient().put(
                ByteSequence.fromString(key + ".value"),
                ByteSequence.fromBytes(value),
                PutOption.newBuilder().withLeaseId(leaseID).build());
        CompletableFuture.allOf(response1, response2).join();
        return true;
    }

    @Override
    public SimpleCacheObject get(final String key) {
        BackingStore.keyCheck(key);
        CompletableFuture<GetResponse> futureValueClass = getKeyValueClient().get(ByteSequence.fromString(key + ".class"));
        CompletableFuture<GetResponse> futureValue = getKeyValueClient().get(ByteSequence.fromString(key + ".value"));
        List<KeyValue> valueClassKV = futureValueClass.join().getKvs();
        if (valueClassKV.size() == 0) {
            return new SimpleCacheObject(Object.class, Optional.empty());
        }
        try {
            return new SimpleCacheObject(Class.forName(futureValueClass.join().getKvs().get(0).getValue().toStringUtf8()), Optional.of(futureValue.join().getKvs().get(0).getValue().getBytes()));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Get request failed due to the class of the value not being found.", e);
        }
    }

    @Override
    public Stream<String> list(final String prefix) {
        requireNonNull(prefix, "prefix");
        return getKeyValueClient().get(ByteSequence.fromString(prefix), GetOption.newBuilder().withRange(ByteSequence.fromString(prefix + "~")).withKeysOnly(true).build())
                .join()
                .getKvs()
                .stream()
                .map(keyValue -> keyValue.getKey().toStringUtf8())
                .map(key -> key.substring(0, key.lastIndexOf('.')))
                .distinct();
    }
}
