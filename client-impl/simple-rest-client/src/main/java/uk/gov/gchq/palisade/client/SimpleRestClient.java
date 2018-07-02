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

package uk.gov.gchq.palisade.client;

import uk.gov.gchq.palisade.data.service.DataService;
import uk.gov.gchq.palisade.data.service.impl.ProxyRestDataService;
import uk.gov.gchq.palisade.data.service.reader.DataReader;
import uk.gov.gchq.palisade.policy.service.PolicyService;
import uk.gov.gchq.palisade.resource.service.ResourceService;
import uk.gov.gchq.palisade.resource.service.impl.ProxyRestResourceService;
import uk.gov.gchq.palisade.service.PalisadeService;
import uk.gov.gchq.palisade.service.impl.ProxyRestPalisadeService;
import uk.gov.gchq.palisade.service.impl.ProxyRestPolicyService;
import uk.gov.gchq.palisade.user.service.UserService;
import uk.gov.gchq.palisade.user.service.impl.ProxyRestUserService;

public class SimpleRestClient<T> extends SimpleClient<T> {
    @Override
    protected PalisadeService createPalisadeService() {
        return new ProxyRestPalisadeService("http://localhost:8080/palisade");
    }

    @Override
    protected PolicyService createPolicyService() {
        return new ProxyRestPolicyService("http://localhost:8081/policy");
    }

    @Override
    protected ResourceService createResourceService() {
        return new ProxyRestResourceService("http://localhost:8082/resource");
    }

    @Override
    protected UserService createUserService() {
        return new ProxyRestUserService("http://localhost:8083/user");
    }

    @Override
    protected DataService createDataService() {
        return new ProxyRestDataService("http://localhost:8084/data");
    }

    @Override
    protected DataReader createDataReader() {
        throw new UnsupportedOperationException("This should never be called.");
    }
}
