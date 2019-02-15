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

package uk.gov.gchq.palisade.redirect.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.redirect.RedirectionResult;
import uk.gov.gchq.palisade.redirect.exception.NoInstanceException;
import uk.gov.gchq.palisade.redirect.result.StringRedirectResult;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An example redirector that should not be used in production! This simple redirector just picks a live instance of the
 * given service at random to redirect a request to.
 */
public class SimpleRandomRedirector extends HeartbeatRedirector<String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleRandomRedirector.class);

    /**
     * {@inheritDoc}
     * <p>
     * This redirector redirects all requests randomly to a live instance.
     */
    @Override
    public RedirectionResult<String> redirectionFor(final String host, final Method method, final Object... args) throws NoInstanceException {
        //get list of live services
        List<String> liveInstances = getScope().auscultate().collect(Collectors.toList());

        //randomize and return
        Collections.shuffle(liveInstances);
        return liveInstances
                .stream()
                .peek(destination -> LOGGER.debug("Call from host {} to \"{}\" being redirected to {}", host, method.getDeclaringClass().getTypeName() + "." + method.getName(), destination))
                .findFirst()
                .map(StringRedirectResult::new)
                .orElseThrow(() -> new NoInstanceException("no live instances of " + super.getRedirectionClass() + " could be found"));
    }
}
