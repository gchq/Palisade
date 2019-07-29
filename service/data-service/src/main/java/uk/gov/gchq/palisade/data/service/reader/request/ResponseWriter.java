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

package uk.gov.gchq.palisade.data.service.reader.request;

import uk.gov.gchq.palisade.data.service.reader.DataReader;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This interface is used by {@link uk.gov.gchq.palisade.data.service.reader.DataReader} instances to provide a facility
 * for {@link uk.gov.gchq.palisade.data.service.DataService} implementations to retrieve the {@link uk.gov.gchq.palisade.service.request.DataRequestResponse}
 * and examine the response metadata (e.g. schema) before asking for the Palisade filtered data to be written to an {@link OutputStream}.
 */
public interface ResponseWriter extends AutoCloseable {

    /**
     * Instruct the {@link uk.gov.gchq.palisade.data.service.reader.DataReader} that created this object to begin the process
     * of reading the data from the backing store, performing any necessary rules applications to it and then writing
     * it to the {@link OutputStream} provided. This is a serial operation that will block the current thread until writing
     * is complete. The provided stream will NOT be closed once the data has been written. It is the responsibility of the code
     * that created {@code output} to close it.
     *
     * @param output the stream to write to
     * @return this object
     * @throws IOException if the data could not be written
     */
    ResponseWriter write(final OutputStream output) throws IOException;

    /**
     * Get the reader instance that created this response.
     *
     * @return the associated data reader
     */
    DataReader getReader();
}
