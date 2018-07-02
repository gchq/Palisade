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

import org.junit.Test;

import uk.gov.gchq.palisade.data.service.NullDataService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RestDataServiceV1Test {
    @Test
    public void shouldLoadServiceFromConfig() {
        // Given

        // When
        final RestDataServiceV1 service = new RestDataServiceV1("/config.json");

        // Then
        assertNotNull(service);
        assertEquals(NullDataService.class, service.getDelegate().getClass());
    }
}
