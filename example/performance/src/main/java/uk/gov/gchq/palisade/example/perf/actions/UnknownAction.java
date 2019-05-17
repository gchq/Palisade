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

package uk.gov.gchq.palisade.example.perf.actions;

/**
 * Subclass of usage to print an error.
 */
public class UnknownAction extends UsageAction {
    @Override
    public Integer apply(final String[] strings) {
        System.err.println("\nERROR: unknown action, please choose one from below!\n");
        System.err.flush();
        return super.apply(strings);
    }
}
