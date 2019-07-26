package uk.gov.gchq.palisade.service;

import org.junit.Before;
import org.junit.Test;
import uk.gov.gchq.palisade.policy.service.MultiPolicy;
import uk.gov.gchq.palisade.policy.service.Policy;
import uk.gov.gchq.palisade.resource.StubResource;
import uk.gov.gchq.palisade.service.exception.NoPolicyException;

import java.util.Collections;

import static org.junit.Assert.fail;

public class PalisadeServiceTest {

    private StubResource res1;
    private Policy policy;
    private MultiPolicy multiPolicy;

    private MultiPolicy emptyPolicy;

    @Before
    public void createPolicyMap() {
        //create a fake resource
        res1 = new StubResource();
        //create a new empty policy for this resource
        policy = new Policy();
        multiPolicy = new MultiPolicy();
        multiPolicy.setPolicy(res1, policy);
        //create a new policy with no entry for the resource
        emptyPolicy = new MultiPolicy();
    }

    @Test(expected = NoPolicyException.class)
    public void throwOnNoPolicyAvailable() {
        //Given

        //When
        PalisadeService.ensureRecordRulesAvailableFor(emptyPolicy, Collections.singletonList(res1));

        //Then
        fail("exception expected");
    }

    @Test
    public void shouldAcceptExplicitEmptyPolicy() {
        //Given - nothing

        //When
        PalisadeService.ensureRecordRulesAvailableFor(multiPolicy, Collections.singleton(res1));

        //Then - pass
    }

}
