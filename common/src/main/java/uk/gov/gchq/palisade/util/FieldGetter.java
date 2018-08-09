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

package uk.gov.gchq.palisade.util;

import java.util.function.BiFunction;


/**
 * A {@code FieldGetter} is a {@link BiFunction} that takes an object and a field name
 * and returns the value of that field.
 *
 * @param <T> the type of object
 */
public interface FieldGetter<T> extends BiFunction<T, String, Object> {
}
