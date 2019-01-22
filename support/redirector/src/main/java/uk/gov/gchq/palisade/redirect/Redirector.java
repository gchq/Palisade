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

package uk.gov.gchq.palisade.redirect;

import uk.gov.gchq.palisade.redirect.exception.NoInstanceException;

import java.lang.reflect.Method;

/**
 * The base interface for a redirector. A redirector knows how to redirect requests based on some specific algorithm and/or
 * metadata. For example, a round-robin redirector may simply send successive requests to the next host on a circular list,
 * whereas another redirector may redirect based on load on the respective hosts, whilst another may take data locality
 * of the request into account.
 *
 * @param <T> the type of result that the redirector can respond with
 */
public interface Redirector<T> {

    /**
     * Ask this redirector to find a suitable destination for the given request. As this method must be generic enough to handle
     * any Palisade service, then it carries both a reference to the original method called as well all the
     * accompanying arguments.
     *
     * @param method the API method that was called originally by the client
     * @param args   the arguments to that request
     * @return a redirection result
     * @throws NoInstanceException if the redirector could not find any suitable service instance to redirect to
     */
    RedirectionResult<T> determineRedirection(final Method method, final Object... args) throws NoInstanceException;
}
