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

package uk.gov.gchq.palisade.example.aws_emr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.cache.service.impl.EtcdBackingStore;
import uk.gov.gchq.palisade.cache.service.impl.SimpleCacheService;
import uk.gov.gchq.palisade.client.ConfiguredClientServices;
import uk.gov.gchq.palisade.config.service.ConfigurationService;
import uk.gov.gchq.palisade.data.service.impl.ProxyRestDataService;
import uk.gov.gchq.palisade.example.client.ExampleConfigurator;
import uk.gov.gchq.palisade.example.client.ExampleSimpleClient;
import uk.gov.gchq.palisade.resource.service.impl.ProxyRestResourceService;
import uk.gov.gchq.palisade.rest.ProxyRestConnectionDetail;
import uk.gov.gchq.palisade.service.impl.ProxyRestPalisadeService;
import uk.gov.gchq.palisade.service.impl.ProxyRestPolicyService;
import uk.gov.gchq.palisade.user.service.impl.ProxyRestUserService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public final class AwsEmrExampleConfigurator {
    private static final Logger LOGGER = LoggerFactory.getLogger(AwsEmrExampleConfigurator.class);

    private AwsEmrExampleConfigurator() {
    }

    public static void main(final String[] args) {
        if (args.length == 7) {
            List<String> etcdEndpoints = Arrays.asList(args[0].split(","));
            String policyBaseUrl = args[1];
            String userBaseUrl = args[2];
            String resourceBaseUrl = args[3];
            String palisadeBaseUrl = args[4];
            String dataBaseUrl = args[5];
            String sourceFile = args[6];

            final ConfigurationService ics = ExampleConfigurator.setupMultiJVMConfigurationService(etcdEndpoints, Optional.empty(),
                    Optional.of(new ProxyRestPolicyService(policyBaseUrl)),
                    Optional.of(new ProxyRestUserService(userBaseUrl)),
                    Optional.of(new ProxyRestResourceService(resourceBaseUrl)),
                    Optional.of(new ProxyRestPalisadeService(palisadeBaseUrl)),
                    Optional.of(new SimpleCacheService().backingStore(new EtcdBackingStore().connectionDetails(etcdEndpoints, false))),
                    Optional.of(new ProxyRestConnectionDetail().url(dataBaseUrl).serviceClass(ProxyRestDataService.class)));
            final ConfiguredClientServices cs = new ConfiguredClientServices(ics);
            new ExampleSimpleClient(cs, sourceFile);
        } else {
            LOGGER.error("There should be 7 arguments: etcd endpoint urls as a csv, the policy service base url, the user service base url, the resource service base url, the palisade service base url, the data service url and then the location of the source file to be read.");
        }
    }
}
