//package uk.gov.gchq.palisade.example;
//
//import org.junit.Assert;
//import org.junit.Test;
//import uk.gov.gchq.palisade.cache.service.impl.EtcdBackingStore;
//import uk.gov.gchq.palisade.cache.service.impl.SimpleCacheService;
//import uk.gov.gchq.palisade.client.ConfiguredClientServices;
//import uk.gov.gchq.palisade.config.service.ConfigurationService;
//import uk.gov.gchq.palisade.example.config.ExampleConfigurator;
//import uk.gov.gchq.palisade.example.client.ExampleSimpleClient;
//import uk.gov.gchq.palisade.resource.service.impl.ProxyRestResourceService;
//import uk.gov.gchq.palisade.service.impl.ProxyRestPalisadeService;
//import uk.gov.gchq.palisade.service.impl.ProxyRestPolicyService;
//import uk.gov.gchq.palisade.user.service.impl.ProxyRestUserService;
//
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.Optional;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
//import static uk.gov.gchq.palisade.example.MultiJVMDockerExample.FILE;
//
//public class MultiJvmDockerExampleIT {
//
//    @Test
//    public void shouldRunWithoutErrors() throws Exception {
//        // Given
//        final MultiJVMDockerExample example = new MultiJVMDockerExample();
//
//        // When
//        example.run();
//
//        // Then - no exceptions
//    }
//
//    @Test
//    public void shouldReadAsAlice() throws Exception {
//        // Given
//        final ConfigurationService ics = getConfigurationService();
//        final ConfiguredClientServices cs = new ConfiguredClientServices(ics);
//        final ExampleSimpleClient client = new ExampleSimpleClient(cs, FILE);
//
//        // When
//        final Stream<ExampleObj> aliceResults = client.read(FILE, "Alice", "Payroll");
//
//        // Then
//        Assert.assertEquals(
//                Arrays.asList(
//                        new ExampleObj("item1c", "public", 20L),
//                        new ExampleObj("item1d", "private", 20L),
//                        new ExampleObj("item2c", "public", 20L),
//                        new ExampleObj("item2d", "private", 20L)
//                ),
//                aliceResults.collect(Collectors.toList())
//        );
//    }
//
//    private ConfigurationService getConfigurationService() {
//        return ExampleConfigurator.setupMultiJVMConfigurationService(Collections.singletonList("http://localhost:2379"),
//                Optional.empty(),
//                Optional.of(new ProxyRestPolicyService("http://policy-service:8080/policy")),
//                Optional.of(new ProxyRestUserService("http://user-service:8080/user")),
//                Optional.of(new ProxyRestResourceService("http://resource-service:8080/resource")),
//                Optional.of(new ProxyRestPalisadeService("http://palisade-service:8080/palisade")),
//                Optional.of(new SimpleCacheService().backingStore(new EtcdBackingStore().connectionDetails(Collections.singletonList("http://etcd:2379"), false)))
//        );
//    }
//
//    @Test
//    public void shouldReadAsBob() throws Exception {
//        // Given
//        final ConfigurationService ics = getConfigurationService();
//        final ConfiguredClientServices cs = new ConfiguredClientServices(ics);
//        final ExampleSimpleClient client = new ExampleSimpleClient(cs, FILE);
//
//        // When
//        final Stream<ExampleObj> aliceResults = client.read(FILE, "Bob", "Payroll");
//
//        // Then
//        Assert.assertEquals(
//                Arrays.asList(
//                        new ExampleObj("redacted", "public", 20L),
//                        new ExampleObj("redacted", "public", 20L)
//                ),
//                aliceResults.collect(Collectors.toList())
//        );
//    }
//}
