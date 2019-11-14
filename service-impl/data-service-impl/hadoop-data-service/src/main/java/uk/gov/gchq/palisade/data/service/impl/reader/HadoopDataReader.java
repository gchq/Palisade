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

package uk.gov.gchq.palisade.data.service.impl.reader;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.collect.Maps;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import uk.gov.gchq.palisade.data.service.reader.CachedSerialisedDataReader;
import uk.gov.gchq.palisade.resource.LeafResource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

/**
 * An HadoopDataReader is an implementation of {@link CachedSerialisedDataReader} for Hadoop that opens a file and returns
 * a single {@link InputStream} containing all the records.
 */
public class HadoopDataReader extends CachedSerialisedDataReader {
    @JsonIgnore
    private FileSystem fs;

    public HadoopDataReader() {
    }

    @JsonCreator
    public HadoopDataReader(@JsonProperty("conf") final Map<String, String> conf) throws IOException {
        conf(conf);
    }

    public HadoopDataReader conf(final Map<String, String> conf) throws IOException {
        requireNonNull(conf, "The conf cannot be null.");
        return conf(createConfig(conf));
    }

    public HadoopDataReader conf(final Configuration conf) throws IOException {
        requireNonNull(conf, "The conf cannot be null.");
        return fs(FileSystem.get(conf));
    }

    public HadoopDataReader fs(final FileSystem fs) {
        requireNonNull(fs, "The file system cannot be set to null.");
        this.fs = fs;
        return this;
    }

    public void setFs(final FileSystem fs) {
        fs(fs);
    }

    public FileSystem getFs() {
        requireNonNull(fs, "The file system has not been set.");
        return fs;
    }

    @Override
    protected InputStream readRaw(final LeafResource resource) {
        requireNonNull(resource, "resource is required");

        InputStream inputStream;
        try {
            //1st attempt: process this as a URI
            try {
                inputStream = fs.open(new Path(new URI(resource.getId())));
            } catch (URISyntaxException e) {
                //2nd attempt: process as a string
                inputStream = fs.open(new Path(resource.getId()));
            }
        } catch (final IOException e) {
            throw new RuntimeException("Unable to read resource: " + resource.getId(), e);
        }

        return inputStream;
    }

    public Configuration getConf() {
        return getFs().getConf();
    }

    @JsonGetter("conf")
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
    Map<String, String> getConfMap() {
        Map<String, String> rtn = Maps.newHashMap();
        Map<String, String> plainJobConfWithoutResolvingValues = getPlainJobConfWithoutResolvingValues();

        for (Entry<String, String> entry : getConf()) {
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
