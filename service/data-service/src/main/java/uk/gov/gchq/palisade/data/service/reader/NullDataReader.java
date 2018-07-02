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

import uk.gov.gchq.palisade.data.service.reader.request.DataReaderRequest;
import uk.gov.gchq.palisade.data.service.reader.request.DataReaderResponse;

/**
 * A null implementation of the {@link NullDataReader} that prevents hitting
 * {@link NullPointerException}s if your deployment does not require a
 * {@link NullDataReader}, but one is expected.
 */
public class NullDataReader implements DataReader {
    @Override
    public <RAW_DATA_TYPE, RULES_DATA_TYPE> DataReaderResponse<RAW_DATA_TYPE> read(final DataReaderRequest<RULES_DATA_TYPE> request) {
        return new DataReaderResponse<>();
    }
}
