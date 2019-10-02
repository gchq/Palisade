package uk.gov.gchq.palisade.resource.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.odpi.openmetadata.accessservices.assetconsumer.client.AssetConsumer;
import org.odpi.openmetadata.frameworks.connectors.ffdc.InvalidParameterException;
import org.odpi.openmetadata.frameworks.connectors.ffdc.PropertyServerException;
import org.odpi.openmetadata.frameworks.connectors.ffdc.UserNotAuthorizedException;
import org.odpi.openmetadata.frameworks.connectors.properties.AssetUniverse;
import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByIdRequest;

import java.util.List;

public class EgeriaResourceServiceTest {


    private EgeriaResourceService resourceService;


    public EgeriaResourceServiceTest() {
    }

    @Before
    public void setUp() {
        resourceService = new EgeriaResourceService("cocoMDS1", "http://localhost:18082");
    }

    @Test
    public void experiment() {
        AssetConsumer assetConsumer;
        try {
            assetConsumer = new AssetConsumer("cocoMDS1", "http://localhost:18082");
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
        try {
            assetConsumer = new AssetConsumer("cocoMDS1", "http://localhost:18082");
            List<String> assetUniverse = assetConsumer.getAssetsByToken("peterprofile", "file://secured/research/clinical-trials/drop-foot/DropFootMeasurementsWeek1.csv", 0, 10);
//            List<String> assetUniverse = assetConsumer.getAssetsByName("peterprofile", "file://secured/research/clinical-trials/drop-foot/DropFootMeasurementsWeek1.csv", 0, 10 );
//            List<String> assetUniverse = assetConsumer.findAssets("peterprofile", ".*file.*", 0, 10);
            assetUniverse.forEach((guid) -> {
                try {
                    AssetUniverse assets = assetConsumer.getAssetProperties("peterprofile", guid);
                    System.out.println(assets);
                } catch (InvalidParameterException e) {
                    e.printStackTrace();
                } catch (PropertyServerException e) {
                    e.printStackTrace();
                } catch (UserNotAuthorizedException e) {
                    e.printStackTrace();
                }
            });
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
        GetResourcesByIdRequest idRequest = new GetResourcesByIdRequest().resourceId("file://secured/research/clinical-trials/drop-foot/").userId(peter);
        resourceService.getResourcesById(idRequest).join();
    }

    @Test
    public void shouldGetResourcesByIdOfAFile() {
        //given

        //when

        //then
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
    public void shouldGetResourcesByResource()  {
        //given

        //when

        //then
    }

    @Test
    public void testAddResource() {
    }

    @Test
    public void shouldJSONSerialiser()  {
    }

    @Test
    public void shouldErrorWithNoConnectionDetails()  {
    }

    @Test
    public void shouldGetFormatConnectionWhenNoTypeConnection()  {
    }


    @Test
    public void shouldResolveParents()  {
    }
}
