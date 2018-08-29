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
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.CommonConfigurationKeysPublic;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.resource.ChildResource;
import uk.gov.gchq.palisade.resource.impl.DirectoryResource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.impl.SystemResource;
import uk.gov.gchq.palisade.resource.service.request.AddResourceRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByIdRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByResourceRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesBySerialisedFormatRequest;
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
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

/**
 * A implementation of the ResourceService for HDFS.
 * <p>
 * This service is for the retrieval of Resources only. Resources cannot be added via this service,
 * they should be added through the actual real HDFS.
 *
 * @see ResourceService
 */
@JsonPropertyOrder(value = {"class", "conf", "dataFormat", "dataType"}, alphabetic = true)
public class HDFSResourceService implements ResourceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(HDFSResourceService.class);
    public static final String ERROR_ADD_RESOURCE = "AddResource is not supported by HDFSResourceService resources should be added/created via regular HDFS behaviour.";
    public static final String ERROR_OUT_SCOPE = "resource ID is out of scope of the this resource Service. Found: %s expected: %s";
    public static final String ERROR_DETAIL_NOT_FOUND = "Connection detail could not be found for type: %s format: %s";
    public static final String ERROR_RESOLVING_PARENTS = "Error occurred while resolving resourceParents";

    private final Configuration conf;
    private final FileSystem fileSystem;
    private ConnectionDetailStorage connectionDetailStorage;

    public HDFSResourceService(final Configuration conf, final HashMap<String, ConnectionDetail> dataFormat, final HashMap<String, ConnectionDetail> dataType) throws IOException {
        requireNonNull(conf);
        this.conf = conf;
        this.fileSystem = FileSystem.get(conf);
        this.connectionDetailStorage = new ConnectionDetailStorage(dataFormat, dataType);
    }

    @JsonCreator
    public HDFSResourceService(@JsonProperty("conf") final Map<String, String> conf,
                               @JsonProperty("dataFormat") final HashMap<String, ConnectionDetail> dataFormat,
                               @JsonProperty("dataType") final HashMap<String, ConnectionDetail> dataType) throws IOException {
        this(createConfig(conf), dataFormat, dataType);
    }

    public HDFSResourceService connectionDetail(final Map<String, ConnectionDetail> dataFormat, final Map<String, ConnectionDetail> dataType) {
        this.connectionDetailStorage = new ConnectionDetailStorage(dataFormat, dataType);
        return this;
    }

    @Override
    public CompletableFuture<Map<uk.gov.gchq.palisade.resource.LeafResource, ConnectionDetail>> getResourcesByResource(final GetResourcesByResourceRequest request) {
        return getResourcesById(new GetResourcesByIdRequest().resourceId(request.getResource().getId()));
    }

    @Override
    public CompletableFuture<Map<uk.gov.gchq.palisade.resource.LeafResource, ConnectionDetail>> getResourcesById(final GetResourcesByIdRequest request) {
        final String resourceId = request.getResourceId();
        final String path = conf.get(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY);
        if (!resourceId.startsWith(path) && !resourceId.startsWith(new Path(path).toUri().getPath())) {
            throw new UnsupportedOperationException(java.lang.String.format(ERROR_OUT_SCOPE, resourceId, path));
        }
        return getMapCompletableFuture(resourceId, ignore -> true);
    }

    private CompletableFuture<Map<uk.gov.gchq.palisade.resource.LeafResource, ConnectionDetail>> getMapCompletableFuture(final String pathString, final Predicate<HDFSResourceDetails> predicate) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                final RemoteIterator<LocatedFileStatus> remoteIterator = this.fileSystem.listFiles(new Path(pathString), true);
                return getPaths(remoteIterator)
                        .stream()
                        .map(HDFSResourceDetails::getResourceDetailsFromConnectionDetails)
                        .filter(predicate)
                        .collect(Collectors.toMap(
                                (HDFSResourceDetails resourceDetails) -> {
                                    final String connectionDetail = resourceDetails.getConnectionDetail();
                                    final FileResource fileFileResource = new FileResource().id(connectionDetail).type(resourceDetails.getType()).serialisedFormat(resourceDetails.getFormat());
                                    resolveParents(fileFileResource, conf);
                                    return fileFileResource;
                                },
                                resourceDetails -> connectionDetailStorage.get(resourceDetails.getType(), resourceDetails.getFormat())
                        ));
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void resolveParents(final ChildResource resource, final Configuration conf) {
        try {
            final String connectionDetail = resource.getId();
            final Path path = new Path(connectionDetail);
            final int fileDepth = path.depth();
            final int fsDepth = new Path(conf.get(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY)).depth();

            if (fileDepth > fsDepth + 1) {
                DirectoryResource parent = new DirectoryResource().id(path.getParent().toString());
                resource.setParent(parent);
                resolveParents(parent, conf);
            } else {
                resource.setParent(new SystemResource().id(path.getParent().toString()));
            }
        } catch (Exception e) {
            throw new RuntimeException(ERROR_RESOLVING_PARENTS, e);
        }
    }

    @Override
    public CompletableFuture<Map<uk.gov.gchq.palisade.resource.LeafResource, ConnectionDetail>> getResourcesByType(final GetResourcesByTypeRequest request) {
        final String pathString = conf.get(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY);
        final Predicate<HDFSResourceDetails> predicate = detail -> request.getType().equals(detail.getType());
        return getMapCompletableFuture(pathString, predicate);
    }

    @Override
    public CompletableFuture<Map<uk.gov.gchq.palisade.resource.LeafResource, ConnectionDetail>> getResourcesBySerialisedFormat(final GetResourcesBySerialisedFormatRequest request) {
        final String pathString = conf.get(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY);
        final Predicate<HDFSResourceDetails> predicate = detail -> request.getSerialisedFormat().equals(detail.getFormat());
        return getMapCompletableFuture(pathString, predicate);
    }

    @Override
    public CompletableFuture<Boolean> addResource(final AddResourceRequest request) {
        throw new UnsupportedOperationException(ERROR_ADD_RESOURCE);
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
        return connectionDetailStorage.getDataType();
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
    public HashMap<String, ConnectionDetail> getDataFormat() {
        return connectionDetailStorage.getDataFormat();
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
    public Map<String, String> getConf() {
        Map<String, String> rtn = Maps.newHashMap();
        Map<String, String> plainJobConfWithoutResolvingValues = getPlainJobConfWithoutResolvingValues();

        for (Entry<String, String> entry : conf) {
            final String plainValue = plainJobConfWithoutResolvingValues.get(entry.getKey());
            final String thisValue = entry.getValue();
            if (isNull(plainValue) || !plainValue.equals(thisValue)) {
                rtn.put(entry.getKey(), entry.getValue());
            }
        }
        return rtn;
    }

    private Map<String, String> getPlainJobConfWithoutResolvingValues() {
        Map<String, String> plainMapWithoutResolvingValues = new HashMap<>();
        for (Entry<String, String> entry : new Configuration()) {
            plainMapWithoutResolvingValues.put(entry.getKey(), entry.getValue());
        }
        return plainMapWithoutResolvingValues;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final HDFSResourceService that = (HDFSResourceService) o;

        final EqualsBuilder builder = new EqualsBuilder()
                .append(this.fileSystem, that.fileSystem)
                .append(this.connectionDetailStorage, that.connectionDetailStorage);

        if (builder.isEquals()) {
            builder.append(this.conf.size(), that.conf.size());
            for (Entry<String, String> entry : this.conf) {
                final String lhs = this.conf.get(entry.getKey());
                final String rhs = that.conf.get(entry.getKey());
                builder.append(lhs, rhs);
                if (!builder.isEquals()) {
                    break;
                }
            }
        }

        return builder.isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(conf)
                .append(fileSystem)
                .append(connectionDetailStorage)
                .toHashCode();
    }

    private class ConnectionDetailStorage {
        private final HashMap<String, ConnectionDetail> dataFormat = new HashMap<>();
        private final HashMap<String, ConnectionDetail> dataType = new HashMap<>();

        ConnectionDetailStorage(final Map<String, ConnectionDetail> dataFormat, final Map<String, ConnectionDetail> dataType) {
            if (nonNull(dataFormat)) {
                this.dataFormat.putAll(dataFormat);
                this.dataFormat.values().removeIf(Objects::isNull);
            }
            if (nonNull(dataType)) {
                this.dataType.putAll(dataType);
                this.dataType.values().removeIf(Objects::isNull);
            }
        }

        public ConnectionDetail get(final String type, final String format) {
            ConnectionDetail rtn = dataType.get(type);
            if (Objects.isNull(rtn)) {
                rtn = dataFormat.get(format);
                if (Objects.isNull(rtn)) {
                    throw new IllegalStateException(String.format(ERROR_DETAIL_NOT_FOUND, type, format));
                }
            }
            return rtn;
        }

        public HashMap<String, ConnectionDetail> getDataFormat() {
            return new HashMap<>(dataFormat);
        }

        public HashMap<String, ConnectionDetail> getDataType() {
            return new HashMap<>(dataType);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final ConnectionDetailStorage that = (ConnectionDetailStorage) o;

            return new EqualsBuilder()
                    .append(this.dataFormat, that.dataFormat)
                    .append(this.dataType, that.dataType)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .append(dataFormat)
                    .append(dataType)
                    .toHashCode();
        }
//
//        @Override
//        public String toString() {
//            return new ToStringBuilder(this)
//                    .append("dataFormat", dataFormat)
//                    .append("dataType", dataType)
//                    .toString();
//        }
    }

    private static Configuration createConfig(final Map<String, String> conf) {
        final Configuration config = new Configuration();
        if (nonNull(conf)) {
            for (final Entry<String, String> entry : conf.entrySet()) {
                config.set(entry.getKey(), entry.getValue());
            }
        }
        return config;
    }
}