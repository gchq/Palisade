/*
 * Copyright 2018 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.gchq.palisade.example.client;

import uk.gov.gchq.koryphe.impl.function.SetValue;
import uk.gov.gchq.koryphe.impl.predicate.CollectionContains;
import uk.gov.gchq.koryphe.impl.predicate.IsMoreThan;
import uk.gov.gchq.koryphe.impl.predicate.Not;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.client.SimpleRestClient;
import uk.gov.gchq.palisade.data.serialise.Serialiser;
import uk.gov.gchq.palisade.data.service.impl.ProxyRestDataService;
import uk.gov.gchq.palisade.example.ExampleObj;
import uk.gov.gchq.palisade.example.data.serialiser.ExampleObjSerialiser;
import uk.gov.gchq.palisade.example.rule.IsExampleObjRecent;
import uk.gov.gchq.palisade.example.rule.IsExampleObjVisible;
import uk.gov.gchq.palisade.example.rule.RedactExampleObjProperty;
import uk.gov.gchq.palisade.example.rule.function.If;
import uk.gov.gchq.palisade.example.rule.predicate.IsXInCollectionY;
import uk.gov.gchq.palisade.policy.service.Policy;
import uk.gov.gchq.palisade.policy.service.PolicyService;
import uk.gov.gchq.palisade.policy.service.request.SetPolicyRequest;
import uk.gov.gchq.palisade.policy.tuple.TupleRule;
import uk.gov.gchq.palisade.resource.impl.DirectoryResource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.service.ResourceService;
import uk.gov.gchq.palisade.resource.service.request.AddResourceRequest;
import uk.gov.gchq.palisade.rest.ProxyRestConnectionDetail;
import uk.gov.gchq.palisade.user.service.UserService;
import uk.gov.gchq.palisade.user.service.request.AddUserRequest;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class ExampleSimpleRestClient extends SimpleRestClient<ExampleObj> {
    public static final String RESOURCE_TYPE = "exampleObj";

    public ExampleSimpleRestClient() {
        super();
        initialiseServices();
    }

    public Stream<ExampleObj> read(final String filename, final String userId, final String justification) {
        return super.read(filename, RESOURCE_TYPE, userId, justification);
    }

    @Override
    protected Serialiser<ExampleObj> createSerialiser() {
        return new ExampleObjSerialiser();
    }

    private void initialiseServices() {
        // The user authorisation owner or sys admin needs to add the user
        final UserService userService = createUserService();
        final CompletableFuture<Boolean> userAliceStatus = userService.addUser(
                new AddUserRequest().user(
                        new User()
                                .userId("Alice")
                                .auths("public", "private")
                                .roles("user", "admin")
                )
        );
        final CompletableFuture<Boolean> userBobStatus = userService.addUser(
                new AddUserRequest().user(
                        new User()
                                .userId("Bob")
                                .auths("public")
                                .roles("user")
                )
        );


        // The policy owner or sys admin needs to add the policies
        final PolicyService policyService = createPolicyService();

        // You can either implement the Rule interface for your Policy rules or
        // you can chain together combinations of Koryphe functions/predicates.
        // Both of the following policies have the same logic, but using
        // koryphe means you don't need to define lots of different rules for
        // different types of objects.

        // Using Custom Rule implementations - without Koryphe
        final SetPolicyRequest customPolicies =
                new SetPolicyRequest()
                        .resource(new FileResource().id("file1").type(RESOURCE_TYPE))
                        .policy(new Policy<ExampleObj>()
                                        .recordLevelRule(
                                                "1-visibility",
                                                new IsExampleObjVisible()
                                        )
                                        .recordLevelRule(
                                                "2-ageOff",
                                                new IsExampleObjRecent(12L)
                                        )
                                        .recordLevelRule(
                                                "3-redactProperty",
                                                new RedactExampleObjProperty()
                                        )
                        );

        // Using Koryphe's functions/predicates
        final SetPolicyRequest koryphePolicies = new SetPolicyRequest()
                .resource(new FileResource().id("file1").type(RESOURCE_TYPE))
                .policy(new Policy<ExampleObj>()
                                .recordLevelRule(
                                        "1-visibility",
                                        new TupleRule<ExampleObj>()
                                                .selection("Record.visibility", "User.auths")
                                                .predicate(new IsXInCollectionY()))
                                .recordLevelRule(
                                        "2-ageOff",
                                        new TupleRule<ExampleObj>()
                                                .selection("Record.timestamp")
                                                .predicate(new IsMoreThan(12L))
                                )
                                .recordLevelRule(
                                        "3-redactProperty",
                                        new TupleRule<ExampleObj>()
                                                .selection("User.roles", "Record.property")
                                                .function(new If<>()
                                                        .predicate(0, new Not<>(new CollectionContains("admin")))
                                                        .then(1, new SetValue("redacted")))
                                                .projection("User.roles", "Record.property")
                                )
                );

        final CompletableFuture<Boolean> policyStatus = policyService.setPolicy(
                koryphePolicies
        );

        // The sys admin needs to add the resources
        final ResourceService resourceService = createResourceService();
        final CompletableFuture<Boolean> resourceStatus = resourceService
                .addResource(new AddResourceRequest()
                                .parent(new DirectoryResource().id("dir1").type(RESOURCE_TYPE))
                                .resource(new FileResource().id("file1").type(RESOURCE_TYPE))
                                .connectionDetail(new ProxyRestConnectionDetail(ProxyRestDataService.class, "http://localhost:8084/data"))
                );

        // Wait for the users, policies and resources to be loaded
        CompletableFuture.allOf(userAliceStatus, userBobStatus, policyStatus, resourceStatus).join();
    }
}
