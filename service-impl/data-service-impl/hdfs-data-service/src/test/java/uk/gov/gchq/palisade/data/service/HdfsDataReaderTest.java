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
package uk.gov.gchq.palisade.data.service;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import uk.gov.gchq.palisade.Justification;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.data.serialise.SimpleStringSerialiser;
import uk.gov.gchq.palisade.data.service.reader.HdfsDataReader;
import uk.gov.gchq.palisade.data.service.reader.request.DataReaderRequest;
import uk.gov.gchq.palisade.data.service.reader.request.DataReaderResponse;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.rule.Rules;
import uk.gov.gchq.palisade.util.TestUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class HdfsDataReaderTest {
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder(TestUtil.TMP_DIRECTORY);

    @Test
    public void shouldReadTextFileWithNoRules() throws IOException {
        // Given
        final File tmpFile = testFolder.newFile("file1.txt");
        FileUtils.write(tmpFile, "some data\nsome more data");

        final Configuration conf = new Configuration();
        final HdfsDataReader reader = new HdfsDataReader(conf);

        final FileResource resource = new FileResource().id(tmpFile.getAbsolutePath()).type("string");
        final Rules<String> rules = new Rules<>();

        final DataReaderRequest request = new DataReaderRequest()
                .resource(resource)
                .user(new User())
                .justification(new Justification())
                .rules(rules);

        // When
        final DataReaderResponse response = reader.read(request);

        // Then
        final InputStream stream = response.getData();
        final Stream<String> lines = new BufferedReader(new InputStreamReader(stream)).lines();
        assertEquals(Arrays.asList("some data", "some more data"), lines.collect(Collectors.toList()));
    }

    @Test
    public void shouldReadTextFileWithRules() throws IOException {
        // Given
        final File tmpFile = testFolder.newFile("file1.txt");
        FileUtils.write(tmpFile, "some data\nsome more data");

        final Configuration conf = new Configuration();
        final HdfsDataReader reader = new HdfsDataReader(conf);
        reader.addSerialiser("string", new SimpleStringSerialiser());

        final FileResource resource = new FileResource().id(tmpFile.getAbsolutePath()).type("string");
        // Redact any records containing the word 'more'
        final Rules<String> rules = new Rules<String>().predicateRule("1", (r, u, j) -> !r.contains("more"));

        final DataReaderRequest request = new DataReaderRequest()
                .resource(resource)
                .user(new User())
                .justification(new Justification())
                .rules(rules);

        // When
        final DataReaderResponse response = reader.read(request);

        // Then
        final InputStream stream = response.getData();
        final Stream<String> lines = new BufferedReader(new InputStreamReader(stream)).lines();
        assertEquals(Collections.singletonList("some data"), lines.collect(Collectors.toList()));
    }
}
