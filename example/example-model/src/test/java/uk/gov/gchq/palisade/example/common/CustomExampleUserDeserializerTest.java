package uk.gov.gchq.palisade.example.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CustomExampleUserDeserializerTest {

    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws Exception {
        SimpleModule module = CustomExampleUserSerializer.getModule();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(module);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testExampleSerializer() {

        //given

        //when
        objectMapper.readValue(json, clazz);
        //then
    }


}