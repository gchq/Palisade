package uk.gov.gchq.palisade.resource.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.odpi.openmetadata.accessservices.assetconsumer.client.AssetConsumer;
import org.odpi.openmetadata.frameworks.connectors.ffdc.InvalidParameterException;
import org.odpi.openmetadata.frameworks.connectors.ffdc.PropertyServerException;
import org.odpi.openmetadata.frameworks.connectors.ffdc.UserNotAuthorizedException;
import org.odpi.openmetadata.frameworks.connectors.properties.AssetUniverse;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByIdRequest;

import java.net.URI;
import java.net.URISyntaxException;

public class EgeriaResourceServiceTest {


    private EgeriaResourceService resourceService;


    public EgeriaResourceServiceTest() {
    }

    @Before
    public void setUp() throws Exception {
        resourceService = new EgeriaResourceService();
    }

    @Test
    public void experiment() {
        AssetConsumer assetConsumer;
        try {
            assetConsumer = new AssetConsumer("cocoMDS1","http://localhost:8080");
            AssetUniverse assetUniverse = assetConsumer.getAssetProperties("peterprofile", "afd95cfe-1296-45cd-8dd7-3f8dc2a9d637");
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
    public void shouldGetResourcesByIdOfAFile() throws Exception {
        //given

        //when

        //then
    }

    @Test
    public void shouldGetResourcesOutsideOfScope() throws Exception {
        //given

        //when

        //then
    }

    @Test
    public void shouldGetResourcesByIdOfAFolder() throws Exception {
        //given

        //when

        //then
    }

    @Test
    public void shouldGetResourcesByType() throws Exception {
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
    public void shouldGetResourcesByFormat() throws Exception {
        //given

        //when

        //then
    }

    @Test
    public void shouldGetResourcesByResource() throws Exception {
        //given

        //when

        //then
    }

    @Test
    public void testAddResource() throws Exception {
    }

    @Test
    public void shouldJSONSerialiser() throws Exception {
    }

    @Test
    public void shouldErrorWithNoConnectionDetails() throws Exception {
    }

    @Test
    public void shouldGetFormatConnectionWhenNoTypeConnection() throws Exception {
    }


    @Test
    public void shouldResolveParents() throws Exception {
    }
}
