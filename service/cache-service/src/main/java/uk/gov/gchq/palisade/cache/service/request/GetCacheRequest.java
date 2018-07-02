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

import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.service.request.Request;

/**
 * This class is used for sending a request to get the
 * {@link uk.gov.gchq.palisade.service.request.DataRequestConfig} out of the
 * cache for the given {@link RequestId}.
 */
public class GetCacheRequest extends Request {
    private RequestId requestId;

    public GetCacheRequest() {
    }

    public GetCacheRequest(final RequestId requestId) {
        this.requestId = requestId;
    }

    public RequestId getRequestId() {
        return requestId;
    }

    public void setRequestId(final RequestId requestId) {
        this.requestId = requestId;
    }
}
