package uk.gov.gchq.palisade.resource.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.odpi.openmetadata.accessservices.assetconsumer.client.AssetConsumer;
import org.odpi.openmetadata.frameworks.connectors.ffdc.InvalidParameterException;
import org.odpi.openmetadata.frameworks.connectors.ffdc.PropertyServerException;
import org.odpi.openmetadata.frameworks.connectors.ffdc.UserNotAuthorizedException;
import org.odpi.openmetadata.frameworks.connectors.properties.AssetUniverse;

import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByIdRequest;
import uk.gov.gchq.palisade.service.ConnectionDetail;
import uk.gov.gchq.palisade.service.EgeriaConnection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class EgeriaResourceServiceTest {


    private EgeriaResourceService resourceService;


    public EgeriaResourceServiceTest() {
    }

    @Before
    public void setUp() {
        resourceService = new EgeriaResourceService("cocoMDS1", "http://localhost:18081");
    }

    @Test
    public void experiment() {
        AssetConsumer assetConsumer;
        try {
            assetConsumer = new AssetConsumer("cocoMDS1", "http://localhost:18081");
            AssetUniverse assetUniverse = assetConsumer.getAssetProperties("peterprofile", "6c94ad6b-d41b-439a-bd82-7cf28a6d40ea");
            System.out.println(assetUniverse);
        } catch (InvalidParameterException e) {
            e.printStackTrace();
        } catch (PropertyServerException e) {
            e.printStackTrace();
        } catch (UserNotAuthorizedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getAll() {
        AssetConsumer assetConsumer;
        List<AssetUniverse> assetUni = new ArrayList();
        try {
            assetConsumer = new AssetConsumer("cocoMDS1", "http://localhost:18081");
//            List<String> assetUniverse = assetConsumer.getAssetsByToken("peterprofile", "file://secured/research/clinical-trials/drop-foot/DropFootMeasurementsWeek1.csv", 0, 10);
//            List<String> assetUniverse = assetConsumer.getAssetsByName("peterprofile", "file://secured/research/clinical-trials/drop-foot/DropFootMeasurementsWeek1.csv", 0, 10 );
            List<String> assetUniverse = assetConsumer.findAssets("peterprofile", ".*file.*", 5, 10);
            assetUniverse.forEach((guid) -> {
                try {
                    assetUni.add(assetConsumer.getAssetProperties("peterprofile", guid));
                } catch (PropertyServerException e) {
                    e.printStackTrace();
                } catch (UserNotAuthorizedException e) {
                    e.printStackTrace();
                } catch (InvalidParameterException e) {
                    e.printStackTrace();
                }
            });
            System.out.println(assetUni.size());
            System.out.println(assetUni.get(0));
            System.out.println(assetUni.get(0).getAssetTypeName());

        } catch (InvalidParameterException e) {
            e.printStackTrace();
        } catch (PropertyServerException e) {
            e.printStackTrace();
        } catch (UserNotAuthorizedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getResourcesByIdTest() {
        UserId peter = new UserId().id("peterprofile");
        GetResourcesByIdRequest idRequest = new GetResourcesByIdRequest().resourceId("file://secured/research/clinical-trials/drop-foot").userId(peter);
        resourceService.getResourcesById(idRequest).join();
    }

    @Test
    public void shouldGetResourcesByIdOfAFile() {
        UserId peter = new UserId().id("peterprofile");
        GetResourcesByIdRequest idRequest = new GetResourcesByIdRequest().resourceId("file://secured/research/clinical-trials/drop-foot/DropFootMeasurementsWeek3.csv").userId(peter);
        Map<LeafResource, ConnectionDetail> actual = resourceService.getResourcesById(idRequest).join();

        Map<LeafResource, ConnectionDetail> expected = new HashMap<>();
        FileResource file = new FileResource().id("/secured/research/clinical-trials/drop-foot/DropFootMeasurementsWeek3.csv").serialisedFormat("csv").type("DropFootMeasurementsWeek3");
        EgeriaConnection egeriaConnection = new EgeriaConnection("http://localhost:18081", "cocoMDS1", idRequest.getUserId().toString());
        expected.put(file, egeriaConnection);

        assertThat(actual.size(), is((1)));
        assertThat(actual, is(expected));

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
