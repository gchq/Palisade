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
package uk.gov.gchq.palisade.resource.service.impl;

import uk.gov.gchq.palisade.ToStringBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * A storage class for the resources within {@link HadoopResourceService}.
 * <p>
 * This class has logic for manipulating the path of the resource into [fileName, type, format]
 */
public class HadoopResourceDetails {
    /**
     * The name schema regex. An example is "employee_file0.avro" where employee is the type, file0 is the name and avro
     * is the format.
     */
    public static final Pattern FILENAME_PATTERN = Pattern.compile("(?<type>.+)_(?<name>.+)\\.(?<format>.+)");

    public static final String FORMAT = "TYPE_FILENAME.FORMAT";

    private String fileName, type, format;

    public HadoopResourceDetails(final String fileName, final String type, final String format) {
        this.fileName = fileName;
        this.type = type;
        this.format = format;
    }

    public String getFileName() {
        return fileName;
    }

    public String getType() {
        return type;
    }

    public String getFormat() {
        return format;
    }

    /**
     * Checks if the given name is a valid name according to the schema in {@link HadoopResourceDetails#FILENAME_PATTERN}.
     *
     * @param fileName the name to test
     * @return true if the {@code fileName} is a valid name
     */
    public static boolean isValidResourceName(final String fileName) {
        requireNonNull(fileName);
        return validateNameRegex(fileName).matches();
    }

    /**
     * Returns the result of matching {@code fileName} against the regular expression parser in {@link HadoopResourceDetails#FILENAME_PATTERN}.
     *
     * @param fileName the name to test
     * @return the relevant matcher or {@code null} if no match could be found
     */
    private static Matcher validateNameRegex(final String fileName) {
        return FILENAME_PATTERN.matcher(fileName);
    }

    protected static HadoopResourceDetails getResourceDetailsFromFileName(final String fileName) {
        //get filename component
        final String[] split = fileName.split(Pattern.quote("/"));
        final String fileString = split[split.length - 1];
        //check match
        Matcher match = validateNameRegex(fileString);
        if (!match.matches()) {
            throw new IllegalArgumentException("Filename doesn't comply with " + FORMAT + ": " + fileName);
        }

        return new HadoopResourceDetails(match.group("name"), match.group("type"), match.group("format"));
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("type", type)
                .append("fileName", fileName)
                .append("format", format)
                .build();
    }
}
