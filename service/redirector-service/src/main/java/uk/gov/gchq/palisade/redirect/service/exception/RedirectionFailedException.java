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

package uk.gov.gchq.palisade.redirect.service.exception;

/**
 * An exception thrown by redirectors when no redirection can occur for some reason. The standard {@link Throwable} only
 * and no-arg constructor have been deleted since a message MUST be supplied.
 */
public class RedirectionFailedException extends RuntimeException {
    public RedirectionFailedException(final String e) {
        super(e);
    }

    public RedirectionFailedException(final String e, final Throwable cause) {
        super(e, cause);
    }
}
