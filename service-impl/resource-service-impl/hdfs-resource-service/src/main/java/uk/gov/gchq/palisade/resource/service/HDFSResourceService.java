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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class HDFSResourceService implements ResourceService {
    public static final String TYPE_DEL = "_";
    public static final String FORMAT_DEL = ".";
    public static final String FILE_NAME_FORMAT = "%s" + TYPE_DEL + "%s" + FORMAT_DEL + "%s";
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
        return CompletableFuture.supplyAsync(() -> {
            try {
                final String resourceId = request.getResourceId();
                final RemoteIterator<LocatedFileStatus> remoteIterator = this.fileSystem.listFiles(new Path(this.jobConf.get(RESOURCE_ROOT_PATH)), true);
                return getNames(remoteIterator)
                        .stream()
                        .map(HDFSResourceService::getResourceDetailsFromFileName)
                        .filter(resourceDetails -> resourceId.equals(resourceDetails[FORMAT_FIELDS.id.pos]))
                        .map(resourceDetails -> new FileResource(resourceDetails[FORMAT_FIELDS.id.pos], resourceDetails[FORMAT_FIELDS.type.pos], resourceDetails[FORMAT_FIELDS.format.pos]))
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
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public CompletableFuture<Map<Resource, ConnectionDetail>> getResourcesByFormat(final GetResourcesByFormatRequest request) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public CompletableFuture<Boolean> addResource(final AddResourceRequest request) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    protected static String[] getResourceDetailsFromFileName(final String fileName) {
        //The refection of FILE_NAME_FORMAT

        final String[] typeSplit = fileName.split(TYPE_DEL);
        if (typeSplit.length == 2) {
            final String type = typeSplit[0];
            final String[] idSplit = typeSplit[1].split(Pattern.quote(FORMAT_DEL));
            if (idSplit.length == 2) {
                final String id = idSplit[0];
                final String format = idSplit[1];

                final String[] rtn = new String[FILE_NAME_FORMAT.length()];
                rtn[FORMAT_FIELDS.id.pos] = id;
                rtn[FORMAT_FIELDS.type.pos] = type;
                rtn[FORMAT_FIELDS.format.pos] = format;

                return rtn;
            }
        }
        throw new IllegalArgumentException("Incorrect format expected:" + FILE_NAME_FORMAT + " found: " + fileName);
    }

    protected static String getFileNameFromResourceDetails(final String[] resourceDetails) {
        return String.format(FILE_NAME_FORMAT, resourceDetails[FORMAT_FIELDS.type.pos], resourceDetails[FORMAT_FIELDS.id.pos], resourceDetails[FORMAT_FIELDS.format.pos]);
    }

    protected static Collection<String> getNames(final RemoteIterator<LocatedFileStatus> remoteIterator) throws IOException {
        final ArrayList<String> names = Lists.newArrayList();
        while (remoteIterator.hasNext()) {
            final LocatedFileStatus next = remoteIterator.next();
            final String name = next.getPath().getName();
            names.add(name);
        }
        return names;
    }

    public enum FORMAT_FIELDS {
        type(1), format(2), id(0);

        private final Integer pos;

        FORMAT_FIELDS(final int pos) {
            this.pos = pos;
        }

        public Integer pos() {
            return pos;
        }

        public static int length() {
            return values().length;
        }
    }
}

