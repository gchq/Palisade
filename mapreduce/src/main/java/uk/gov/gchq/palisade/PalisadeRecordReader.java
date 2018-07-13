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
package uk.gov.gchq.palisade;

import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import java.io.IOException;
import java.util.Objects;

public class PalisadeRecordReader extends RecordReader {

    public PalisadeRecordReader() {
    }

    @Override
    public void initialize(final InputSplit inputSplit, final TaskAttemptContext taskAttemptContext) throws IOException, InterruptedException {
        Objects.requireNonNull(inputSplit, "inputSplit");
        Objects.requireNonNull(taskAttemptContext, "taskAttemptContext");
        if (inputSplit instanceof PalisadeInputSplit) {
            throw new ClassCastException("input split MUST be instance of " + PalisadeInputSplit.class.getName());
        }
    }

    @Override
    public boolean nextKeyValue() throws IOException, InterruptedException {
        return false;
    }

    @Override
    public Object getCurrentKey() throws IOException, InterruptedException {
        return null;
    }

    @Override
    public Object getCurrentValue() throws IOException, InterruptedException {
        return null;
    }

    @Override
    public float getProgress() throws IOException, InterruptedException {
        return (float) Math.random(); //hehehe
    }

    @Override
    public void close() throws IOException {

    }
}
