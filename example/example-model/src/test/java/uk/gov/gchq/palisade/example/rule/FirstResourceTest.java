/*
 * Copyright 2019 Crown Copyright
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

package uk.gov.gchq.palisade.example.rule;

import org.junit.Test;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.example.common.Role;
import uk.gov.gchq.palisade.resource.impl.FileResource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class FirstResourceTest {

    private static final User TEST_USER_HR = new User().userId("1").roles(Role.HR.name());
    private static final User TEST_USER_NOT_HR = new User().userId("1").roles("Not HR");
    private static final FileResource TEST_RESOURCE = new FileResource();
    private static final String FILE_ID_1 = "file1.avro";
    private static final String FILE_ID_2 = "file.other.file2.avro";
    private static final FirstResourceRule RESOURCE_RULE = new FirstResourceRule();

    @Test
    public void hrGetFirstFile() {
        //Given - FileId, User
        TEST_RESOURCE.setId(FILE_ID_1);

        //When
        FileResource actual = RESOURCE_RULE.apply(TEST_RESOURCE, TEST_USER_HR);

        //Then
        assertEquals(TEST_RESOURCE, actual);
    }

    @Test
    public void nonHrGetFirstFile() {
        //Given - FileId, User
        TEST_RESOURCE.setId(FILE_ID_1);

        //When
        FileResource actual = RESOURCE_RULE.apply(TEST_RESOURCE, TEST_USER_NOT_HR);

        //Then
        assertNull(actual);
    }

    @Test
    public void hrGetSecondFile() {
        //Given - FileId, User
        TEST_RESOURCE.setId(FILE_ID_2);

        //When
        FileResource actual = RESOURCE_RULE.apply(TEST_RESOURCE, TEST_USER_HR);

        //Then
        assertEquals(TEST_RESOURCE, actual);
    }

    @Test
    public void nonHrGetSecondFile() {
        //Given - FileId, User
        TEST_RESOURCE.setId(FILE_ID_2);

        //When
        FileResource actual = RESOURCE_RULE.apply(TEST_RESOURCE, TEST_USER_NOT_HR);

        //Then
        assertEquals(TEST_RESOURCE, actual);
    }
}
