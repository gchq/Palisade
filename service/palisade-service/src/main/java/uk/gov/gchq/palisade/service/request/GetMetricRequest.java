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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import uk.gov.gchq.palisade.exception.ForbiddenException;
import uk.gov.gchq.palisade.service.MetricsProviderUtil;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * A request sent to retrieve details on the Palisade system itself. The request should be sent along with a list of whitelist
 * filters which specify which metrics to fetch. Filters may use a simple wildcard facility where a single '*' character
 * may be present at either the start OR end of a filter. This filter will then match any metric name that starts or ends
 * with the rest of the filter as appropriate. There may be only one wildcard used per filter and it cannot occur in the middle
 * of a filter.
 * <p>
 * Examples:
 * <p>
 * "hello" matches "hello" alone
 * <p>
 * "hello*" matches "hello.metric1" and "hello.metric2"
 * <p>
 * "*hello" matches "server1.metric.hello" and "server2.metric.hello"
 *
 * @see uk.gov.gchq.palisade.service.CommonMetrics
 */
@JsonIgnoreProperties(value = {"originalRequestId"})
public class GetMetricRequest extends Request {
    private final List<String> patternFilter = new ArrayList<>();

    //no-arg constructor required
    public GetMetricRequest() {
    }

    /**
     * Add the given filter to the list of filters.
     *
     * @param filter the filter to add
     * @return this object
     * @throws IllegalArgumentException if the filter is invalid
     */
    public GetMetricRequest addFilter(final String filter) {
        requireNonNull(filter, "filter");
        MetricsProviderUtil.validateFilter(filter);
        patternFilter.add(filter);
        return this;
    }

    /**
     * Add all the strings in the given list to the request filters.
     *
     * @param filters the filters to add
     * @return this object
     * @throws IllegalArgumentException if a filter is invalid
     */
    public GetMetricRequest addFilter(final List<String> filters) {
        requireNonNull(filters, "filters");
        filters.forEach(this::addFilter);
        return this;
    }

    /**
     * Set the filter list to the ones given. The filter list is cleared and the items from the list added.
     *
     * @param filters the replacement filter list
     * @return this object
     * @throws IllegalArgumentException if a filter is invalid
     */
    public GetMetricRequest filters(final List<String> filters) {
        requireNonNull(filters, "filters");
        patternFilter.clear();
        addFilter(filters);
        return this;
    }

    /**
     * Set the filter list to the ones given. The filter list is cleared and the items from the list added.
     *
     * @param filters the replacement filter list
     * @throws IllegalArgumentException if a filter is invalid
     */
    public void setFilters(final List<String> filters) {
        filters(filters);
    }

    /**
     * Gets a copy of the filter list
     *
     * @return the filter list
     */
    public List<String> getFilters() {
        List<String> filterList = new ArrayList<>(patternFilter);
        return filterList;
    }

    @Override
    public void setOriginalRequestId(final String originalRequestId) {
        throw new ForbiddenException("Should not call GetMetricRequest.setOriginalRequestId()");
    }

    @Override
    public String getOriginalRequestId() {
        throw new ForbiddenException("Should not call GetMetricRequest.getOriginalRequestId()");
    }


    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GetMetricRequest that = (GetMetricRequest) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(patternFilter, that.patternFilter)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(11, 37)
                .appendSuper(super.hashCode())
                .append(patternFilter)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("patternFilter", patternFilter)
                .toString();
    }
}
