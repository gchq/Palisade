package uk.gov.gchq.palisade.example.common;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import uk.gov.gchq.palisade.UserId;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashSet;

@RunWith(JUnit4.class)
public class CustomExampleUserSerializerTest {

    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws Exception {
        SimpleModule module = CustomExampleUserSerializer.getModule();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(module);
    }

    @Test
    public void serialize() throws JsonProcessingException, IOException {

        //given
        ExampleUser exampleUser = new ExampleUser();
        exampleUser.setTrainingCompleted(TrainingCourse.PAYROLL_TRAINING_COURSE);

        UserId userId = new UserId();
        userId.setId("Test1");
        exampleUser.setUserId(userId);

        exampleUser.setField("field entry", "field value");
        exampleUser.addRoles(new HashSet<String>(Arrays.asList("roleOne", "roleTwo")));
        exampleUser.addAuths(new HashSet<String>(Arrays.asList("auth1", "auth2")));

        Writer jsonWriter = new StringWriter();
        JsonGenerator jsonGenerator = new JsonFactory().createGenerator(jsonWriter);
        jsonGenerator.useDefaultPrettyPrinter();
        SerializerProvider serializerProvider = new ObjectMapper().getSerializerProvider();

        String serialisedData = exampleUser.toString();

        System.out.println(serialisedData);


        //when
        new CustomExampleUserSerializer().serialize(exampleUser, jsonGenerator, serializerProvider);

        jsonGenerator.flush();

        System.out.println(jsonGenerator.toString());

        JsonParser parser = objectMapper.getFactory().createParser(jsonGenerator.toString());
        //then

        new CustomExampleUserDeserialiser().deserialize(parser, null);


    }
}

