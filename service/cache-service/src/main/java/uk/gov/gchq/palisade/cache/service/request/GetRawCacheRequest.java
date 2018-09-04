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
package uk.gov.gchq.palisade.cache.service.request;

import java.util.function.BiFunction;

/**
 * An get cache request class that allows retrieving of raw byte[] arrays into a cache service.
 */
public class GetRawCacheRequest extends GetCacheRequest<byte[]> {

    /**
     * {@inheritDoc}
     *
     * @return a function that always returns the byte array unchanged
     */
    @Override
    public BiFunction<byte[], Class<byte[]>, byte[]> getValueDecoder() {
        return (x, y) -> x;
    }
}
