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

package uk.gov.gchq.palisade.service.request;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import uk.gov.gchq.palisade.ToStringBuilder;
import uk.gov.gchq.palisade.service.NullService;
import uk.gov.gchq.palisade.service.Service;

/**
 * A simple implementation of the {@link ConnectionDetail} that holds an instance
 * of {@link Service}
 */
public class SimpleConnectionDetail implements ConnectionDetail {
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.CLASS,
            include = As.PROPERTY,
            property = "class"
    )
    private Service service;

    public SimpleConnectionDetail() {
        service(new NullService());
    }

    public SimpleConnectionDetail service(final Service service) {
        this.service = service;
        return this;
    }

    public Service getService() {
        return service;
    }

    public void setService(final Service service) {
        this.service = service;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <S extends Service> S createService() {
        return (S) service;
    }

    @Override
    public String _getClass() {
        return null;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final SimpleConnectionDetail that = (SimpleConnectionDetail) o;

        return new EqualsBuilder()
                .append(service, that.service)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 41)
                .append(service)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("service", service)
                .toString();
    }
}
