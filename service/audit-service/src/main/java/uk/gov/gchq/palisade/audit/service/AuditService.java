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

package uk.gov.gchq.palisade.audit.service;

import uk.gov.gchq.palisade.audit.service.request.AuditRequest;
import uk.gov.gchq.palisade.service.Service;
import uk.gov.gchq.palisade.service.request.Request;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * The core API for the audit service.
 * This service is responsible for logging audit messages, whether that is
 * locally or to a centralised repository.
 * Implementations of the audit service may include proxies to forward the
 * messages to another audit service, aggregator's to reduce the volumes of
 * logging to be stored as well as implementations that actually write the logs
 * to storage.
 * By splitting the functionality of the audit components in this way, where
 * they all implement this interface but do some small processing before passing
 * to the next component, for example proxy - receiver - aggregator - storage.
 * It means that if we don't want to aggregate audit records then we just remove
 * the aggregator implementation when building that micro-service.
 */
public interface AuditService extends Service {

    /**
     * This method applies the functionality that the implementation of the
     * Audit Service needs to apply, whether that is to forward to request
     * somewhere else, put the request into cache so it can be aggregated with
     * other requests, or to write it to storage.
     *
     * @param request An {@link AuditRequest} object that contains the details
     *                required to create an audit log.
     */
    void audit(final AuditRequest request);

    default void audit(final List<AuditRequest> requests) {
        requests.forEach(this::audit);
    }

    @Override
    default CompletableFuture<?> process(final Request request) {
        if (request instanceof AuditRequest) {
            audit((AuditRequest) request);
            return null;
        }
        return Service.super.process(request);
    }
}
