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

package uk.gov.gchq.palisade.audit.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.audit.service.AuditService;
import uk.gov.gchq.palisade.audit.service.request.AuditRequest;
import uk.gov.gchq.palisade.exception.NoConfigException;
import uk.gov.gchq.palisade.service.ServiceState;

import java.util.concurrent.CompletableFuture;

import static java.util.Objects.requireNonNull;

/**
 * A LoggerAuditService is a simple implementation of an {@link AuditService} that simply constructs a message and logs
 * it using log4j {@link Logger}. <ul> <li>Messages are logged at INFO logging level.</li> <li>Error messages are logged
 * at ERROR logging level.</li> </ul> <p> An example message is: </p>
 * <pre>
 * 'Alice' accessed 'file1' for 'Payroll' and it was processed using 'Age off and visibility filtering'
 * </pre>
 */
public class LoggerAuditService implements AuditService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerAuditService.class);

    @Override
    public CompletableFuture<Boolean> audit(final AuditRequest request) {
        requireNonNull(request, "The audit request can not be null.");
        final String msg = "'" + request.getUser().getUserId().getId()
                + "' accessed '" + request.getResource().getId()
                + "' for '" + request.getContext().getJustification()
                + "' and it was processed using '" + request.getHowItWasProcessed() + "'";

        if (null != request.getException()) {
            LOGGER.error(msg + "', but an error occurred " + request.getException().getMessage(), request.getException());
        } else {
            LOGGER.info(msg);
        }
        return CompletableFuture.completedFuture(Boolean.TRUE);
    }

    @Override
    public void recordCurrentConfigTo(final ServiceState config) {
        requireNonNull(config, "config");
        config.put(AuditService.class.getTypeName(), getClass().getTypeName());
        LOGGER.debug("Wrote configuration data: no-op");
    }

    @Override
    public void applyConfigFrom(final ServiceState config) throws NoConfigException {
        requireNonNull(config, "config");
        LOGGER.debug("Read configuration data: no-op");
    }
}
