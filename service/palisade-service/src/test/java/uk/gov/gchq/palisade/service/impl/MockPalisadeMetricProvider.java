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

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.mockito.Mockito;

import uk.gov.gchq.palisade.ToStringBuilder;
import uk.gov.gchq.palisade.service.PalisadeMetricProvider;
import uk.gov.gchq.palisade.service.request.GetMetricRequest;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MockPalisadeMetricProvider implements PalisadeMetricProvider {
    private static PalisadeMetricProvider mock = Mockito.mock(PalisadeMetricProvider.class);

    public static PalisadeMetricProvider getMock() {
        return mock;
    }

    public static void setMock(final PalisadeMetricProvider mock) {
        if (null == mock) {
            MockPalisadeMetricProvider.mock = Mockito.mock(PalisadeMetricProvider.class);
        }
        MockPalisadeMetricProvider.mock = mock;
    }

    @Override
    public CompletableFuture<Map<String, String>> getMetrics(final GetMetricRequest request) {
        return mock.getMetrics(request);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 17)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .toString();
    }
}
