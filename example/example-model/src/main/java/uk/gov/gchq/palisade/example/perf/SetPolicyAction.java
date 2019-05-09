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

package uk.gov.gchq.palisade.example.perf;

/**
 * Uses an existing deployment of Palisade to set policies on the given files. The policy from the example
 * deployment is used.
 */
public class SetPolicyAction extends PerfAction {
    @Override
    public String name() {
        return "policy";
    }

    @Override
    public String description() {
        return "sets example policies on generated files";
    }

    @Override
    public String help() {
        return "Action "+name()+" sets policies on the files previously generated," +
                "\nso that they can be read through Palisade.";
    }

    @Override
    public Integer apply(String[] strings) {
        return null;
    }
}
