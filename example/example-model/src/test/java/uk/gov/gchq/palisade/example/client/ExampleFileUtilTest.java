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

package uk.gov.gchq.palisade.example.client;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.gov.gchq.palisade.example.util.ExampleFileUtil;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class ExampleFileUtilTest {

    private static Path temp;

    @BeforeClass
    public static void createDummy() throws Exception {
        temp = Files.createTempFile("test", "file");
    }

    @AfterClass
    public static void removeDummy() {
        FileUtils.deleteQuietly(temp.toFile());
    }

    @Test(expected = NullPointerException.class)
    public void throwNPE() {
        //Given - nothing

        //When
        ExampleFileUtil.convertToFileURI(null);

        //Then
        fail("exception expected");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwOnEmpty() {
        //Given - nothing

        //When
        ExampleFileUtil.convertToFileURI("");

        //Then
        fail("exception expected");
    }

    @Test
    public void shouldConvertLocalRelativePath() {
        //Given
        String input = temp.getParent().toString() + "/./././././././" + temp.getFileName();
        String expected = (System.getProperty("os.name").toLowerCase().contains("win") ? "file:///" : "file://") + temp.toString().replace("\\", "/");

        //When
        String actual = ExampleFileUtil.convertToFileURI(input).toString();

        //Then
        assertThat(actual, is(equalTo(expected)));
    }

    @Test
    public void shouldConvertLocalAbsolutePath() {
        //Given
        String input = temp.toString();
        String expected = (System.getProperty("os.name").toLowerCase().contains("win") ? "file:///" : "file://") + temp.toString().replace("\\", "/");

        //When
        String actual = ExampleFileUtil.convertToFileURI(input).toString();

        //Then
        assertThat(actual, is(equalTo(expected)));
    }

    @Test
    public void shouldConvertFileSchemeWithRelative() {
        //Given
        String input = (System.getProperty("os.name").toLowerCase().contains("win") ? "file:///" : "file://") + temp.getParent().toString().replace("\\", "/") + "/././././" + temp.getFileName().toString();
        String expected = (System.getProperty("os.name").toLowerCase().contains("win") ? "file:///" : "file://") + temp.toString().replace("\\", "/");

        //When
        String actual = ExampleFileUtil.convertToFileURI(input).toString();

        //Then
        assertThat(actual, is(equalTo(expected)));
    }

    @Test
    public void shouldConvertFileSchemeWithAbsolute() {
        //Given
        String input = (System.getProperty("os.name").toLowerCase().contains("win") ? "file:///" : "file://") + temp.toString().replace("\\", "/");
        String expected = (System.getProperty("os.name").toLowerCase().contains("win") ? "file:///" : "file://") + temp.toString().replace("\\", "/");

        //When
        String actual = ExampleFileUtil.convertToFileURI(input).toString();

        //Then
        assertThat(actual, is(equalTo(expected)));
    }

    @Test
    public void shouldConvertFileSchemeWithSingleSlash() {
        //Given
        String input = (System.getProperty("os.name").toLowerCase().contains("win") ? "file:/" : "file:") + temp.toString().replace("\\", "/");
        String expected = (System.getProperty("os.name").toLowerCase().contains("win") ? "file://" : "file:/") + temp.toString().replace("\\", "/");

        //When
        String actual = ExampleFileUtil.convertToFileURI(input).toString();

        //Then
        assertThat(actual, is(equalTo(expected)));
    }

    @Test
    public void shouldNotChangeOtherProtocol() {
        //Given
        String expected = "hdfs://some/path/to/some/file";

        //When
        String actual = ExampleFileUtil.convertToFileURI(expected).toString();

        //Then
        assertThat(actual, is(equalTo(expected)));
    }

    @Test
    public void shouldNotChangeNonExistentFile() {
        //Given
        String expected = Paths.get(URI.create("file:///nowhere/no_file")).toUri().toString();

        //When
        String actual = ExampleFileUtil.convertToFileURI(expected).toString();

        //Then
        assertThat(actual, is(equalTo(expected)));
    }
}
