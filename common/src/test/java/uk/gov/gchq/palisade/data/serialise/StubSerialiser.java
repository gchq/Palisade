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
package uk.gov.gchq.palisade.data.serialise;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class StubSerialiser<T> implements Serialiser<T,String> {

    public String getT() {
        return t;
    }

    public void setT(String t) {
        this.t = t;
    }

    private String t;

    public StubSerialiser() {}
    public StubSerialiser(String t) {
        this.t=t;
    }

    @Override
    public T serialise(String object) {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        StubSerialiser<?> that = (StubSerialiser<?>) o;

        return new EqualsBuilder()
                .append(t, that.t)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(t)
                .toHashCode();
    }

    @Override
    public String deserialise(T form) {
        return null;
    }
}
