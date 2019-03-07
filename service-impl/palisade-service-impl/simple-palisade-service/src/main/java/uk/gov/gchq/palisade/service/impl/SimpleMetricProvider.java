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

package uk.gov.gchq.palisade.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.cache.service.CacheService;
import uk.gov.gchq.palisade.cache.service.request.ListCacheRequest;
import uk.gov.gchq.palisade.service.CommonMetrics;
import uk.gov.gchq.palisade.service.MetricsProviderUtil;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * The class that provides metrics on the SimplePalisadeService. The cache service is used to find any metrics for the Palisade service.
 * This has to be co-ordinated via the cache to account for multiple Palisade service instances working in parallel.
 */
public class SimpleMetricProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleMetricProvider.class);
    /**
     * The cache service being used by this provider. This will be passed to various functions for the metrics.
     */
    private final CacheService cache;

    /**
     * The map of functions for computing the metrics.
     */
    private static final EnumMap<CommonMetrics, Function<SimpleMetricProvider, String>> METRIC_FUNCTIONS = new EnumMap<>(CommonMetrics.class);

    static {
        METRIC_FUNCTIONS.put(CommonMetrics.CURRENT_RESOURCE_COUNT, SimpleMetricProvider::getNumberResourcesBeingRequested);
    }

    /**
     * Create a metrics provider.
     *
     * @param cache the cache service to use
     */
    public SimpleMetricProvider(final CacheService cache) {
        requireNonNull(cache, "cache");
        this.cache = cache;
    }

    /**
     * Get the number of resources currently being requested by clients from this Palisade instance. This queries the cache
     * to look at all in-flight requests.
     *
     * @return the number of resources as a string
     */
    public String getNumberResourcesBeingRequested() {
        final ListCacheRequest countsRequest = new ListCacheRequest()
                .prefix(SimplePalisadeService.RES_COUNT_KEY)
                .service(SimplePalisadeService.class);
        //ask the cache for all keys starting with the metrix prefix
        Stream<String> futureCounts = cache.list(countsRequest).join();
        //each key has the count at the end to save many trips to the cache
        int totalCount = futureCounts
                .mapToInt(entry -> {
                    //map each key of the form RES_COUNT_KEY_<some_id>_count to the count as an int
                    try {
                        return Integer.parseInt(entry.substring(entry.lastIndexOf("_") + 1));
                    } catch (Exception e) {
                        LOGGER.warn("Invalid key in cache for counts: {}", entry);
                        return 0;
                    }
                })
                .sum();
        return String.valueOf(totalCount);
    }

    /**
     * Computes a map of the given metrics. The list of filters is applied to the list of known metrics so that only
     * the desired metrics are computed.
     *
     * @param filters the list of filters
     * @return a map of metrics
     * @throws IllegalArgumentException if any filter is invalid
     */
    public Map<String, String> computeMetrics(final List<String> filters) {
        requireNonNull(filters, "filters");
        //check the filters
        filters.forEach(MetricsProviderUtil::validateFilter);

        Map<String, String> metrics = new HashMap<>();
        METRIC_FUNCTIONS.entrySet()
                .parallelStream()
                .filter(entry -> anyFiltermatches(entry, filters))
                .forEach(entry -> metrics.put(entry.getKey().getMetricName(), entry.getValue().apply(this)));

        return metrics;
    }

    /**
     * Checks to see if any of the filters given matches the key in this entry.
     *
     * @param entry   the metric entry to check
     * @param filters the list of approval filters
     * @return true if one of the filters match
     */
    private static boolean anyFiltermatches(final Map.Entry<CommonMetrics, ?> entry, final List<String> filters) {
        return filters.parallelStream()
                .anyMatch(filter -> MetricsProviderUtil.filterMatches(filter, entry.getKey().getMetricName()));
    }
}
