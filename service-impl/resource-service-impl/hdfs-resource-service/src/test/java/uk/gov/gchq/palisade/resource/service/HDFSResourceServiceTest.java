package uk.gov.gchq.palisade.resource.service;

import uk.gov.gchq.palisade.resource.service.impl.ResourceServiceTest;

public class HDFSResourceServiceTest  extends ResourceServiceTest{

    @Override
    public ResourceService getResourceService() {
        return new HDFSResourceService();
    }
}