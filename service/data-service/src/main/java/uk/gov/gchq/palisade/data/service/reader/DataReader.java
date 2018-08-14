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

package uk.gov.gchq.palisade.data.service.reader;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;

import uk.gov.gchq.palisade.data.service.reader.request.DataReaderRequest;
import uk.gov.gchq.palisade.data.service.reader.request.DataReaderResponse;

/**
 * The core API for the data reader.
 *
 * The responsibility of the data reader is to connect to the requested resource,
 * apply the rules, then passes back to the data service the stream of data in
 * the expected format.
 *
 * There is a utility method {@link uk.gov.gchq.palisade.Util#applyRulesToStream(java.util.stream.Stream, uk.gov.gchq.palisade.User, uk.gov.gchq.palisade.Context, uk.gov.gchq.palisade.rule.Rules)}
 * that does the part of applying the rules provided your input data is in the
 * format that the rules expect.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = As.EXISTING_PROPERTY,
        property = "class"
)
public interface DataReader {

    /**
     * This method will read the data from a single resource and apply all the rules.
     *
     * @param request {@link DataReaderRequest} containing the resource to be
     *                read, rules to be applied, the user requesting the data
     *                and the justification for accessing the data.
     * @return a {@link DataReaderRequest} that contains the stream of data.
     */
    DataReaderResponse read(final DataReaderRequest request);

    @JsonGetter("class")
    default String _getClass() {
        return getClass().getName();
    }

    @JsonSetter("class")
    default void _setClass(final String className) {
        // do nothing.
    }
}
