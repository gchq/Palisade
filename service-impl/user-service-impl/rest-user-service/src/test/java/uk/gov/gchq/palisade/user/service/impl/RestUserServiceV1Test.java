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

package uk.gov.gchq.palisade.user.service.impl;

import org.junit.Test;

import uk.gov.gchq.palisade.user.service.NullUserService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RestUserServiceV1Test {
    @Test
    public void shouldLoadServiceFromConfig() {
        // Given

        // When
        final RestUserServiceV1 service = new RestUserServiceV1("/config.json");

        // Then
        assertNotNull(service);
        assertEquals(NullUserService.class, service.getDelegate().getClass());
    }
}
