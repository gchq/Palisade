/*
 * Copyright 2019 Crown Copyright
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

package uk.gov.gchq.palisade.service;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import uk.gov.gchq.palisade.ToStringBuilder;

import static java.util.Objects.requireNonNull;

public class EgeriaConnection implements ConnectionDetail {
    private String url;


    public EgeriaConnection(final String serverPlatformRootURL, final String serverName, final String userId) {
        requireNonNull(serverPlatformRootURL, "Egeria URL cannot be null");
        url = serverPlatformRootURL + "/servers/" + serverName + "/open-metadata/access-services/asset-owner/users/" + userId + "/assets/";
    }

    @Override
    public <S extends Service> S createService() {
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

        final EgeriaConnection that = (EgeriaConnection) o;

        return new EqualsBuilder()
                .append(url, that.url)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 41)
                .append(url)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("url", url)
                .toString();
    }
}
