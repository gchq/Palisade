package uk.gov.gchq.palisade.resource.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.odpi.openmetadata.frameworks.connectors.ffdc.InvalidParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.cache.service.impl.HashMapBackingStore;
import uk.gov.gchq.palisade.cache.service.impl.SimpleCacheService;
import uk.gov.gchq.palisade.data.service.impl.MockDataService;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByIdRequest;
import uk.gov.gchq.palisade.service.SimpleConnectionDetail;

import java.io.IOException;

public class EgeriaResourceServiceTest {

    public static final String FILE = System.getProperty("os.name").toLowerCase().startsWith("win") ? "file:///" : "file://";
    private static final Logger LOGGER = LoggerFactory.getLogger(EgeriaResourceServiceTest.class);

    private EgeriaResourceService resourceService;
    private SimpleConnectionDetail simpleConnection;
    private SimpleCacheService simpleCache;

    public EgeriaResourceServiceTest() {
    }

    @Before
    public void setUp() throws InvalidParameterException, IOException {
        simpleConnection = new SimpleConnectionDetail().service(new MockDataService());
        simpleCache = new SimpleCacheService().backingStore(new HashMapBackingStore(true));

        resourceService = new EgeriaResourceService("cocoMDS1", "http://localhost:18081");
    }

    @Test
    public void getAll() {

    }

    @Test
    public void getResourcesByIdTest() {
        //given
        UserId peter = new UserId().id("peterprofile");
        //when
        GetResourcesByIdRequest idRequest = new GetResourcesByIdRequest().resourceId("file://secured/research/clinical-trials/drop-foot/DropFootMeasurementsWeek3.csv").userId(peter);
        resourceService.getResourcesById(idRequest);
    }

    @Test
    public void shouldGetResourcesOutsideOfScope() {
        //given

        //when

        //then
    }

    @Test
    public void shouldGetResourcesByIdOfAFolder() {
        //given

        //when

        //then
    }

    @Test
    public void shouldGetResourcesByType() {
        //given

        //when

        //then
    }

    @Test
    public void shouldBeEqualAfterConfigure() {
        //given

        //when

        //then
    }

    @Test
    public void shouldGetResourcesByFormat() {
        //given

        //when

        //then
    }

    @Test
    public void shouldGetResourcesByResource() {
        //given

        //when

        //then
    }

    @Test
    public void testAddResource() {
    }

    @Test
    public void shouldJSONSerialiser() {
    }

    @Test
    public void shouldErrorWithNoConnectionDetails() {
    }

    @Test
    public void shouldGetFormatConnectionWhenNoTypeConnection() {
    }

    @Test
    public void shouldResolveParents() {
    }

}
