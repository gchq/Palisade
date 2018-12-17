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

import uk.gov.gchq.koryphe.impl.function.If;
import uk.gov.gchq.koryphe.impl.function.SetValue;
import uk.gov.gchq.koryphe.impl.predicate.CollectionContains;
import uk.gov.gchq.koryphe.impl.predicate.IsMoreThan;
import uk.gov.gchq.koryphe.impl.predicate.Not;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.client.ServicesFactory;
import uk.gov.gchq.palisade.client.SimpleClient;
import uk.gov.gchq.palisade.example.ExampleObj;
import uk.gov.gchq.palisade.example.data.serialiser.ExampleObjSerialiser;
import uk.gov.gchq.palisade.example.rule.IsExampleObjRecent;
import uk.gov.gchq.palisade.example.rule.IsExampleObjVisible;
import uk.gov.gchq.palisade.example.rule.RedactExampleObjProperty;
import uk.gov.gchq.palisade.example.rule.predicate.IsXInCollectionY;
import uk.gov.gchq.palisade.policy.service.Policy;
import uk.gov.gchq.palisade.policy.service.request.SetResourcePolicyRequest;
import uk.gov.gchq.palisade.policy.tuple.TupleRule;
import uk.gov.gchq.palisade.resource.ParentResource;
import uk.gov.gchq.palisade.resource.impl.DirectoryResource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.impl.SystemResource;
import uk.gov.gchq.palisade.user.service.UserService;
import uk.gov.gchq.palisade.user.service.request.AddUserRequest;

import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Objects.isNull;

public class ExampleSimpleClient extends SimpleClient<ExampleObj> {
    private final String file;

    public ExampleSimpleClient(final ServicesFactory services, final String file) {
        super(services, new ExampleObjSerialiser());
        URI absoluteFileURI = ExampleFileUtil.convertToFileURI(file);
        String absoluteFile = absoluteFileURI.toString();
        this.file = absoluteFile;
        initialiseServices();
    }

    private void initialiseServices() {
        // The user authorisation owner or sys admin needs to add the user
        final UserService userService = getServicesFactory().getUserService();

        final User alice = new User()
                .userId("Alice")
                .auths("public", "private")
                .roles("user", "admin");

        final CompletableFuture<Boolean> userAliceStatus = userService.addUser(
                new AddUserRequest().user(alice)
        );
        final CompletableFuture<Boolean> userBobStatus = getServicesFactory().getUserService().addUser(
                new AddUserRequest().user(
                        new User()
                                .userId("Bob")
                                .auths("public")
                                .roles("user")
                )
        );

        // You can either implement the Rule interface for your Policy rules or
        // you can chain together combinations of Koryphe functions/predicates.
        // Both of the following policies have the same logic, but using
        // koryphe means you don't need to define lots of different rules for
        // different types of objects.

        // Using Custom Rule implementations - without Koryphe
        final SetResourcePolicyRequest customPolicies =
                new SetResourcePolicyRequest()
                        .resource(new FileResource().id(file).type("exampleObj").serialisedFormat("txt").parent(getParent(file)))
                        .policy(new Policy<ExampleObj>()
                                .owner(alice)
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
        final SetResourcePolicyRequest koryphePolicies = new SetResourcePolicyRequest()
                .resource(new FileResource().id(file).type("exampleObj").serialisedFormat("txt").parent(getParent(file)))
                .policy(new Policy<ExampleObj>()
                        .owner(alice)
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

        final CompletableFuture<Boolean> policyStatus = getServicesFactory().getPolicyService().setResourcePolicy(
                koryphePolicies
        );
        // Wait for the users and policies to be loaded
        CompletableFuture.allOf(userAliceStatus, userBobStatus, policyStatus).join();
    }

    public Stream<ExampleObj> read(final String filename, final String userId, final String justification) {
        URI absoluteFileURI = ExampleFileUtil.convertToFileURI(filename);
        String absoluteFile = absoluteFileURI.toString();
        return super.read(absoluteFile, ExampleConfigurator.RESOURCE_TYPE, userId, justification);
    }

    public static ParentResource getParent(final String fileURL) {
        URI normalised = ExampleFileUtil.convertToFileURI(fileURL);
        //this should only be applied to things that start with file:/// not other types of URL
        if (normalised.getScheme().equals(FileSystems.getDefault().provider().getScheme())) {
            Path current = Paths.get(normalised);
            Path parent = current.getParent();
            //no parent can be found, must already be a directory tree root
            if (isNull(parent)) {
                throw new IllegalArgumentException(fileURL + " is already a directory tree root");
            } else if (isDirectoryRoot(parent)) {
                //else if this is a directory tree root
                return new SystemResource().id(parent.toUri().toString());
            } else {
                //else recurse up a level
                return new DirectoryResource().id(parent.toUri().toString()).parent(getParent(parent.toUri().toString()));
            }
        } else {
            //if this is another scheme then there is no definable parent
            return new SystemResource().id("");
        }
    }

    /**
     * Tests if the given {@link Path} represents a root of the default local file system.
     *
     * @param path the path to test
     * @return true if {@code parent} is a root
     */
    private static boolean isDirectoryRoot(final Path path) {
        return StreamSupport
                .stream(FileSystems.getDefault()
                        .getRootDirectories()
                        .spliterator(), false)
                .anyMatch(path::equals);
    }

    /**
     * Gets the file passed at construction as a fully qualified URI.
     *
     * @return the absolute URI file path
     */
    public String getURIConvertedFile() {
        return file;
    }
}
