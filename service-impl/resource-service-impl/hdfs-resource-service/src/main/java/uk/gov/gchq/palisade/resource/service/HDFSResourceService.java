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

package uk.gov.gchq.palisade.resource.service;

import com.google.common.collect.Lists;
import org.apache.hadoop.fs.CommonConfigurationKeysPublic;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.hadoop.mapred.JobConf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.service.request.AddResourceRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByFormatRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByIdRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByResourceRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByTypeRequest;
import uk.gov.gchq.palisade.service.request.ConnectionDetail;
import uk.gov.gchq.palisade.service.request.NullConnectionDetail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class HDFSResourceService implements ResourceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(HDFSResourceService.class);
    public static final String ADD_RESOURCE_ERROR = "AddResource is not supported by HDFSResourceService resources should be added/created via regular HDFS behaviour.";

    private final JobConf jobConf;
    private final FileSystem fileSystem;

    public HDFSResourceService(JobConf jobConf) throws IOException {
        this.jobConf = jobConf;
        this.fileSystem = FileSystem.get(jobConf);
    }


    @Override
    public CompletableFuture<Map<Resource, ConnectionDetail>> getResourcesByResource(final GetResourcesByResourceRequest request) {
        return getResourcesById(new GetResourcesByIdRequest(request.getResource().getId()));
    }

    @Override
    public CompletableFuture<Map<Resource, ConnectionDetail>> getResourcesById(final GetResourcesByIdRequest request) {
        final String resourceId = request.getResourceId();
        if (!resourceId.contains(jobConf.get(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY))) {
            throw new UnsupportedOperationException("resource ID is out of scope of the this resource Service. Found: " + resourceId + " expected: " + jobConf.get(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY));
        }

        final Predicate<HDFSResourceDetails> predicate = d -> true;
        return getMapCompletableFuture(resourceId, predicate);
    }

    private CompletableFuture<Map<Resource, ConnectionDetail>> getMapCompletableFuture(final String pathString, final Predicate<HDFSResourceDetails> predicate) {
        return CompletableFuture.supplyAsync(() -> {
            try {

                final RemoteIterator<LocatedFileStatus> remoteIterator = this.fileSystem.listFiles(new Path(pathString), true);
                return getPaths(remoteIterator)
                        .stream()
                        .map(HDFSResourceDetails::getResourceDetailsFromConnectionDetails)
                        .filter(predicate)
                        .map(resourceDetails -> (Resource) new FileResource(resourceDetails.getConnectionDetail(), resourceDetails.getType(), resourceDetails.getFormat()))
                        .collect(Collectors.toMap(fileResource -> fileResource, ignore -> new NullConnectionDetail()));
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Map<Resource, ConnectionDetail>> getResourcesByType(final GetResourcesByTypeRequest request) {
        final String pathString = jobConf.get(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY);
        final Predicate<HDFSResourceDetails> predicate = hdfsResourceDetails -> request.getType().equals(hdfsResourceDetails.getType());
        return getMapCompletableFuture(pathString, predicate);
    }

    @Override
    public CompletableFuture<Map<Resource, ConnectionDetail>> getResourcesByFormat(final GetResourcesByFormatRequest request) {
        final String pathString = jobConf.get(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY);
        final Predicate<HDFSResourceDetails> predicate = hdfsResourceDetails -> request.getFormat().equals(hdfsResourceDetails.getFormat());
        return getMapCompletableFuture(pathString, predicate);
    }

    @Override
    public CompletableFuture<Boolean> addResource(final AddResourceRequest request) {
        throw new UnsupportedOperationException(ADD_RESOURCE_ERROR);
    }

    protected static Collection<String> getPaths(final RemoteIterator<LocatedFileStatus> remoteIterator) throws IOException {
        final ArrayList<String> paths = Lists.newArrayList();
        while (remoteIterator.hasNext()) {
            final LocatedFileStatus next = remoteIterator.next();
            final String pathWithoutFSName = next.getPath().toString().split(Pattern.quote(":"))[1];
            paths.add(pathWithoutFSName);
        }
        return paths;
    }


}

