package uk.gov.gchq.palisade.resource.service.impl;

import org.junit.Test;

import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.resource.impl.DirectoryResource;
import uk.gov.gchq.palisade.resource.service.ResourceService;
import uk.gov.gchq.palisade.resource.service.request.AddResourceRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByIdRequest;
import uk.gov.gchq.palisade.service.request.ConnectionDetail;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public abstract class ResourceServiceTest {
    public static final String DIRECTORY_RESOURCE = "directoryResource";


    @Test
    public void shouldAddAndGetResourceById() throws Exception {
        //given
        ResourceService testObject = getResourceService();
        final AddResourceRequest request = new AddResourceRequest();
        final DirectoryResource resource = new DirectoryResource(DIRECTORY_RESOURCE);
        request.setResource(resource);

        //when
        testObject.addResource(request);
        final Map<Resource, ConnectionDetail> resultMap = testObject.getResourcesById(new GetResourcesByIdRequest(DIRECTORY_RESOURCE)).get(2l, TimeUnit.SECONDS);

        //then
        assertEquals(1, resultMap.size());
        assertEquals(resource, resultMap.keySet().iterator().next());
    }

    public abstract ResourceService getResourceService() throws Exception;
}
