package uk.gov.gchq.palisade.example;

import org.junit.Assert;
import org.junit.Test;
import uk.gov.gchq.palisade.client.ConfiguredClientServices;
import uk.gov.gchq.palisade.config.service.InitialConfigurationService;
import uk.gov.gchq.palisade.example.client.ExampleConfigurator;
import uk.gov.gchq.palisade.example.client.ExampleSimpleClient;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.gchq.palisade.example.MultiDockerJvmExample.FILE;

public class MultiJvmDockerExampleIT {

    @Test
    public void shouldRunWithoutErrors() throws Exception {
        // Given
        final MultiDockerJvmExample example = new MultiDockerJvmExample();

        // When
        example.run();

        // Then - no exceptions
    }

    @Test
    public void shouldReadAsAlice() throws Exception {
        // Given
        final InitialConfigurationService ics = ExampleConfigurator.setupDockerConfigurationService();
        final ConfiguredClientServices cs = new ConfiguredClientServices(ics);
        final ExampleSimpleClient client = new ExampleSimpleClient(cs, FILE);

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

    @Test
    public void shouldReadAsBob() throws Exception {
        // Given
        final InitialConfigurationService ics = ExampleConfigurator.setupDockerConfigurationService();
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
