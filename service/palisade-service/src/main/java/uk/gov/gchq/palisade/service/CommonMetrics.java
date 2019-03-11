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

package uk.gov.gchq.palisade.service;

/**
 * The list of common Palisade metric names. All Palisade instances support this set.
 *
 * @see PalisadeMetricProvider
 */
public enum CommonMetrics {

    CURRENT_RESOURCE_COUNT("palisade.current.resource.count");

    /**
     * The common metric name.
     */
    private final String metricName;

    /**
     * Private constructor for enum.
     *
     * @param metricName the metric name
     */
    CommonMetrics(final String metricName) {
        this.metricName = metricName;
    }

    /**
     * Get the metric name for this enum instance.
     *
     * @return the metric name.
     */
    public String getMetricName() {
        return metricName;
    }
}
