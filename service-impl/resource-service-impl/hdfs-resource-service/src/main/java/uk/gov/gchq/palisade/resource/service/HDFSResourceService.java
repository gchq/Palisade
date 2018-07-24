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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class HDFSResourceService implements ResourceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(HDFSResourceService.class);
    public static final String ADD_RESOURCE_ERROR = "AddResource is not supported by HDFSResourceService resources should be added/created via regular HDFS behaviour.";
    public static final String ERROR_OUT_SCOPE = "resource ID is out of scope of the this resource Service. Found: %s expected: %s";

    private final JobConf jobConf;
    private final FileSystem fileSystem;
    private final HashMap<String, ConnectionDetail> dataFormat = new HashMap<>();
    private final HashMap<String, ConnectionDetail> dataType = new HashMap<>();

    public HDFSResourceService(JobConf jobConf, final HashMap<String, ConnectionDetail> dataFormat, final HashMap<String, ConnectionDetail> dataType) throws IOException {
        this.jobConf = jobConf;
        this.fileSystem = FileSystem.get(jobConf);
        this.dataFormat.putAll(dataFormat);
        dataFormat.values().removeIf(Objects::isNull);
        this.dataType.putAll(dataType);
        dataType.values().removeIf(Objects::isNull);
    }

    @JsonCreator
    public HDFSResourceService(@JsonProperty("jobConf") Map<String, String> jobConf,
                               @JsonProperty("dataFormat") final HashMap<String, ConnectionDetail> dataFormat,
                               @JsonProperty("dataType") final HashMap<String, ConnectionDetail> dataType) throws IOException {
        this(new JobConf(), dataFormat, dataType);
        for (Entry<String, String> entry : jobConf.entrySet()) {
            this.jobConf.set(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public CompletableFuture<Map<Resource, ConnectionDetail>> getResourcesByResource(final GetResourcesByResourceRequest request) {
        return getResourcesById(new GetResourcesByIdRequest(request.getResource().getId()));
    }


    @Override
    public CompletableFuture<Map<Resource, ConnectionDetail>> getResourcesById(final GetResourcesByIdRequest request) {
        final String resourceId = request.getResourceId();
        if (!resourceId.contains(jobConf.get(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY))) {
            throw new UnsupportedOperationException(String.format(ERROR_OUT_SCOPE, resourceId, jobConf.get(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY)));
        }

        final Predicate<HDFSResourceDetails> predicate = (HDFSResourceDetails detail) -> nonNull(dataType.get(detail.getType()));
        final Function<HDFSResourceDetails, ConnectionDetail> connectionDetailFunction = resourceDetails -> dataType.get(resourceDetails.getType());
        return getMapCompletableFuture(resourceId, predicate, connectionDetailFunction);
    }

    private CompletableFuture<Map<Resource, ConnectionDetail>> getMapCompletableFuture(final String pathString, final Predicate<HDFSResourceDetails> predicate, final Function<HDFSResourceDetails, ConnectionDetail> connectionDetailFunction) {
        return CompletableFuture.supplyAsync(() -> {
            try {

                final RemoteIterator<LocatedFileStatus> remoteIterator = this.fileSystem.listFiles(new Path(pathString), true);
                return getPaths(remoteIterator)
                        .stream()
                        .map(HDFSResourceDetails::getResourceDetailsFromConnectionDetails)
                        .filter(predicate)
                        .collect(Collectors.toMap(
                                (HDFSResourceDetails resourceDetails) -> new FileResource(resourceDetails.getConnectionDetail(), resourceDetails.getType(), resourceDetails.getFormat()),
                                connectionDetailFunction
                        ));
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
        final Predicate<HDFSResourceDetails> predicate = detail -> nonNull(dataType.get(detail.getType())) && request.getType().equals(detail.getType());
        final Function<HDFSResourceDetails, ConnectionDetail> connectionDetailFunction = resourceDetails -> dataType.get(resourceDetails.getType());
        return getMapCompletableFuture(pathString, predicate, connectionDetailFunction);
    }

    @Override
    public CompletableFuture<Map<Resource, ConnectionDetail>> getResourcesByFormat(final GetResourcesByFormatRequest request) {
        final String pathString = jobConf.get(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY);
        final Predicate<HDFSResourceDetails> predicate = detail -> nonNull(dataFormat.get(detail.getFormat())) && request.getFormat().equals(detail.getFormat());
        final Function<HDFSResourceDetails, ConnectionDetail> connectionDetailFunction = resourceDetails -> dataFormat.get(resourceDetails.getFormat());
        return getMapCompletableFuture(pathString, predicate, connectionDetailFunction);
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

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
    public HashMap<String, ConnectionDetail> getDataType() {
        return dataType;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
    public HashMap<String, ConnectionDetail> getDataFormat() {
        return dataFormat;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
    public Map<String, String> getJobConf() {
        final JobConf plain = new JobConf();
        Map<String, String> rtn = Maps.newHashMap();
        for (Entry<String, String> entry : jobConf) {
            final String s = plain.get(entry.getKey());
            final String s2 = jobConf.get(entry.getKey());
            if (isNull(s) || !s.equals(s2)) {
                rtn.put(entry.getKey(), entry.getValue());
            }
        }
        return rtn;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        } else if (isNull(o)) {
            return false;
        }

        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(this.getClass(), o.getClass());
        if (builder.isEquals()) {
            final HDFSResourceService that = (HDFSResourceService) o;
            builder.append(this.fileSystem, that.fileSystem);
            builder.append(this.dataFormat, that.dataFormat);
            builder.append(this.dataType, that.dataType);
            if (builder.isEquals()) {
                builder.append(this.jobConf.size(), that.jobConf.size());
                for (Entry<String, String> entry : this.jobConf) {
                    final String lhs = this.jobConf.get(entry.getKey());
                    final String rhs = that.jobConf.get(entry.getKey());
                    builder.append(lhs, rhs);
                    if (!builder.isEquals()) {
                        break;
                    }
                }
            }
        }
        return builder.isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 17)
                .append(jobConf)
                .append(fileSystem)
                .append(dataFormat)
                .append(dataType)
                .build();
    }
}

