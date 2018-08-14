package uk.gov.gchq.palisade.policy.service.impl;

import org.junit.Before;
import org.junit.Test;

import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.policy.service.MultiPolicy;
import uk.gov.gchq.palisade.policy.service.Policy;
import uk.gov.gchq.palisade.policy.service.request.CanAccessRequest;
import uk.gov.gchq.palisade.policy.service.request.GetPolicyRequest;
import uk.gov.gchq.palisade.policy.service.request.SetPolicyRequest;
import uk.gov.gchq.palisade.policy.service.response.CanAccessResponse;
import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.resource.impl.DirectoryResource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.impl.SystemResource;
import uk.gov.gchq.palisade.rule.Rules;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the functionality of the {@link HierarchicalPolicyService}
 */
public class HierarchicalPolicyServiceTest {

    private HierarchicalPolicyService policyService;

    @Before
    public void setup() {
        policyService = new HierarchicalPolicyService(createDataTypePolicyStore(), createResourcePolicyStore());
    }

    private static HashMap<String, Policy> createDataTypePolicyStore() {
        HashMap<String, Policy> store = new HashMap<>();
        Policy policy = new Policy<>()
                .resourceLevelSimplePredicateRule("Input is not null", Objects::nonNull);
        store.put("TestObj", policy);
        return store;
    }

    private static SystemResource createTestSystemResource() {
        return new SystemResource().id("File");
    }

    private static DirectoryResource createTestDirectoryResource() {
        DirectoryResource directoryResource = new DirectoryResource().id("File://temp");
        directoryResource.setParent(createTestSystemResource());
        return directoryResource;
    }


    private static FileResource createTestFileResource() {
        FileResource fileResource = new FileResource().id("File://temp/TestObj_001.txt").type("TestObj").serialisedFormat("txt");
        fileResource.setParent(createTestDirectoryResource());
        return fileResource;
    }

    private static HashMap<Resource, Policy> createResourcePolicyStore() {
        HashMap<Resource, Policy> store = new HashMap<>();

        // system level policy
        Policy systemPolicy = new Policy<>()
                .resourceLevelSimplePredicateRule("Resource type is TestObj", resource -> resource.getType().equalsIgnoreCase("testobj"));
        store.put(createTestSystemResource(), systemPolicy);

        // directory level policy
        Policy directoryPolicy = new Policy<>()
                .recordLevelSimpleFunctionRule("Does nothing", a -> a);
        store.put(createTestDirectoryResource(), directoryPolicy);

        // file level policy
        Policy filePolicy = new Policy<>()
                .recordLevelRule("Check user has 'Sensitive' auth", (record, user, justification) -> {
                    if (user.getAuths().contains("Sensitive")) {
                        return record;
                    } else {
                        return null;
                    }
                });
        store.put(createTestFileResource(), filePolicy);
        return store;
    }

    @Test
    public void getApplicableResourceLevelRules() {
        // given
        FileResource testResource = createTestFileResource();
        // try
        Rules<Resource> result = policyService.getApplicableRules(testResource, true, testResource.getType());
        // check
        assertEquals("Input is not null, Resource type is TestObj", result.getMessage());
        assertEquals(2, result.getRules().keySet().size());
    }

    @Test
    public void getApplicableRecordLevelRules() {
        // given
        FileResource testResource = createTestFileResource();
        // try
        Rules<Resource> result = policyService.getApplicableRules(testResource, false, testResource.getType());
        // check
        assertEquals("Does nothing, Check user has 'Sensitive' auth", result.getMessage());
        assertEquals(2, result.getRules().keySet().size());
    }

    @Test
    public void canAccessIsValid() throws InterruptedException, ExecutionException, TimeoutException {
        // given
        User user = new User().userId("testUser").auths("Sensitive");
        Context context = new Context().justification("testing");
        Resource resource = createTestFileResource();
        // try
        CompletableFuture<CanAccessResponse> future = policyService.canAccess(new CanAccessRequest().resources(Collections.singletonList(resource)).user(user).context(context));
        CanAccessResponse response = future.get();
        Collection<Resource> resources = response.getCanAccessResources();
        // check
        assertEquals(1, resources.size());
        assertEquals(resource, resources.iterator().next());
    }

    @Test
    public void getPolicy() throws InterruptedException, ExecutionException, TimeoutException {
        // given
        User user = new User().userId("testUser").auths("Sensitive");
        Context context = new Context().justification("testing");
        Resource resource = createTestFileResource();
        // try
        CompletableFuture<MultiPolicy> future = policyService.getPolicy(new GetPolicyRequest().user(user).context(context).resources(Collections.singletonList(resource)));
        MultiPolicy response = future.get();
        Map<Resource, Rules> ruleMap = response.getRuleMap();
        // check
        assertEquals(1, ruleMap.size());
        assertEquals("Does nothing, Check user has 'Sensitive' auth", ruleMap.get(resource).getMessage());
    }

    @Test
    public void setPolicyForNewResource() throws InterruptedException, ExecutionException, TimeoutException {
        // given
        User testUser = new User().userId("testUser").auths("Sensitive");
        FileResource newResource = new FileResource().id("File://temp/TestObj_002.txt").type("TestObj").serialisedFormat("txt");
        newResource.setParent(createTestDirectoryResource());
        Policy newPolicy = new Policy().resourceLevelPredicateRule("Justification is testing", (resource, user, justification) -> justification.getJustification().equals("testing"));
        // try
        CompletableFuture<Boolean> future = policyService.setPolicy(new SetPolicyRequest().resource(newResource).policy(newPolicy));
        Boolean result = future.get();
        assertTrue(result);

        // try
        CompletableFuture<CanAccessResponse> future2 = policyService.canAccess(new CanAccessRequest().resources(Collections.singletonList(newResource)).user(testUser).context(new Context().justification("fun")));
        CanAccessResponse response2 = future2.get();
        Collection<Resource> resources2 = response2.getCanAccessResources();
        // check
        assertEquals(0, resources2.size());
    }

    @Test
    public void setPolicyForExistingResource() throws InterruptedException, ExecutionException, TimeoutException {
        // given
        User testUser = new User().userId("testUser").auths("Sensitive");
        Context testContext = new Context().justification("testing");
        Resource testResource = createTestFileResource();
        // try
        CompletableFuture<CanAccessResponse> future1 = policyService.canAccess(new CanAccessRequest().resources(Collections.singletonList(testResource)).user(testUser).context(testContext));
        CanAccessResponse response = future1.get();
        Collection<Resource> resources = response.getCanAccessResources();
        // check
        assertEquals(1, resources.size());
        assertEquals(testResource, resources.iterator().next());

        // given
        Policy newPolicy = new Policy().resourceLevelPredicateRule("Justification is testing", (resource, user, justification) -> justification.getJustification().equals("testing"));
        // try
        CompletableFuture<Boolean> future = policyService.setPolicy(new SetPolicyRequest().resource(testResource).policy(newPolicy));
        Boolean result = future.get();
        assertTrue(result);

        // try
        CompletableFuture<CanAccessResponse> future2 = policyService.canAccess(new CanAccessRequest().resources(Collections.singletonList(testResource)).user(testUser).context(new Context().justification("fun")));
        CanAccessResponse response2 = future2.get();
        Collection<Resource> resources2 = response2.getCanAccessResources();
        // check
        assertEquals(0, resources2.size());
    }
}
