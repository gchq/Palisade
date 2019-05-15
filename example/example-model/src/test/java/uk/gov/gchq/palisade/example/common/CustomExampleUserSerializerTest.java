package uk.gov.gchq.palisade.example.common;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

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
        exampleUser.
        Writer jsonWriter = new StringWriter();
        JsonGenerator jsonGenerator = new JsonFactory().createGenerator(jsonWriter);
        jsonGenerator.useDefaultPrettyPrinter();
        SerializerProvider serializerProvider = new ObjectMapper().getSerializerProvider();


        //when
        new CustomExampleUserSerializer().serialize(exampleUser, jsonGenerator, serializerProvider);

        //then
    }
}

