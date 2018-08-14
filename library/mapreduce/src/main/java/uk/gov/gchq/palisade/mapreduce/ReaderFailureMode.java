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
package uk.gov.gchq.palisade.mapreduce;

/**
 * This enum allows the specification of how a {@link PalisadeRecordReader} should behave on failure to connect to a
 * {@link uk.gov.gchq.palisade.data.service.DataService}. As a {@link PalisadeRecordReader} may have several resources
 * to process for one map task of a MapReduce job, the user may wish the job to continue in the face of a failed read of
 * a Resource or may a job to terminate. This can be set by calling {@link PalisadeInputFormat#setResourceErrorBehaviour(org.apache.hadoop.mapreduce.JobContext,
 * ReaderFailureMode)}. All failures will be logged and a counter set on the task.
 */
public enum ReaderFailureMode {
    /**
     * Cause the entire task to fail when a {@link uk.gov.gchq.palisade.resource.Resource} cannot be retrieved from a
     * {@link uk.gov.gchq.palisade.data.service.DataService}.
     */
    FAIL_ON_READ_FAILURE,
    /**
     * Allow the task to continue when a {@link uk.gov.gchq.palisade.resource.Resource} cannot be retrieved.
     */
    CONTINUE_ON_READ_FAILURE,
}
