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

package uk.gov.gchq.palisade.redirect.service.redirect;

/**
 * The result of a redirection request for an API call to a Palisade service. Instances of implementing classes are generated
 * by a {@link RedirectionMarshall} and are not intended to be created by client code.
 *
 * @param <T> the type of redirection, typically a host/port pair or a URL for example
 */
public interface RedirectionResult<T> {
    /**
     * Get the result of the redirection request.
     *
     * @return the result object
     */
    T get();
}
