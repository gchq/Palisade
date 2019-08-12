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

package uk.gov.gchq.palisade.data.service.impl;

import uk.gov.gchq.palisade.data.service.reader.CachedSerialisedDataReader;
import uk.gov.gchq.palisade.resource.LeafResource;

import java.io.InputStream;

import static java.util.Objects.requireNonNull;

public final class TestDataReader extends CachedSerialisedDataReader {

    private final InputStream returnStream;

    public TestDataReader(final InputStream returnStream) {
        requireNonNull(returnStream, "returnStream");
        this.returnStream = returnStream;
    }

    @Override
    protected InputStream readRaw(LeafResource resource) {
        return returnStream;
    }
}