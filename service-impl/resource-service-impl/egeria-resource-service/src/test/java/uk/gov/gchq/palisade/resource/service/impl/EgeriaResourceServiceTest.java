package uk.gov.gchq.palisade.resource.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.cache.service.impl.HashMapBackingStore;
import uk.gov.gchq.palisade.cache.service.impl.SimpleCacheService;
import uk.gov.gchq.palisade.data.service.impl.MockDataService;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByIdRequest;
import uk.gov.gchq.palisade.service.ConnectionDetail;
import uk.gov.gchq.palisade.service.SimpleConnectionDetail;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class EgeriaResourceServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgeriaResourceServiceTest.class);

    private EgeriaResourceService resourceService;
    private SimpleConnectionDetail simpleConnection;
    private SimpleCacheService simpleCache;

    public EgeriaResourceServiceTest() {
    }

    @Before
    public void setUp() throws RuntimeException {
        simpleConnection = new SimpleConnectionDetail().service(new MockDataService());
        simpleCache = new SimpleCacheService().backingStore(new HashMapBackingStore(true));

        //resourceService = new EgeriaResourceService("cocoMDS1", "http://localhost:30881");
        resourceService = new EgeriaResourceService("cocoMDS1", "http://localhost:18081");
    }

    @Test
    public void getAll() {

    }

    @Test
    public void getResourcesByIdTest() {
        UserId peter = new UserId().id("peterprofile");
        GetResourcesByIdRequest request;
        Map<LeafResource, ConnectionDetail> resources;

        // given
        request = new GetResourcesByIdRequest().resourceId("file://secured/research/clinical-trials/drop-foot/DropFootMeasurementsWeek1.csv").userId(peter);
        // when
        resources = resourceService.getResourcesById(request).join();
        // then
        assertThat(resources.size(), equalTo(1));

        // given
        request = new GetResourcesByIdRequest().resourceId("file://secured/research/clinical-trials/drop-foot/.*").userId(peter);
        // when
        resources = resourceService.getResourcesById(request).join();
        // then
        assertThat(resources.size(), equalTo(3));
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
