package uk.gov.gchq.palisade.example;

import org.junit.Assert;
import org.junit.Test;

import uk.gov.gchq.palisade.audit.service.AuditService;
import uk.gov.gchq.palisade.audit.service.impl.LoggerAuditService;
import uk.gov.gchq.palisade.cache.service.CacheService;
import uk.gov.gchq.palisade.cache.service.impl.EtcdBackingStore;
import uk.gov.gchq.palisade.cache.service.impl.SimpleCacheService;
import uk.gov.gchq.palisade.client.ConfiguredClientServices;
import uk.gov.gchq.palisade.config.service.ConfigurationService;
import uk.gov.gchq.palisade.config.service.impl.ProxyRestConfigService;
import uk.gov.gchq.palisade.config.service.request.AddConfigRequest;
import uk.gov.gchq.palisade.data.service.impl.ProxyRestDataService;
import uk.gov.gchq.palisade.example.client.ExampleConfigurator;
import uk.gov.gchq.palisade.example.client.ExampleSimpleClient;
import uk.gov.gchq.palisade.policy.service.PolicyService;
import uk.gov.gchq.palisade.resource.service.ResourceService;
import uk.gov.gchq.palisade.resource.service.impl.ProxyRestResourceService;
import uk.gov.gchq.palisade.rest.ProxyRestConnectionDetail;
import uk.gov.gchq.palisade.service.PalisadeService;
import uk.gov.gchq.palisade.service.Service;
import uk.gov.gchq.palisade.service.impl.ProxyRestPalisadeService;
import uk.gov.gchq.palisade.service.impl.ProxyRestPolicyService;
import uk.gov.gchq.palisade.service.request.ServiceConfiguration;
import uk.gov.gchq.palisade.user.service.UserService;
import uk.gov.gchq.palisade.user.service.impl.ProxyRestUserService;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static uk.gov.gchq.palisade.example.MultiJVMDockerExample.FILE;

public class MultiJvmDockerExampleIT {

    @Test
    public void shouldRunWithoutErrors() throws Exception {
        // Given
        EtcdBackingStore store = null;
        try {
            store = new EtcdBackingStore().connectionDetails(Collections.singletonList("http://localhost:2379"));
            AuditService audit = new LoggerAuditService();
            PolicyService policy = new ProxyRestPolicyService("http://localhost:8081/policy");
            UserService user = new ProxyRestUserService("http://localhost:8083/user");
            ResourceService resource = new ProxyRestResourceService("http://localhost:8082/resource");
            PalisadeService palisade = new ProxyRestPalisadeService("http://localhost:8080/palisade");
            CacheService cache = new SimpleCacheService().backingStore(store);
            ConfigurationService config = new ProxyRestConfigService("http://localhost:8085/config");
            final ServiceConfiguration sc = new ServiceConfiguration();

            //each service to write their configuration into the initial configuration
            Collection<Service> services = Stream.of(audit, user, resource, policy, palisade, cache, config).collect(Collectors.toList());
            services.forEach(service -> service.recordCurrentConfigTo(sc));

            CacheService dockerCacheService = new SimpleCacheService().backingStore(new EtcdBackingStore().connectionDetails(Collections.singleton("http://etcd:2379"), false));

            // When
            final MultiJVMDockerExample example = new MultiJVMDockerExample();
            example.run(Optional.of(sc), config, new ProxyRestConnectionDetail().url("http://localhost:8084/data").serviceClass(ProxyRestDataService.class),
                    dockerCacheService, cache);

            // Then - no exceptions
        } finally {
            if (nonNull(store)) {
                store.close();
            }
        }
    }

    @Test
    public void shouldReadAsAlice() throws Exception {
        // Given
        final ConfigurationService configurationService = getConfigurationService();
        final ConfiguredClientServices ccs = new ConfiguredClientServices(configurationService);
        final ExampleSimpleClient client = new ExampleSimpleClient(ccs, FILE);

        // When
        final Stream<ExampleObj> aliceResults = client.read(FILE, "Alice", "Payroll");

        // Then
        Assert.assertEquals(
                Arrays.asList(
                        new ExampleObj("item1c", "public", 20L),
                        new ExampleObj("item1d", "private", 20L),
                        new ExampleObj("item2c", "public", 20L),
                        new ExampleObj("item2d", "private", 20L)
                ),
                aliceResults.collect(Collectors.toList())
        );
    }

    private ConfigurationService getConfigurationService() {
        EtcdBackingStore store = null;
        try {
            store = new EtcdBackingStore().connectionDetails(Collections.singletonList("http://localhost:2379"));
            AuditService audit = new LoggerAuditService();
            PolicyService policy = new ProxyRestPolicyService("http://localhost:8081/policy");
            UserService user = new ProxyRestUserService("http://localhost:8083/user");
            ResourceService resource = new ProxyRestResourceService("http://localhost:8082/resource");
            PalisadeService palisade = new ProxyRestPalisadeService("http://localhost:8080/palisade");
            CacheService cache = new SimpleCacheService().backingStore(store);
            ConfigurationService config = new ProxyRestConfigService("http://localhost:8085/config");
            final ServiceConfiguration sc = new ServiceConfiguration();

            //each service to write their configuration into the initial configuration
            Collection<Service> services = Stream.of(audit, user, resource, policy, palisade, cache, config).collect(Collectors.toList());
            services.forEach(service -> service.recordCurrentConfigTo(sc));

            CacheService dockerCacheService = new SimpleCacheService().backingStore(new EtcdBackingStore().connectionDetails(Collections.singleton("http://etcd:2379"), false));

            ExampleConfigurator.setupMultiJVMConfigurationService(
                    new ProxyRestPolicyService("http://policy-service:8080/policy"),
                    new ProxyRestUserService("http://user-service:8080/user"),
                    new ProxyRestResourceService("http://resource-service:8080/resource"),
                    new ProxyRestPalisadeService("http://palisade-service:8080/palisade"),
                    dockerCacheService,
                    new ProxyRestConfigService("http://localhost:8085/config"),
                    new ProxyRestConnectionDetail().url("http://localhost:8084/data").serviceClass(ProxyRestDataService.class),
                    cache
            );

            //override the client configuration
            config.add((AddConfigRequest) new AddConfigRequest()
                    .config(sc)
                    .service(Optional.empty())).join();

            return config;
        } finally {
            if (nonNull(store)) {
                store.close();
            }
        }
    }

    @Test
    public void shouldReadAsBob() throws Exception {
        // Given
        final ConfigurationService ics = getConfigurationService();
        final ConfiguredClientServices cs = new ConfiguredClientServices(ics);
        final ExampleSimpleClient client = new ExampleSimpleClient(cs, FILE);

        // When
        final Stream<ExampleObj> aliceResults = client.read(FILE, "Bob", "Payroll");

        // Then
        Assert.assertEquals(
                Arrays.asList(
                        new ExampleObj("redacted", "public", 20L),
                        new ExampleObj("redacted", "public", 20L)
                ),
                aliceResults.collect(Collectors.toList())
        );
    }
}
