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

import uk.gov.gchq.palisade.ToStringBuilder;

import java.util.regex.Pattern;

/**
 * A storage class for the resources within {@link HDFSResourceService}.
 * <p>
 * This class has logic for manipulating the path of the resource into [connectionDetail, type, format]
 */
public class HDFSResourceDetails {
    public static final String TYPE_DEL = "_";
    public static final String FORMAT_DEL = ".";
    public static final String FILE_NAME_FORMAT = "%s" + TYPE_DEL + "%s" + FORMAT_DEL + "%s";
    private String connectionDetail, type, format;

    public HDFSResourceDetails(final String connectionDetail, final String type, final String format) {
        this.connectionDetail = connectionDetail;
        this.type = type;
        this.format = format;
    }

    public String getConnectionDetail() {
        return connectionDetail;
    }

    public String getType() {
        return type;
    }

    public String getFormat() {
        return format;
    }

    protected static HDFSResourceDetails getResourceDetailsFromConnectionDetails(final String connectionDetail) {
        //The mirror of the FILE_NAME_FORMAT
        final String[] split = connectionDetail.split(Pattern.quote("/"));
        final String fileString = split[split.length - 1];
        final String[] typeSplit = fileString.split(TYPE_DEL);
        if (typeSplit.length == 2) {
            final String type = typeSplit[0];
            final String[] idSplit = typeSplit[1].split(Pattern.quote(FORMAT_DEL));
            if (idSplit.length == 2) {
                final String name = idSplit[0];
                final String format = idSplit[1];

                return new HDFSResourceDetails(connectionDetail, type, format);
            }
        }
        throw new IllegalArgumentException("Incorrect format expected:" + FILE_NAME_FORMAT + " found: " + fileString);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("type", type)
                .append("id", connectionDetail)
                .append("format", format)
                .build();
    }
}
