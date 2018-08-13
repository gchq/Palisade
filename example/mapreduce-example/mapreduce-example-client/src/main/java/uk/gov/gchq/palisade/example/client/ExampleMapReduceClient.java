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

import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.JobContext;

import uk.gov.gchq.palisade.Justification;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.client.SimpleClient;
import uk.gov.gchq.palisade.data.serialise.Serialiser;
import uk.gov.gchq.palisade.data.service.impl.SimpleDataService;
import uk.gov.gchq.palisade.example.ExampleObj;
import uk.gov.gchq.palisade.example.data.ExampleSimpleDataReader;
import uk.gov.gchq.palisade.example.data.serialiser.ExampleObjSerialiser;
import uk.gov.gchq.palisade.example.function.IsTimestampMoreThan;
import uk.gov.gchq.palisade.example.function.IsVisible;
import uk.gov.gchq.palisade.mapreduce.PalisadeInputFormat;
import uk.gov.gchq.palisade.policy.service.Policy;
import uk.gov.gchq.palisade.policy.service.request.SetPolicyRequest;
import uk.gov.gchq.palisade.resource.impl.DirectoryResource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.service.request.AddResourceRequest;
import uk.gov.gchq.palisade.service.PalisadeService;
import uk.gov.gchq.palisade.service.request.RegisterDataRequest;
import uk.gov.gchq.palisade.service.request.SimpleConnectionDetail;
import uk.gov.gchq.palisade.user.service.request.AddUserRequest;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class ExampleMapReduceClient extends SimpleClient<ExampleObj> {
    public static final String RESOURCE_TYPE = "exampleObj";

    public ExampleMapReduceClient() {
        super();

        // The user authorisation owner or sys admin needs to add the user
        final CompletableFuture<Boolean> userAliceStatus = userService.addUser(
                new AddUserRequest()
                        .user(
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
        final CompletableFuture<Boolean> policyStatus = policyService.setPolicy(
                new SetPolicyRequest().resource(
                        new FileResource()
                                .id("file1")
                                .type(RESOURCE_TYPE))
                        .policy(new Policy<ExampleObj>()
                                        .recordLevelPredicateRule(
                                                "visibility",
                                                new IsVisible()
                                        )
                                        .recordLevelSimplePredicateRule(
                                                "ageOff",
                                                new IsTimestampMoreThan(12L)
                                        )
                        )
        );

        // The sys admin needs to add the resources
        final CompletableFuture<Boolean> resourceStatus = resourceService.addResource(new AddResourceRequest()
                .parent(new DirectoryResource().id("dir1").type(RESOURCE_TYPE))
                .resource(new FileResource().id("file1").type(RESOURCE_TYPE))
                .connectionDetail(new SimpleConnectionDetail().service(new SimpleDataService().palisadeService(palisadeService).reader(new ExampleSimpleDataReader()))
                ));

        // Wait for the users, policies and resources to be loaded
        CompletableFuture.allOf(userAliceStatus, userBobStatus, policyStatus, resourceStatus).join();
    }

    public Stream<ExampleObj> read(final String filename, final String userId, final String justification) {
        return super.read(filename, RESOURCE_TYPE, userId, justification);
    }

    @Override
    protected Serialiser<ExampleObj> createSerialiser() {
        return new ExampleObjSerialiser();
    }

    /**
     * Set up a given MapReduce job to source data from an {@link ExampleMapReduceClient}. This convenience method will
     * configure the input format, set the {@link PalisadeService} and the serialiser.
     *
     * @param job        the job to configure
     * @param client     the particular client
     * @param maxMapHint maximum mapper hint
     */
    public static void initialiseJob(final Job job, final ExampleMapReduceClient client, final int maxMapHint) {
        job.setInputFormatClass(PalisadeInputFormat.class);
        //tell it which Palisade service to use
        PalisadeInputFormat.setPalisadeService(job, client.getPalisadeService());
        //set a maximum mapper hint
        PalisadeInputFormat.setMaxMapTasksHint(job, maxMapHint);
        //configure the serialiser to use
        PalisadeInputFormat.setSerialiser(job, new ExampleObjSerialiser());
    }

    /**
     * Get the {@link PalisadeService} used by this client. This is exposed here so that we can  pass it to the input
     * format.
     *
     * @return the service
     */
    public PalisadeService getPalisadeService() {
        return palisadeService;
    }

    /**
     * Utility method to add a read request to a job.
     *
     * @param context       the job to add the request to
     * @param filename      example filename
     * @param resourceType  the example resource type
     * @param userId        the example user id
     * @param justification the example justification
     */
    public static void addDataRequest(final JobContext context, final String filename, final String resourceType, final String userId, final String justification) {
        final RegisterDataRequest dataRequest = new RegisterDataRequest().resourceId(filename).userId(new UserId().id(userId)).justification(new Justification().justification(justification));
        PalisadeInputFormat.addDataRequest(context, dataRequest);
    }
}
