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
import org.codehaus.jackson.annotate.JsonIgnore;

import uk.gov.gchq.palisade.ToStringBuilder;

import java.util.UUID;

import static java.util.Objects.requireNonNull;

/**
 * This is the high level API for any request sent to a service.
 * This makes sure each request has a unique identifier.
 */
public abstract class Request {
    private String id; //this is a unique ID for each individual request made between services
    private String originalRequestId; //this Id is unique between requests


    public Request() {
        id(UUID.randomUUID().toString());
        originalRequestId = new String(id); //for the moment just default originalRequestId to id - this will need to
        // be set for all services - for example Beat will need to set this to a unique identifer representing the particular Beat
        // use a new instance rather than pointing to the original
    }


    public Request id(final String id) {
        requireNonNull(id, "The id cannot be set to null.");
        this.id = id;
        return this;
    }

    public Request originalRequestId(final String originalRequestId) {
        requireNonNull(originalRequestId, "The id cannot be set to null.");
        this.originalRequestId = originalRequestId;
        return this;
    }

    public void setId(final String id) {
        id(id);
    }

    public String getId() {
        //id will never be null
        return id;
    }

    @JsonIgnore
    public void setOriginalRequestId(final String originalRequestId) {
        this.originalRequestId = originalRequestId;
    }

    @JsonIgnore
    public String getOriginalRequestId() {
        requireNonNull(originalRequestId, "The originalRequestId type cannot be null");
        return originalRequestId;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Request request = (Request) o;

        return new EqualsBuilder()
                .append(id, request.id)
                .append(originalRequestId, request.originalRequestId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(5, 37)
                .append(id)
                .append(originalRequestId)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("originalRequestId", originalRequestId)
                .toString();
    }
}
