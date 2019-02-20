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

import uk.gov.gchq.palisade.rest.SystemProperty;

/**
 * Contains the main method to launch a redirector.
 */
public final class Launcher {

    private Launcher() {
    }

    public static void main(final String[] args) throws Exception {
        //Create a REST Redirector which will cause it to launch and attmept to connect to the configuration service.
        RESTRedirector<?, ?> redirectService = new RESTRedirector<>();
        //read the base path from the system property
        String basePath = System.getProperty(SystemProperty.BASE_PATH, SystemProperty.BASE_PATH_DEFAULT);
        //launch
        EmbeddedHttpServer server = new EmbeddedHttpServer(basePath, redirectService);
        server.startServer();
    }
}
