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
import java.util.stream.Collectors;

public class HDFSResourceService implements ResourceService {
    public static final String RESOURCE_ROOT_PATH = "resource.root.path";
    private static final Logger LOGGER = LoggerFactory.getLogger(HDFSResourceService.class);

    private final JobConf jobConf;
    private final FileSystem fileSystem;

    public HDFSResourceService(JobConf jobConf) throws IOException {
        this.jobConf = jobConf;
        this.fileSystem = FileSystem.get(jobConf);
    }


    @Override
    public CompletableFuture<Map<Resource, ConnectionDetail>> getResourcesByResource(final GetResourcesByResourceRequest request) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public CompletableFuture<Map<Resource, ConnectionDetail>> getResourcesById(final GetResourcesByIdRequest request) {
        final Predicate<HDFSResourceDetails> filterPredicate = resourceDetails -> request.getResourceId().equals(resourceDetails.getId());
        return getMapCompletableFuture(filterPredicate);
    }

    @Override
    public CompletableFuture<Map<Resource, ConnectionDetail>> getResourcesByType(final GetResourcesByTypeRequest request) {
        final Predicate<HDFSResourceDetails> filterPredicate = resourceDetails -> request.getType().equals(resourceDetails.getType());
        return getMapCompletableFuture(filterPredicate);
    }

    @Override
    public CompletableFuture<Map<Resource, ConnectionDetail>> getResourcesByFormat(final GetResourcesByFormatRequest request) {
        final Predicate<HDFSResourceDetails> filterPredicate = resourceDetails -> request.getFormat().equals(resourceDetails.getFormat());
        return getMapCompletableFuture(filterPredicate);
    }

    @Override
    public CompletableFuture<Boolean> addResource(final AddResourceRequest request) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    private CompletableFuture<Map<Resource, ConnectionDetail>> getMapCompletableFuture(final Predicate<HDFSResourceDetails> filterPredicate) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                final RemoteIterator<LocatedFileStatus> remoteIterator = this.fileSystem.listFiles(new Path(this.jobConf.get(RESOURCE_ROOT_PATH)), true);
                return getFileNames(remoteIterator)
                        .stream()
                        .map(HDFSResourceDetails::getResourceDetailsFromFileName)
                        .filter(filterPredicate)
                        .map(resourceDetails -> new FileResource(resourceDetails.getId(), resourceDetails.getType(), resourceDetails.getFormat()))
                        .collect(Collectors.toMap(fileResource -> fileResource, ignore -> new NullConnectionDetail()));

            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    protected static Collection<String> getFileNames(final RemoteIterator<LocatedFileStatus> remoteIterator) throws IOException {
        final ArrayList<String> names = Lists.newArrayList();
        while (remoteIterator.hasNext()) {
            final LocatedFileStatus next = remoteIterator.next();
            final String name = next.getPath().getName();
            names.add(name);
        }
        return names;
    }


}

