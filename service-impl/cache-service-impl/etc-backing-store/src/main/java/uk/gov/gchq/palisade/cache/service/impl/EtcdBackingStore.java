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

import java.time.Duration;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class EtcdBackingStore implements BackingStore {
    @Override
    public boolean store(String key, Class<?> valueClass, byte[] value, Optional<Duration> timeToLive) {
        String cachedKey = BackingStore.keyCheck(key);
        requireNonNull(valueClass, "valueClass");
        requireNonNull(value, "value");
        BackingStore.durationCheck(timeToLive);

        return false;
    }

    @Override
    public BasicCacheObject retrieve(String key) {
        String cachedKey = BackingStore.keyCheck(key);
        return null;
    }

    @Override
    public Stream<String> list(String prefix) {
        requireNonNull(prefix, "prefix");
        return null;
    }
}
