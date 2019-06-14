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

package uk.gov.gchq.palisade.example.perf.actions;

import uk.gov.gchq.palisade.client.ClientConfiguredServices;
import uk.gov.gchq.palisade.config.service.ConfigUtils;
import uk.gov.gchq.palisade.config.service.ConfigurationService;
import uk.gov.gchq.palisade.example.common.ExamplePolicies;
import uk.gov.gchq.palisade.example.perf.Perf;
import uk.gov.gchq.palisade.example.perf.PerfAction;
import uk.gov.gchq.palisade.example.perf.PerfFileSet;
import uk.gov.gchq.palisade.example.util.ExampleFileUtil;
import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.palisade.policy.service.request.SetResourcePolicyRequest;
import uk.gov.gchq.palisade.util.StreamUtil;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static uk.gov.gchq.palisade.example.perf.PerfUtils.getLargeFile;
import static uk.gov.gchq.palisade.example.perf.PerfUtils.getNoPolicyName;
import static uk.gov.gchq.palisade.example.perf.PerfUtils.getSmallFile;
import static uk.gov.gchq.palisade.example.perf.PerfUtils.toURI;

/**
 * Uses an existing deployment of Palisade to set policies on the given files. The policy from the example
 * deployment is used.
 */
public class SetPolicyAction extends PerfAction {
    @Override
    public String name() {
        return "policy";
    }

    @Override
    public String description() {
        return "sets example policies on generated files";
    }

    @Override
    public String help() {
        return "Action " + name() + " sets policies on the files previously generated," +
                "\nso that they can be read through Palisade. This takes exactly one argument" +
                "\nwhich is the path or URI to where the files have been created.";
    }

    @Override
    public Integer apply(final String[] args) {
        validate(args);

        //if the files exist on the local system, then convert the path to a absolute path
        URI uriOut = ExampleFileUtil.convertToFileURI(args[0]);
        Perf.LOGGER.info("Specified path {} has been normalised to {}", args[0], uriOut);

        //for path manipulations, we temporarily strip off the URI scheme so that we can operate on abstract path components
        //as if they used the file scheme
        String scheme = uriOut.getScheme();
        String schemelessComponent = uriOut.getRawSchemeSpecificPart();

        Path output = Paths.get(schemelessComponent);

        //make paths
        PerfFileSet fileSet = new PerfFileSet(toURI(scheme, getSmallFile(output)), toURI(scheme, getLargeFile(output)));
        PerfFileSet noPolicySet = new PerfFileSet(toURI(scheme, getNoPolicyName(getSmallFile(output))), toURI(scheme, getNoPolicyName(getLargeFile(output))));

        Stream<URI> paths = Stream.of(fileSet.getSmallFile(), fileSet.getLargeFile());
        Stream<URI> noPolicyPaths = Stream.of(noPolicySet.getSmallFile(), noPolicySet.getLargeFile());

        //attempt to connect to Palisade
        final InputStream stream = StreamUtil.openStream(SetPolicyAction.class, ConfigUtils.retrieveConfigurationPath());
        ConfigurationService configService = JSONSerialiser.deserialise(stream, ConfigurationService.class);
        ClientConfiguredServices cs = new ClientConfiguredServices(configService);

        //some files need a policy
        paths.forEach(path -> setPolicy(cs, ExamplePolicies.getExamplePolicy(path.toString())));
        //others need an empty policy (Palisade doesn't allow no policy)
        noPolicyPaths.forEach(path -> setPolicy(cs, ExamplePolicies.getEmptyPolicy(path.toString())));

        return Integer.valueOf(0);
    }

    /**
     * Set a policy on the given file.
     *
     * @param cs            the services provider
     * @param policyRequest the policy to set on the path
     */
    public static void setPolicy(final ClientConfiguredServices cs, final SetResourcePolicyRequest policyRequest) {
        requireNonNull(cs, "cs");
        requireNonNull(policyRequest, "policyRequest");
        String path = policyRequest.getResource().getId();
        Perf.LOGGER.debug("Attempt to set security policy on {}", path);
        //get result and log
        Boolean result = cs.getPolicyService().setResourcePolicy(policyRequest).join();
        Perf.LOGGER.info("Security policy has been set for {}: {}", path, result);
    }

    /**
     * Validate the arguments.
     *
     * @param args action arguments
     * @throws IllegalArgumentException if any error is found in the arguments
     */
    private void validate(final String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("expected exactly 1 argument, see \"help " + name() + "\"");
        }
    }
}
