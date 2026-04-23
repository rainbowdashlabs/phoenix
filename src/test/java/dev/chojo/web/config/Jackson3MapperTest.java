package dev.chojo.web.config;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Jackson3MapperTest {

    @Test
    void testToJsonStream() throws Exception {
        ObjectMapper mapper = JsonMapper.builder().build();
        Jackson3Mapper jackson3Mapper = new Jackson3Mapper(mapper);

        TestObject obj = new TestObject("test", 123);
        InputStream inputStream = jackson3Mapper.toJsonStream(obj, TestObject.class);

        byte[] bytes = inputStream.readAllBytes();
        String json = new String(bytes, StandardCharsets.UTF_8);

        assertEquals("{\"name\":\"test\",\"value\":123}", json);
    }

    public static record TestObject(String name, int value) {}
}
