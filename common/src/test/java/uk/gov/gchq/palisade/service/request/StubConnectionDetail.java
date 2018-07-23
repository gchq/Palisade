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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import uk.gov.gchq.palisade.ToStringBuilder;
import uk.gov.gchq.palisade.service.Service;

public class StubConnectionDetail implements ConnectionDetail {

    public StubConnectionDetail(String con) {
        this.con = con;
    }

    public StubConnectionDetail() {

    }

    private String con;

    private Service serviceToCreate;

    @Override
    public <S extends Service> S createService() {
        return (S) getServiceToCreate();
    }

    public String getCon() {
        return con;
    }

    public void setCon(String con) {
        this.con = con;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final StubConnectionDetail stub = (StubConnectionDetail) o;

        return new EqualsBuilder()
                .append(con, stub.con)
                .append(getServiceToCreate(), stub.getServiceToCreate())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 11)
                .append(con)
                .append(getServiceToCreate())
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("con", con)
                .append("serviceToCreate", getServiceToCreate())
                .toString();
    }

    public Service getServiceToCreate() {
        return serviceToCreate;
    }

    public StubConnectionDetail setServiceToCreate(Service serviceToCreate) {
        this.serviceToCreate = serviceToCreate;
        return this;
    }
}
