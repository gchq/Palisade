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

package uk.gov.gchq.palisade.example.client;

import uk.gov.gchq.palisade.client.ConfiguredClientServices;
import uk.gov.gchq.palisade.client.ServicesFactory;
import uk.gov.gchq.palisade.client.SimpleClient;
import uk.gov.gchq.palisade.config.service.ConfigurationService;
import uk.gov.gchq.palisade.example.ExampleObj;
import uk.gov.gchq.palisade.example.config.ServicesConfigurator;
import uk.gov.gchq.palisade.example.data.serialiser.ExampleObjSerialiser;
import uk.gov.gchq.palisade.example.util.ExampleFileUtil;
import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.palisade.rest.RestUtil;
import uk.gov.gchq.palisade.util.StreamUtil;

import java.io.InputStream;
import java.net.URI;
import java.util.stream.Stream;

public class ExampleSimpleClient extends SimpleClient<ExampleObj> {
    private final String file;

    public static void main(final String[] args) {
        final InputStream stream = StreamUtil.openStream(ExampleSimpleClient.class, System.getProperty(RestUtil.CONFIG_SERVICE_PATH));
        ConfigurationService configService = JSONSerialiser.deserialise(stream, ConfigurationService.class);
        ConfiguredClientServices cs = new ConfiguredClientServices(configService);
        new ExampleSimpleClient(cs, args[0]);
    }

    public ExampleSimpleClient(final ServicesFactory services, final String file) {
        super(services, new ExampleObjSerialiser());
        URI absoluteFileURI = ExampleFileUtil.convertToFileURI(file);
        this.file = absoluteFileURI.toString();
    }

    public Stream<ExampleObj> read(final String filename, final String userId, final String justification) {
        URI absoluteFileURI = ExampleFileUtil.convertToFileURI(filename);
        String absoluteFile = absoluteFileURI.toString();
        return super.read(absoluteFile, ServicesConfigurator.RESOURCE_TYPE, userId, justification);
    }

    /**
     * Gets the file passed at construction as a fully qualified URI.
     *
     * @return the absolute URI file path
     */
    public String getURIConvertedFile() {
        return file;
    }
}
