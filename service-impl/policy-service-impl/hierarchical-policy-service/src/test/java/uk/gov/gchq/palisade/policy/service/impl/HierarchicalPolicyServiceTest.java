package uk.gov.gchq.palisade.policy.service.impl;

import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;

import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.cache.service.impl.SimpleCacheService;
import uk.gov.gchq.palisade.policy.HasSensitiveAuthRule;
import uk.gov.gchq.palisade.policy.HasTestingJustification;
import uk.gov.gchq.palisade.policy.IsTextResourceRule;
import uk.gov.gchq.palisade.policy.PassThroughRule;
import uk.gov.gchq.palisade.policy.service.MultiPolicy;
import uk.gov.gchq.palisade.policy.service.Policy;
import uk.gov.gchq.palisade.policy.service.request.CanAccessRequest;
import uk.gov.gchq.palisade.policy.service.request.GetPolicyRequest;
import uk.gov.gchq.palisade.policy.service.request.SetResourcePolicyRequest;
import uk.gov.gchq.palisade.policy.service.request.SetTypePolicyRequest;
import uk.gov.gchq.palisade.policy.service.response.CanAccessResponse;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.impl.DirectoryResource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.impl.SystemResource;
import uk.gov.gchq.palisade.rule.Rules;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the functionality of the {@link HierarchicalPolicyService}
 */
public class HierarchicalPolicyServiceTest {

    private HierarchicalPolicyService policyService;
    private static final SimpleCacheService cacheService = new SimpleCacheService().backingStore(new HeartbeatTestBackingStore());
    private static final User testUser = new User().userId("testUser");
    private final SystemResource systemResource = createTestSystemResource();
    private final DirectoryResource directoryResource = createTestDirectoryResource();
    private final FileResource fileResource1 = createTestFileResource(1);
    private final FileResource fileResource2 = createTestFileResource(2);

    @Before
    public void setup() {
        policyService = new HierarchicalPolicyService().cacheService(cacheService);

        policyService.setResourcePolicy(new SetResourcePolicyRequest()
                        .resource(fileResource1)
                        .policy(new Policy<>()
                                .owner(testUser)
                                .resourceLevelRule("Input is not null", new PassThroughRule<>())
                                .recordLevelRule("Check user has 'Sensitive' auth", new HasSensitiveAuthRule<>()))
        );

        policyService.setResourcePolicy(new SetResourcePolicyRequest()
                        .resource(fileResource2)
                        .policy(new Policy<>()
                                .owner(testUser)
                                .resourceLevelRule("Input is not null", new PassThroughRule<>())
                                .recordLevelRule("Check user has 'Sensitive' auth", new HasSensitiveAuthRule<>()))
        );

        policyService.setResourcePolicy(new SetResourcePolicyRequest()
                        .resource(directoryResource)
                        .policy(new Policy<>()
                                .owner(testUser)
                                .recordLevelRule("Does nothing", new PassThroughRule<>()))
        );

        policyService.setResourcePolicy(new SetResourcePolicyRequest()
                        .resource(systemResource)
                        .policy(new Policy<>()
                                .owner(testUser)
                                .resourceLevelRule("Resource serialised format is txt", new IsTextResourceRule()))
        );
    }

    private static SystemResource createTestSystemResource() {
        return new SystemResource().id("File");
    }

    private static DirectoryResource createTestDirectoryResource() {
        DirectoryResource directoryResource = new DirectoryResource().id("File://temp");
        directoryResource.setParent(createTestSystemResource());
        return directoryResource;
    }

    private static FileResource createTestFileResource(final int i) {
        FileResource fileResource = new FileResource().id("File://temp/TestObj_00" + i + ".txt").type("TestObj" + i).serialisedFormat("txt");
        fileResource.setParent(createTestDirectoryResource());
        return fileResource;
    }

    @Test
    public void getApplicableResourceLevelRules() {
        // try
        Rules result = policyService.getApplicableRules(fileResource1, true, fileResource1.getType()).join();
        // check
        assertEquals("Resource serialised format is txt, Input is not null", result.getMessage());
        assertEquals(2, result.getRules().keySet().size());
    }

    @Test
    public void getApplicableRecordLevelRules() {
        // try
        Rules result = policyService.getApplicableRules(fileResource1, false, fileResource1.getType()).join();
        // check
        assertEquals("Does nothing, Check user has 'Sensitive' auth", result.getMessage());
        assertEquals(2, result.getRules().keySet().size());
    }

    @Test
    public void canAccessIsValid() throws InterruptedException, ExecutionException, TimeoutException {
        // given
        User user = new User().userId("testUser").auths("Sensitive");
        Context context = new Context().justification("testing");

        // try
        CompletableFuture<CanAccessResponse> future = policyService.canAccess(
                new CanAccessRequest()
                        .resources(Collections.singletonList(fileResource1))
                        .user(user)
                        .context(context));

        CanAccessResponse response = future.get();
        Collection<LeafResource> resources = response.getCanAccessResources();
        // check
        assertEquals(1, resources.size());
        assertEquals(fileResource1, resources.iterator().next());
    }

    @Test
    public void getPolicy() throws InterruptedException, ExecutionException, TimeoutException {
        // given
        User user = new User().userId("testUser").auths("Sensitive");
        Context context = new Context().justification("testing");
        // try
        CompletableFuture<MultiPolicy> future = policyService.getPolicy(new GetPolicyRequest().user(user).context(context).resources(Collections.singletonList(fileResource1)));
        MultiPolicy response = future.get();
        Map<LeafResource, Rules> ruleMap = response.getRuleMap();
        // check
        assertEquals(1, ruleMap.size());
        assertEquals("Does nothing, Check user has 'Sensitive' auth", ruleMap.get(fileResource1).getMessage());
    }

    @Test
    public void setPolicyForNewResource() throws InterruptedException, ExecutionException, TimeoutException {
        // given
        User testUser = new User().userId("testUser").auths("Sensitive");
        FileResource newResource = new FileResource().id("File://temp/TestObj_002.txt").type("TestObj").serialisedFormat("txt");
        newResource.setParent(createTestDirectoryResource());
        Policy newPolicy = new Policy()
                .owner(testUser)
                .resourceLevelRule("Justification is testing", new HasTestingJustification<>());
        // try
        CompletableFuture<Boolean> future = policyService.setResourcePolicy(new SetResourcePolicyRequest().resource(newResource).policy(newPolicy));
        Boolean result = future.get();
        assertTrue(result);

        // try
        CompletableFuture<CanAccessResponse> future2 = policyService.canAccess(new CanAccessRequest().resources(Collections.singletonList(newResource)).user(testUser).context(new Context().justification("fun")));
        CanAccessResponse response2 = future2.get();
        Collection<LeafResource> resources2 = response2.getCanAccessResources();
        // check
        assertEquals(0, resources2.size());
    }

    @Test
    public void setPolicyForExistingResource() throws InterruptedException, ExecutionException, TimeoutException {
        // given
        User testUser = new User().userId("testUser").auths("Sensitive");
        Context testContext = new Context().justification("testing");
        // try
        CompletableFuture<CanAccessResponse> future1 = policyService.canAccess(new CanAccessRequest().resources(Collections.singletonList(fileResource1)).user(testUser).context(testContext));
        CanAccessResponse response = future1.get();
        Collection<LeafResource> resources = response.getCanAccessResources();
        // check
        assertEquals(1, resources.size());
        assertEquals(fileResource1, resources.iterator().next());

        // given
        Policy newPolicy = new Policy().owner(testUser).resourceLevelRule("Justification is testing", new HasTestingJustification<>());
        // try
        CompletableFuture<Boolean> future = policyService.setResourcePolicy(new SetResourcePolicyRequest().resource(fileResource1).policy(newPolicy));
        Boolean result = future.get();
        assertTrue(result);

        // try
        CompletableFuture<CanAccessResponse> future2 = policyService.canAccess(new CanAccessRequest().resources(Collections.singletonList(fileResource1)).user(testUser).context(new Context().justification("fun")));
        CanAccessResponse response2 = future2.get();
        Collection<LeafResource> resources2 = response2.getCanAccessResources();
        // check
        assertEquals(0, resources2.size());
    }

    @Test
    public void setTypePolicy() throws InterruptedException, ExecutionException, TimeoutException {
        // Given
        final User testUser = new User().userId("testUser").auths("Sensitive");

        // Check before policy added
        final CompletableFuture<CanAccessResponse> canAccessBeforeResult = policyService.canAccess(
                new CanAccessRequest()
                        .resources(Arrays.asList(fileResource1, fileResource2))
                        .user(testUser)
                        .context(new Context().justification("fun"))
        );
        final Set<String> types = canAccessBeforeResult.get().getCanAccessResources().stream().map(LeafResource::getType).collect(Collectors.toSet());
        assertEquals(Sets.newHashSet("TestObj1", "TestObj2"), types);
        assertEquals(2, canAccessBeforeResult.get().getCanAccessResources().size());


        final Policy newPolicy = new Policy()
                .owner(testUser)
                .resourceLevelPredicateRule("Justification is testing", (resource, user, justification) -> justification.getJustification().equals("testing"));

        // When
        final CompletableFuture<Boolean> setPolicyResult = policyService.setTypePolicy(
                new SetTypePolicyRequest()
                        .type("TestObj2")
                        .policy(newPolicy)
        );

        // Then
        assertTrue(setPolicyResult.get());
        final CompletableFuture<CanAccessResponse> canAccessAfterResult = policyService.canAccess(
                new CanAccessRequest()
                        .resources(Collections.singletonList(fileResource1))
                        .user(testUser)
                        .context(new Context().justification("fun"))
        );
        assertEquals(1, canAccessAfterResult.get().getCanAccessResources().size());
        assertNotEquals("TestObj2", canAccessAfterResult.get().getCanAccessResources().iterator().next().getType());
    }
}
