package uk.gov.gchq.palisade.resource.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.odpi.openmetadata.accessservices.assetconsumer.client.AssetConsumer;
import org.odpi.openmetadata.frameworks.connectors.ffdc.InvalidParameterException;
import org.odpi.openmetadata.frameworks.connectors.ffdc.PropertyServerException;
import org.odpi.openmetadata.frameworks.connectors.ffdc.UserNotAuthorizedException;
import org.odpi.openmetadata.frameworks.connectors.properties.AssetDescriptor;
import org.odpi.openmetadata.frameworks.connectors.properties.AssetDetail;
import org.odpi.openmetadata.frameworks.connectors.properties.AssetElementType;
import org.odpi.openmetadata.frameworks.connectors.properties.AssetSummary;
import org.odpi.openmetadata.frameworks.connectors.properties.AssetUniverse;
import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByIdRequest;

import java.util.ArrayList;
import java.util.List;

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
            List<String> assetUniverse = assetConsumer.findAssets("peterprofile", ".*file.*", 0, 10);
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

            AssetDetail assetDetail = assetUni.get(0);
            AssetSummary assetSummary = assetDetail;
            AssetDescriptor assetDescriptor = assetSummary;
            AssetElementType assetElementType = assetSummary.getType();
            System.out.println(assetElementType.getElementTypeName());
            System.out.println(assetUni.get(0));
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
        GetResourcesByIdRequest idRequest = new GetResourcesByIdRequest().resourceId("CSVFile:file://secured/research/clinical-trials/drop-foot/DropFootMeasurementsWeek1.csv").userId(peter);
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
