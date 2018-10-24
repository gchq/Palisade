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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.CommonConfigurationKeysPublic;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.cache.service.CacheService;
import uk.gov.gchq.palisade.cache.service.request.AddCacheRequest;
import uk.gov.gchq.palisade.cache.service.request.GetCacheRequest;
import uk.gov.gchq.palisade.exception.NoConfigException;
import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.palisade.resource.ChildResource;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.impl.DirectoryResource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.impl.SystemResource;
import uk.gov.gchq.palisade.resource.service.request.AddResourceRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByIdRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByResourceRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesBySerialisedFormatRequest;
import uk.gov.gchq.palisade.resource.service.request.GetResourcesByTypeRequest;
import uk.gov.gchq.palisade.service.request.ConnectionDetail;
import uk.gov.gchq.palisade.service.request.InitialConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
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
 * This service is for the retrieval of Resources only. Resources cannot be added via this service, they should be added
 * through the actual real HDFS.
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

    public static final String HADOOP_CONF_STRING = "hdfs.init.conf";
    public static final String CACHE_IMPL_KEY = "hdfs.cache.svc";
    public static final String CONNECTION_DETAIL_KEY = "hdfs.conn.detail";

    private Configuration conf;
    private FileSystem fileSystem;

    private CacheService cacheService;

    @JsonIgnore
    private ConnectionDetailStorage connectionDetailStorage = new ConnectionDetailStorage();

    public HDFSResourceService() {
    }

    public HDFSResourceService(final Configuration conf, final CacheService cache) throws IOException {
        requireNonNull(conf, "conf");
        requireNonNull(cache, "cache");
        this.conf = conf;
        this.fileSystem = FileSystem.get(conf);
        this.cacheService = cache;
    }

    @JsonCreator
    public HDFSResourceService(@JsonProperty("conf") final Map<String, String> conf,
                               @JsonProperty("cacheService") final CacheService cache) throws IOException {
        this(createConfig(conf), cache);
    }

    public HDFSResourceService cacheService(final CacheService cacheService) {
        requireNonNull(cacheService, "Cache service cannot be set to null.");
        this.cacheService = cacheService;
        return this;
    }

    public void setCacheService(final CacheService cacheService) {
        cacheService(cacheService);
    }

    public CacheService getCacheService() {
        requireNonNull(cacheService, "The cache service has not been set.");
        return cacheService;
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

    /**
     * Write the internal details to the given cache.
     */
    private void writeConnectionDetails() {
        final CacheService cache = getCacheService();
        AddCacheRequest<ConnectionDetailStorage> dataFormatRequest = new AddCacheRequest<>()
                .service(HDFSResourceService.class)
                .key(CONNECTION_DETAIL_KEY)
                .value(this.connectionDetailStorage);
        cache.add(dataFormatRequest).join();
    }

    /**
     * Read the internal details from the given cache.
     */
    private void readConnectionDetails() {
        final CacheService cache = getCacheService();
        GetCacheRequest<ConnectionDetailStorage> dataFormatRequest = new GetCacheRequest<>()
                .service(HDFSResourceService.class)
                .key(CONNECTION_DETAIL_KEY);
        Optional<ConnectionDetailStorage> formats = cache.get(dataFormatRequest).join();
        formats.ifPresent(cds -> connectionDetail(cds.getDataFormat(), cds.getDataType(), true));
    }

    @Override
    public void configure(final InitialConfig config) throws NoConfigException {
        requireNonNull(config, "config");
        //get the configuration string
        String serialisedConfig = config.getOrDefault(HADOOP_CONF_STRING, null);
        //make this into a map
        Map<String, String> confMap = null;
        if (nonNull(serialisedConfig)) {
            confMap = JSONSerialiser.deserialise(serialisedConfig.getBytes(JSONSerialiser.UTF8), Map.class);
        }
        //make this into a config, confMap may be null at this point
        Configuration newConf = createConfig(confMap);
        this.conf = newConf;
        try {
            this.fileSystem = FileSystem.get(this.conf);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //extract cache
        String serialisedCache = config.getOrDefault(CACHE_IMPL_KEY, null);
        if (nonNull(serialisedCache)) {
            cacheService = JSONSerialiser.deserialise(serialisedCache.getBytes(JSONSerialiser.UTF8), CacheService.class);
        } else {
            throw new NoConfigException("no cache service specified in configuration");
        }
    }

    @Override
    public void writeConfiguration(final InitialConfig config) {
        requireNonNull(config, "config");
        Map<String, String> confMap = getConf();
        String serialisedConf = new String(JSONSerialiser.serialise(confMap), JSONSerialiser.UTF8);
        config.put(HADOOP_CONF_STRING, serialisedConf);
        String serialisedCache = new String(JSONSerialiser.serialise(cacheService), JSONSerialiser.UTF8);
        config.put(CACHE_IMPL_KEY, serialisedCache);
    }

    protected Configuration getInternalConf() {
        requireNonNull(conf, "configuration must be set");
        return conf;
    }

    protected FileSystem getFileSystem() {
        requireNonNull(fileSystem, "configuration must be set");
        return fileSystem;
    }

    public HDFSResourceService connectionDetail(final Map<String, ConnectionDetail> dataFormat, final Map<String, ConnectionDetail> dataType) {
        return connectionDetail(dataFormat, dataType, false);
    }

    private HDFSResourceService connectionDetail(final Map<String, ConnectionDetail> dataFormat, final Map<String, ConnectionDetail> dataType, final boolean skipWrite) {
        this.connectionDetailStorage = new ConnectionDetailStorage(dataFormat, dataType);
        if (!skipWrite) {
            writeConnectionDetails();
        }
        return this;
    }

    @Override
    public CompletableFuture<Map<LeafResource, ConnectionDetail>>
    getResourcesByResource(final GetResourcesByResourceRequest request) {
        return getResourcesById(new GetResourcesByIdRequest().resourceId(request.getResource().getId()));
    }

    @Override
    public CompletableFuture<Map<LeafResource, ConnectionDetail>> getResourcesById(final GetResourcesByIdRequest request) {
        final String resourceId = request.getResourceId();
        final String path = getInternalConf().get(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY);
        if (!resourceId.startsWith(path) && !resourceId.startsWith(new Path(path).toUri().getPath())) {
            throw new UnsupportedOperationException(java.lang.String.format(ERROR_OUT_SCOPE, resourceId, path));
        }
        return getMapCompletableFuture(resourceId, ignore -> true);
    }

    private CompletableFuture<Map<LeafResource, ConnectionDetail>> getMapCompletableFuture(
            final String pathString, final Predicate<HDFSResourceDetails> predicate) {
        readConnectionDetails();
        return CompletableFuture.supplyAsync(() -> {
            try {
                //pull latest connection details
                final RemoteIterator<LocatedFileStatus> remoteIterator = this.getFileSystem().listFiles(new Path(pathString), true);
                return getPaths(remoteIterator)
                        .stream()
                        .map(HDFSResourceDetails::getResourceDetailsFromConnectionDetails)
                        .filter(predicate)
                        .collect(Collectors.toMap(
                                (HDFSResourceDetails resourceDetails) -> {
                                    final String connectionDetail = resourceDetails.getConnectionDetail();
                                    final FileResource fileFileResource = new FileResource().id(connectionDetail).type(resourceDetails.getType()).serialisedFormat(resourceDetails.getFormat());
                                    resolveParents(fileFileResource, getInternalConf());
                                    return fileFileResource;
                                },
                                resourceDetails -> this.connectionDetailStorage.get(resourceDetails.getType(), resourceDetails.getFormat())

                        ));
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void resolveParents(final ChildResource resource, final Configuration configuration) {
        try {
            final String connectionDetail = resource.getId();
            final Path path = new Path(connectionDetail);
            final int fileDepth = path.depth();
            final int fsDepth = new Path(configuration.get(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY)).depth();

            if (fileDepth > fsDepth + 1) {
                DirectoryResource parent = new DirectoryResource().id(path.getParent().toString());
                resource.setParent(parent);
                resolveParents(parent, configuration);
            } else {
                resource.setParent(new SystemResource().id(path.getParent().toString()));
            }
        } catch (Exception e) {
            throw new RuntimeException(ERROR_RESOLVING_PARENTS, e);
        }
    }

    @Override
    public CompletableFuture<Map<LeafResource, ConnectionDetail>> getResourcesByType(final GetResourcesByTypeRequest request) {
        final String pathString = getInternalConf().get(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY);
        final Predicate<HDFSResourceDetails> predicate = detail -> request.getType().equals(detail.getType());
        return getMapCompletableFuture(pathString, predicate);
    }

    @Override
    public CompletableFuture<Map<LeafResource, ConnectionDetail>> getResourcesBySerialisedFormat(final GetResourcesBySerialisedFormatRequest request) {
        final String pathString = getInternalConf().get(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY);
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

    @JsonIgnore
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
    public Map<String, ConnectionDetail> getDataType() {
        return this.connectionDetailStorage.getDataType();
    }

    @JsonIgnore
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
    public Map<String, ConnectionDetail> getDataFormat() {
        return this.connectionDetailStorage.getDataFormat();
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
    public Map<String, String> getConf() {
        Map<String, String> rtn = Maps.newHashMap();
        Map<String, String> plainJobConfWithoutResolvingValues = getPlainJobConfWithoutResolvingValues();

        for (Entry<String, String> entry : getInternalConf()) {
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
                .append(this.getFileSystem(), that.getFileSystem())
                .append(this.cacheService, that.cacheService);

        if (builder.isEquals()) {
            builder.append(this.getInternalConf().size(), that.getInternalConf().size());
            for (Entry<String, String> entry : this.getInternalConf()) {
                final String lhs = this.getInternalConf().get(entry.getKey());
                final String rhs = that.getInternalConf().get(entry.getKey());
                builder.append(lhs, rhs);
                if (!builder.isEquals()) {
                    break;
                }
            }
        }

        return builder.isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("conf", conf)
                .append("fileSystem", fileSystem)
                .append("cacheService", cacheService)
                .toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(conf)
                .append(fileSystem)
                .toHashCode();
    }

    public static class ConnectionDetailStorage {
        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
        private final Map<String, ConnectionDetail> dataFormat = new HashMap<>();
        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
        private final Map<String, ConnectionDetail> dataType = new HashMap<>();

        ConnectionDetailStorage() {
        }

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

        public Map<String, ConnectionDetail> getDataFormat() {
            return new HashMap<>(dataFormat);
        }

        public Map<String, ConnectionDetail> getDataType() {
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

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .appendSuper(super.toString())
                    .append("dataFormat", dataFormat)
                    .append("dataType", dataType)
                    .toString();
        }
    }
}
