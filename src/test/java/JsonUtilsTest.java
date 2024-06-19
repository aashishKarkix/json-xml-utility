import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import json.utility.JsonUtils;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class JsonUtilsTest {

    static class Person {
        @JsonProperty("name")
        public String name;
        @JsonProperty("age")
        public int age;

        public Person() {
        }

        @JsonCreator
        public Person(@JsonProperty("name") String name, @JsonProperty("age") int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Person person = (Person) o;

            if (age != person.age) return false;
            return Objects.equals(name, person.name);
        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + age;
            return result;
        }
    }

    @Test
    void testToJson() {
        Person person = new Person("Aashish Karki", 24);
        String json = JsonUtils.toJson(person);
        assertNotNull(json);
        assertTrue(json.contains("\"name\":\"Aashish Karki\""));
        assertTrue(json.contains("\"age\":24"));
    }

    @Test
    void testFromJson() {
        String json = "{\"name\":\"Aashish Karki\",\"age\":24}";
        Person person = JsonUtils.fromJson(json, Person.class);
        assertEquals(new Person("Aashish Karki", 24), person);
    }

    @Test
    void testPrettyPrint() {
        String json = "{\"name\":\"Aashish Karki\",\"age\":24}";
        String prettyJson = JsonUtils.prettyPrint(json);
        assertNotNull(prettyJson);
        assertTrue(prettyJson.contains("{\n"));
        assertTrue(prettyJson.contains("\"name\" : \"Aashish Karki\""));
        assertTrue(prettyJson.contains("\"age\" : 24"));
    }

    @Test
    void testIsValidJson() {
        String validJson = "{\"name\":\"Aashish Karki\",\"age\":24}";
        assertTrue(JsonUtils.isValidJson(validJson));

        String invalidJson = "{\"name\":\"Aashish Karki\",\"age\":24";
        assertFalse(JsonUtils.isValidJson(invalidJson));
    }

    @Test
    void testFromJsonToList() {
        String json = "[{\"name\":\"John\",\"age\":30},{\"name\":\"Jane\",\"age\":25}]";
        List<Person> people = JsonUtils.fromJsonToList(json, Person.class);
        assertNotNull(people);
        assertEquals(2, people.size());
        assertEquals(new Person("John", 30), people.get(0));
        assertEquals(new Person("Jane", 25), people.get(1));
    }

    @Test
    void testFromJsonToMap() {
        String json = "{\"key1\":\"value1\",\"key2\":\"value2\"}";
        Map<String, String> map = JsonUtils.fromJsonToMap(json);
        assertNotNull(map);
        assertEquals(2, map.size());
        assertEquals("value1", map.get("key1"));
        assertEquals("value2", map.get("key2"));
    }

    @Test
    void testParseJson() {
        String json = "{\"name\":\"Aashish\",\"age\":24}";
        JsonNode node = JsonUtils.parseJson(json);
        assertNotNull(node);
        assertEquals("Aashish", node.get("name").asText());
        assertEquals(24, node.get("age").asInt());
    }

    @Test
    void testGetNodeValue() {
        String json = "{\"name\":\"Aashish\",\"age\":24}";
        String name = JsonUtils.getNodeValue(json, "name");
        assertEquals("Aashish", name);
    }

    @Test
    void testValidateJsonAgainstSchema() {
        String schema = "{ \"$schema\": \"http://json-schema.org/draft-07/schema#\", \"type\": \"object\", \"properties\": { \"name\": { \"type\": \"string\" }, \"age\": { \"type\": \"integer\" } }, \"required\": [\"name\", \"age\"] }";
        String validJson = "{\"name\":\"Aashish\",\"age\":24}";
        String invalidJson = "{\"name\":\"Aashish\"}";

        assertTrue(JsonUtils.validateJsonAgainstSchema(validJson, schema));
        assertFalse(JsonUtils.validateJsonAgainstSchema(invalidJson, schema));
    }

    @Test
    void testStreamJsonFile() throws IOException {
        File tempFile = File.createTempFile("test", ".json");
        tempFile.deleteOnExit();

        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("{\"name\":\"Aashish\",\"age\":24}");
        }

        JsonNode node = JsonUtils.streamJsonFile(tempFile.getAbsolutePath());
        assertNotNull(node);
        assertEquals("Aashish", node.get("name").asText());
        assertEquals(24, node.get("age").asInt());
    }

    @Test
    void testToXml() {
        Person person = new Person("Aashish Karki", 24);
        String xml = JsonUtils.toXml(person);
        assertNotNull(xml);
        assertTrue(xml.contains("<name>Aashish Karki</name>"));
        assertTrue(xml.contains("<age>24</age>"));
    }

    @Test
    void testFromXml() {
        String xml = "<Person><name>Aashish Karki</name><age>24</age></Person>";
        Person person = JsonUtils.fromXml(xml, Person.class);
        assertEquals(new Person("Aashish Karki", 24), person);
    }

    @Test
    void testConvertJsonToXml() throws JSONException {
        String json = "{\"name\":\"Aashish\",\"age\":24}";
        String xml = JsonUtils.convertJsonToXml(json);
        assertNotNull(xml);
        assertTrue(xml.contains("<name>Aashish</name>"));
        assertTrue(xml.contains("<age>24</age>"));
    }

    @Test
    void testConvertXmlToJson() throws JSONException {
        String xml = "<Person><name>Aashish</name><age>24</age></Person>";
        String json = JsonUtils.convertXmlToJson(xml);
        assertNotNull(json);
        assertTrue(json.contains("\"name\":\"Aashish\""));
        assertTrue(json.contains("\"age\":24"));
    }
}
