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

import io.etcd.jetcd.launcher.junit.EtcdClusterResource;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

public class EtcdBackingStoreTest extends AbstractBackingStoreTest {

    private static EtcdClusterResource etcd;
    private static List<String> clientEndpoints;
    private EtcdBackingStore etcdBackingStore;

    @BeforeClass
    public static void startETCDCluster() {
        // tests require docker to be installed
        etcd = new EtcdClusterResource("test-etcd", 1);
        etcd.cluster().start();
        List<URI> clientEndpointURIs = etcd.cluster().getClientEndpoints();
        clientEndpoints = clientEndpointURIs.stream().map(URI::toString).collect(Collectors.toList());
    }

    @Override
    public EtcdBackingStore createBackingStore() {
        etcdBackingStore = new EtcdBackingStore().connectionDetails(clientEndpoints);
        return etcdBackingStore;
    }

    @After
    public void closeClients() {
        etcdBackingStore.close();
    }

    @AfterClass
    public static void endETCDCluster() {
        if (etcd != null) {
            etcd.cluster().close();
        }
    }
}