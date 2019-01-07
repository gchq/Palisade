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

package uk.gov.gchq.palisade.cache.service.impl;

import org.junit.Test;
import uk.gov.gchq.palisade.cache.service.CacheService;

public class HeartbeatIT {

    private static CacheService cache=new SimpleCacheService().backingStore(new HeartbeatTestBackingStore(true));

    @Test
    public void shouldStartAndStopAndBeVisible() {
    
    }

    //tests needed
    /*
    create a single one start it ..check..stop it..check its stopped
    create two check they are both there
    create two from different services, check no overlap
    create one check name is a match

     */
}