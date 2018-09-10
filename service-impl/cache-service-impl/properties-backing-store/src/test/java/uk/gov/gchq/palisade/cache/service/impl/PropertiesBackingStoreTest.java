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
package uk.gov.gchq.palisade.cache.service.impl;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PropertiesBackingStoreTest extends AbstractBackingStoreTest {

    private static Path testFile;

    @BeforeClass
    public static void createTestStore() throws IOException {
        testFile = Files.createTempFile(null, null);
    }

    @AfterClass
    public static void removeTestStore() throws IOException {
        Files.deleteIfExists(testFile);
    }

    @Override
    public BackingStore createBackingStore() {
        return new PropertiesBackingStore(testFile.toAbsolutePath().toString());
    }
}