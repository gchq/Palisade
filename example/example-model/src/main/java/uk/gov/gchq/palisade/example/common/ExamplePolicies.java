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

package uk.gov.gchq.palisade.example.common;

import uk.gov.gchq.palisade.example.hrdatagenerator.types.Employee;
import uk.gov.gchq.palisade.example.rule.BankDetailsRule;
import uk.gov.gchq.palisade.example.rule.DutyOfCareRule;
import uk.gov.gchq.palisade.example.rule.NationalityRule;
import uk.gov.gchq.palisade.example.rule.ZipCodeMaskingRule;
import uk.gov.gchq.palisade.example.util.ExampleFileUtil;
import uk.gov.gchq.palisade.policy.service.Policy;
import uk.gov.gchq.palisade.policy.service.request.SetResourcePolicyRequest;
import uk.gov.gchq.palisade.resource.ParentResource;
import uk.gov.gchq.palisade.resource.impl.DirectoryResource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.impl.SystemResource;

import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.StreamSupport;

import static java.util.Objects.isNull;

/**
 * Convenience methods for creating example policies.
 */
public final class ExamplePolicies {

    private ExamplePolicies() {
    }

    /**
     * Create an example security policy on the given file.
     *
     * @param file file to set policy for
     * @return security policy request
     */
    public static SetResourcePolicyRequest getExamplePolicy(final String file) {
        return new SetResourcePolicyRequest()
                .resource(new FileResource().id(file).type(Employee.class.getTypeName()).serialisedFormat("avro").parent(getParent(file)))
                .policy(new Policy<Employee>()
                        .owner(ExampleUsers.getAlice())
                        .recordLevelRule(
                                "1-Bank details only visible for the purpose of salary",
                                new BankDetailsRule()
                        )
                        .recordLevelRule(
                                "2-Emergency numbers only available for duty of care purpose",
                                new DutyOfCareRule()
                        )
                        .recordLevelRule(
                                "3-Nationality is only available for staff report",
                                new NationalityRule()
                        )
                        .recordLevelRule(
                                "4-Address masked for estates staff and otherwise only available for duty of care",
                                new ZipCodeMaskingRule()
                        )

                );
    }

    /**
     * Create an example security policy that contains no record level rules.
     *
     * @param file the file to set policy for
     * @return security policy request
     */
    public static SetResourcePolicyRequest getEmptyPolicy(final String file) {
        return new SetResourcePolicyRequest()
                .resource(new FileResource().id(file).type(Employee.class.getTypeName()).serialisedFormat("avro").parent(getParent(file)))
                .policy(new Policy<Employee>()
                        .owner(ExampleUsers.getAlice())
                );
    }

    //    public static SetResourcePolicyRequest getKorpyheExamplePolicy(final String file) {
    // You can either implement the Rule interface for your Policy rules or
    // you can chain together combinations of Koryphe functions/predicates.
    // Both of the following policies have the same logic, but using
    // koryphe means you don't need to define lots of different rules for
    // different types of objects.
//        return new SetResourcePolicyRequest()
//                .resource(new FileResource().id(file).type(Employee.class.getTypeName()).serialisedFormat("avro").parent(getParent(file)))
//                .policy(new Policy<ExampleObj>()
//                        .owner(alice)
//                        .recordLevelRule(
//                                "1-visibility",
//                                new TupleRule<ExampleObj>()
//                                        .selection("Record.visibility", "User.auths")
//                                        .predicate(new IsXInCollectionY()))
//                        .recordLevelRule(
//                                "2-ageOff",
//                                new TupleRule<ExampleObj>()
//                                        .selection("Record.timestamp")
//                                        .predicate(new IsMoreThan(12L))
//                        )
//                        .recordLevelRule(
//                                "3-redactProperty",
//                                new TupleRule<ExampleObj>()
//                                        .selection("User.roles", "Record.property")
//                                        .function(new If<>()
//                                                .predicate(0, new Not<>(new CollectionContains("admin")))
//                                                .then(1, new SetValue("redacted")))
//                                        .projection("User.roles", "Record.property")
//                        )
//                );

//    }

    public static ParentResource getParent(final String fileURL) {
        URI normalised = ExampleFileUtil.convertToFileURI(fileURL);
        //this should only be applied to URLs that start with 'file://' not other types of URL
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
    public static boolean isDirectoryRoot(final Path path) {
        return StreamSupport
                .stream(FileSystems.getDefault()
                        .getRootDirectories()
                        .spliterator(), false)
                .anyMatch(path::equals);
    }
}
