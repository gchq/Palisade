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

package uk.gov.gchq.palisade.jsonserialisation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.BeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import com.google.common.collect.Sets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A {@code JSONSerialiser} provides the ability to serialise and deserialise to/from JSON.
 * The serialisation is set to not include nulls or default values.
 * <p>
 * JSONSerialiser is a singleton. The behaviour of the {@link ObjectMapper}
 * can be configured by extending this class and configuring the ObjectMapper.
 * Child classes must has a default no argument constructor. You will then need
 * to set the palisade.serialiser.json.class property in your StoreProperties or
 * as a System Property. You can also add modules to the ObjectMapper by writing
 * an implementation of {@link JSONSerialiserModules} and registering it using the
 * palisade.serialiser.json.modules property in your StoreProperties or
 * as a System Property.
 * </p>
 * <p>
 * Once the singleton instance has been instantiated it will not be updated,
 * unless update() or update(jsonSerialiserClass, jsonSerialiserModules) is called.
 * An update will be done automatically in the REST API when it is first initialised and
 * also when a Store is initialised.
 * </p>
 */
public class JSONSerialiser {
    public static final String JSON_SERIALISER_CLASS_KEY = "palisade.serialiser.json.class";
    /**
     * CSV of {@link JSONSerialiserModules} class names. These modules will
     * be added to the {@link ObjectMapper}.
     */
    public static final String JSON_SERIALISER_MODULES = "palisade.serialiser.json.modules";
    public static final String DEFAULT_SERIALISER_CLASS_NAME = JSONSerialiser.class.getName();

    public static final String STRICT_JSON = "palisade.serialiser.json.strict";
    public static final boolean STRICT_JSON_DEFAULT = true;
    private static final String STRICT_JSON_DEFAULT_STR = Boolean.toString(STRICT_JSON_DEFAULT);

    public static final String FILTER_FIELDS_BY_NAME = "filterFieldsByName";

    private static final JsonFactory JSON_FACTORY = new JsonFactory();
    private static final Logger LOGGER = LoggerFactory.getLogger(JSONSerialiser.class);

    private static JSONSerialiser instance;

    private final ObjectMapper mapper;

    /**
     * Constructs a {@code JSONSerialiser} that skips nulls and default values.
     */
    protected JSONSerialiser() {
        this(createDefaultMapper());
    }

    /**
     * Constructs a {@code JSONSerialiser} with a custom {@link ObjectMapper}.
     * To create the custom ObjectMapper it is advised that you start with the
     * default mapper provided from JSONSerialiser.createDefaultMapper() then
     * add your custom configuration.
     *
     * @param mapper a custom object mapper
     */
    protected JSONSerialiser(final ObjectMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Child classes of this JSONSerialiser can call this method to register
     * custom modules.
     *
     * @param modules the {@link ObjectMapper} modules to register
     */
    protected void registerModules(final Module... modules) {
        for (final Module module : modules) {
            mapper.registerModule(module);
        }
    }

    /**
     * Child classes of this JSONSerialiser can call this method to register
     * custom modules.
     *
     * @param modules the {@link ObjectMapper} modules to register
     */
    protected void registerModules(final Collection<Module> modules) {
        modules.forEach(mapper::registerModule);
    }

    /**
     * Update the json serialiser with the provided custom properties.
     *
     * @param jsonSerialiserClass   the json serialiser class to use (or null to use the default)
     * @param jsonSerialiserModules any extra json serialiser modules required
     * @param strictJson            true if strict json conversion should be used
     */
    public static void update(final String jsonSerialiserClass,
                              final String jsonSerialiserModules,
                              final Boolean strictJson) {
        if (null != jsonSerialiserModules && !jsonSerialiserModules.isEmpty()) {
            final String modulesCsv = deduplicateConcat(
                    System.getProperty(JSON_SERIALISER_MODULES),
                    jsonSerialiserModules
            );
            System.setProperty(JSON_SERIALISER_MODULES, modulesCsv);
        }

        if (null != jsonSerialiserClass) {
            System.setProperty(JSON_SERIALISER_CLASS_KEY, jsonSerialiserClass);
        }

        if (null != strictJson) {
            System.setProperty(STRICT_JSON, strictJson.toString());
        }

        update();
    }

    public static void update() {
        final String jsonSerialiserClass = System.getProperty(JSON_SERIALISER_CLASS_KEY, DEFAULT_SERIALISER_CLASS_NAME);
        final JSONSerialiser newInstance;
        try {
            newInstance = Class.forName(jsonSerialiserClass).asSubclass(JSONSerialiser.class).newInstance();
        } catch (final InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new IllegalArgumentException("Property " + JSON_SERIALISER_CLASS_KEY + " must be set to a class that is a sub class of " + JSONSerialiser.class.getName() + ". This class is not valid: " + jsonSerialiserClass, e);
        }

        final String moduleFactories = System.getProperty(JSON_SERIALISER_MODULES, "");
        final Set<String> factoryClasses = Sets.newHashSet(moduleFactories.split(","));
        factoryClasses.remove("");
        for (final String factoryClass : factoryClasses) {
            final JSONSerialiserModules factory;
            try {
                factory = Class.forName(factoryClass).asSubclass(JSONSerialiserModules.class).newInstance();
            } catch (final InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                throw new IllegalArgumentException("Property " + JSON_SERIALISER_MODULES + " must be set to a csv of classes that are a sub class of " + JSONSerialiserModules.class.getName() + ". These classes are not valid: " + factoryClass, e);
            }
            final List<Module> modules = factory.getModules();
            if (null != modules) {
                newInstance.mapper.registerModules(modules);
            }
        }

        newInstance.mapper.configure(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                Boolean.parseBoolean(System.getProperty(STRICT_JSON, STRICT_JSON_DEFAULT_STR))
        );

        instance = newInstance;
        LOGGER.debug("Updated json serialiser to use: {}, and modules: {}", jsonSerialiserClass, moduleFactories);
    }

    public static ObjectMapper createDefaultMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationFeature.CLOSE_CLOSEABLE, true);
        mapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);

        // Allow unknown properties. This will help to avoid conflicts between Palisade versions.
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, STRICT_JSON_DEFAULT);

        // Using the deprecated version for compatibility with older versions of jackson
        mapper.registerModule(new JSR310Module());

        mapper.registerModule(ResourceKeySerialiser.getModule());

        // Use the 'setFilters' method so it is compatible with older versions of jackson
        mapper.setFilters(getFilterProvider());

        return mapper;
    }

    public static FilterProvider getFilterProvider(final String... fieldsToExclude) {
        if (null == fieldsToExclude || fieldsToExclude.length == 0) {
            // Use the 'serializeAllExcept' method so it is compatible with older versions of jackson
            return new SimpleFilterProvider()
                    .addFilter(FILTER_FIELDS_BY_NAME, (BeanPropertyFilter) SimpleBeanPropertyFilter.serializeAllExcept());
        }

        return new SimpleFilterProvider()
                .addFilter(FILTER_FIELDS_BY_NAME, (BeanPropertyFilter) SimpleBeanPropertyFilter.serializeAllExcept(fieldsToExclude));
    }

    /**
     * @param clazz the clazz of the object to be serialised/deserialised
     * @return true if the clazz can be serialised/deserialised
     */
    public static boolean canHandle(final Class clazz) {
        return getInstance().mapper.canSerialize(clazz);
    }

    /**
     * Serialises an object.
     *
     * @param object          the object to be serialised
     * @param fieldsToExclude optional property names to exclude from the json
     * @return the provided object serialised into bytes
     * @throws RuntimeException if the object fails to be serialised
     */
    public static byte[] serialise(final Object object, final String... fieldsToExclude) {
        return serialise(object, false, fieldsToExclude);
    }


    /**
     * Serialises an object.
     *
     * @param object          the object to be serialised
     * @param prettyPrint     true if the object should be serialised with pretty printing
     * @param fieldsToExclude optional property names to exclude from the json
     * @return the provided object serialised (with pretty printing) into bytes
     * @throws RuntimeException if the object fails to serialise
     */
    public static byte[] serialise(final Object object, final boolean prettyPrint, final String... fieldsToExclude) {
        final ByteArrayBuilder byteArrayBuilder = new ByteArrayBuilder();
        try {
            serialise(object, JSON_FACTORY.createGenerator(byteArrayBuilder, JsonEncoding.UTF8), prettyPrint, fieldsToExclude);
        } catch (final IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        return byteArrayBuilder.toByteArray();
    }

    /**
     * Serialises an object using the provided json generator.
     *
     * @param object          the object to be serialised and written to file
     * @param jsonGenerator   the {@link com.fasterxml.jackson.core.JsonGenerator} to use to write the object to
     * @param prettyPrint     true if the object should be serialised with pretty printing
     * @param fieldsToExclude optional property names to exclude from the json
     * @throws RuntimeException if the object fails to serialise
     */
    public static void serialise(final Object object, final JsonGenerator jsonGenerator, final boolean prettyPrint, final String... fieldsToExclude) {
        if (prettyPrint) {
            jsonGenerator.useDefaultPrettyPrinter();
        }

        final ObjectWriter writer = getInstance().mapper.writer(getFilterProvider(fieldsToExclude));
        try {
            writer.writeValue(jsonGenerator, object);
        } catch (final IOException e) {
            throw new RuntimeException("Failed to serialise object to json: " + e.getMessage(), e);
        }
    }

    /**
     * @param json  the json of the object to deserialise
     * @param clazz the class of the object to deserialise
     * @param <T>   the type of the object
     * @return the deserialised object
     * @throws RuntimeException if the json fails to deserialise
     */
    public static <T> T deserialise(final String json, final Class<T> clazz) {
        try {
            return getInstance().mapper.readValue(json, clazz);
        } catch (final IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * @param bytes the bytes of the object to deserialise
     * @param clazz the class of the object to deserialise
     * @param <T>   the type of the object
     * @return the deserialised object
     * @throws RuntimeException if the bytes fail to deserialise
     */
    public static <T> T deserialise(final byte[] bytes, final Class<T> clazz) {
        try {
            return getInstance().mapper.readValue(bytes, clazz);
        } catch (final IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * @param stream the {@link java.io.InputStream} containing the bytes of the object to deserialise
     * @param clazz  the class of the object to deserialise
     * @param <T>    the type of the object
     * @return the deserialised object
     * @throws RuntimeException if the bytes fail to deserialise
     */
    public static <T> T deserialise(final InputStream stream, final Class<T> clazz) {
        try (final InputStream stream2 = stream) {
            final byte[] bytes = IOUtils.toByteArray(stream2);
            return deserialise(bytes, clazz);
        } catch (final IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * @param bytes the bytes of the object to deserialise
     * @param type  the type reference of the object to deserialise
     * @param <T>   the type of the object
     * @return the deserialised object
     * @throws RuntimeException if the bytes fail to deserialise
     */
    public static <T> T deserialise(final byte[] bytes, final TypeReference<T> type) {
        try {
            return getInstance().mapper.readValue(bytes, type);
        } catch (final IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * @param stream the {@link java.io.InputStream} containing the bytes of the object to deserialise
     * @param type   the type reference of the object to deserialise
     * @param <T>    the type of the object
     * @return the deserialised object
     * @throws RuntimeException if the bytes fail to deserialise
     */
    public static <T> T deserialise(final InputStream stream, final TypeReference<T> type) {
        try (final InputStream stream2 = stream) {
            final byte[] bytes = IOUtils.toByteArray(stream2);
            return deserialise(bytes, type);
        } catch (final IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * @param content the {@link java.lang.String} containing the bytes of the object to deserialise
     * @return the deserialised object
     * @throws RuntimeException if the bytes fail to deserialise
     */
    public static JsonNode getJsonNodeFromString(final String content) {
        try {
            return getInstance().mapper.readTree(content);
        } catch (final IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @JsonIgnore
    public static ObjectMapper getMapper() {
        return getInstance().mapper;
    }

    @JsonIgnore
    public static JSONSerialiser getInstance() {
        if (null == instance) {
            update();
        }
        return instance;
    }

    private static String deduplicateConcat(final String a, final String b) {
        if (null == a) {
            return b;
        }
        if (null == b) {
            return a;
        }
        final LinkedHashSet<String> set = new LinkedHashSet<>();
        Collections.addAll(set, StringUtils.removeStart(a, ",").split(","));
        Collections.addAll(set, StringUtils.removeStart(b, ",").split(","));
        return StringUtils.join(set, ",");
    }
}
