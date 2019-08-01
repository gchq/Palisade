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

import org.apache.commons.io.output.NullOutputStream;
import org.junit.Test;
import org.mockito.Mockito;
import uk.gov.gchq.palisade.data.service.reader.request.DataReaderResponse;
import uk.gov.gchq.palisade.data.service.reader.request.ResponseWriter;
import uk.gov.gchq.palisade.data.service.request.ReadResponse;

import java.io.IOException;
import java.io.OutputStream;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class ReadResponseTest {

    @Test
    public void shouldCloseResponseWriterNormally() throws IOException {
        //Given
        ResponseWriter writer = Mockito.mock(ResponseWriter.class);
        when(writer.write(any(OutputStream.class))).thenReturn(writer);

        DataReaderResponse testResponse = new DataReaderResponse().writer(writer);

        //When
        ReadResponse resp = ReadResponse.createNoInputResponse(testResponse);
        resp.writeTo(NullOutputStream.NULL_OUTPUT_STREAM);

        //Then
        // ensure write then close called
        verify(writer).write(any(OutputStream.class));
        verify(writer).close();
        verifyNoMoreInteractions(writer);
    }

    @Test
    public void shouldCloseResponseWriterExceptionally() throws IOException {
        //Given
        ResponseWriter writer = Mockito.mock(ResponseWriter.class);
        //throw an exception when write called
        when(writer.write(any(OutputStream.class))).thenThrow(IOException.class);

        DataReaderResponse testResponse = new DataReaderResponse().writer(writer);

        //When
        ReadResponse resp = ReadResponse.createNoInputResponse(testResponse);
        try {
            resp.writeTo(NullOutputStream.NULL_OUTPUT_STREAM);
        } catch (IOException expected) {

        }

        //Then
        // ensure write then close called
        verify(writer).write(any(OutputStream.class));
        verify(writer).close();
        verifyNoMoreInteractions(writer);
    }
}
