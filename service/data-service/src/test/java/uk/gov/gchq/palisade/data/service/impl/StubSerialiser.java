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

import uk.gov.gchq.palisade.data.serialise.LineSerialiser;

public final class StubSerialiser extends LineSerialiser {

    @Override
    public String serialiseLine(Object obj) {
        return null;
    }

    @Override
    public Object deserialiseLine(String line) {
        return null;
    }

    /**
     * All are equal.
     *
     * @return
     */
    @Override
    public int hashCode() {
        return 0;
    }

    /**
     * All instances are equal.
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        return true;
    }
}
