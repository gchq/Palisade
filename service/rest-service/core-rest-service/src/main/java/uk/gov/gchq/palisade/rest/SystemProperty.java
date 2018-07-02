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

package uk.gov.gchq.palisade.rest;

import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.palisade.rest.factory.UnknownUserFactory;
import uk.gov.gchq.palisade.util.DebugUtil;

/**
 * System property keys and default values.
 */
public abstract class SystemProperty {
    // KEYS
    public static final String BASE_PATH = "palisade.rest.basePath";
    public static final String REST_API_VERSION = "palisade.rest.version";
    public static final String USER_FACTORY_CLASS = "palisade.user.factory.class";
    public static final String SERVICES_PACKAGE_PREFIX = "palisade.rest.resourcePackage";
    public static final String PACKAGE_PREFIXES = "palisade.package.prefixes";
    public static final String JSON_SERIALISER_CLASS = JSONSerialiser.JSON_SERIALISER_CLASS_KEY;
    public static final String JSON_SERIALISER_MODULES = JSONSerialiser.JSON_SERIALISER_MODULES;
    public static final String REST_DEBUG = DebugUtil.DEBUG;

    // Exposed Property Keys
    /**
     * A CSV of properties to expose via the properties endpoint.
     */
    public static final String EXPOSED_PROPERTIES = "palisade.properties";
    public static final String APP_TITLE = "palisade.properties.app.title";
    public static final String APP_DESCRIPTION = "palisade.properties.app.description";
    public static final String APP_BANNER_COLOUR = "palisade.properties.app.banner.colour";
    public static final String APP_BANNER_DESCRIPTION = "palisade.properties.app.banner.description";
    public static final String APP_DOCUMENTATION_URL = "palisade.properties.app.doc.url";
    public static final String LOGO_LINK = "palisade.properties.app.logo.link";
    public static final String LOGO_IMAGE_URL = "palisade.properties.app.logo.src";
    public static final String FAVICON_SMALL_URL = "palisade.properties.app.logo.favicon.small";
    public static final String FAVICON_LARGE_URL = "palisade.properties.app.logo.favicon.large";

    // DEFAULTS
    /**
     * Comma separated list of package prefixes to search for Functions.
     */
    public static final String PACKAGE_PREFIXES_DEFAULT = "uk.gov.gchq";
    public static final String SERVICES_PACKAGE_PREFIX_DEFAULT = "uk.gov.gchq.palisade.rest";
    public static final String BASE_PATH_DEFAULT = "rest";
    public static final String CORE_VERSION = "0.0.1";
    public static final String USER_FACTORY_CLASS_DEFAULT = UnknownUserFactory.class.getName();
    public static final String REST_DEBUG_DEFAULT = DebugUtil.DEBUG_DEFAULT;
    public static final String APP_TITLE_DEFAULT = "Palisade REST";
    public static final String APP_DESCRIPTION_DEFAULT = "The Palisade REST service.";
    public static final String APP_DOCUMENTATION_URL_DEFAULT = "https://gchq.github.io/palisade/";
    public static final String LOGO_LINK_DEFAULT = "https://github.com/gchq/Palisade";
    public static final String LOGO_IMAGE_URL_DEFAULT = "images/logo.png";

    private SystemProperty() {
        // Private constructor to prevent instantiation.
    }
}
