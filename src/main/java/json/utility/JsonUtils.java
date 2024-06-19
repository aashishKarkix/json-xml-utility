package json.utility;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Utility class providing methods for JSON and XML serialization/deserialization,
 * JSON validation against schemas, and conversion between JSON and XML formats.
 * Includes methods for converting Java objects to JSON/XML, parsing JSON strings,
 * validating JSON against JSON schema, and more.
 *
 * <p>This class uses Jackson ObjectMapper for JSON handling and XMLMapper for XML handling.
 * It supports conversion of LocalDate objects to ISO format during serialization and
 * deserialization.
 *
 * <p>Author: Aashish Karki
 */
public class JsonUtils {
    private static Logger logger = LoggerFactory.getLogger(JsonUtils.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final XmlMapper xmlMapper = new XmlMapper();

    static class LocalDateSerializer extends JsonSerializer<LocalDate> {
        @Override
        public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
    }

    static class LocalDateDeserializer extends JsonDeserializer<LocalDate> {
        @Override
        public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return LocalDate.parse(p.getText(), DateTimeFormatter.ISO_LOCAL_DATE);
        }
    }

    static {
        SimpleModule module = new SimpleModule();
        module.addSerializer(LocalDate.class, new LocalDateSerializer());
        module.addDeserializer(LocalDate.class, new LocalDateDeserializer());
        objectMapper.registerModule(module);

        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private JsonUtils() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    /**
     * Converts a Java object to its JSON representation.
     *
     * @param object The Java object to convert.
     * @return JSON string representing the object.
     */
    public static String toJson(Object object) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            logger.error("Error converting object to JSON: {}", object, e);
            return null;
        }
    }

    /**
     * Converts a JSON string into an instance of the specified class.
     *
     * @param json  The JSON string to deserialize.
     * @param clazz The class type to deserialize into.
     * @param <T>   The type of the class.
     * @return An instance of the specified class.
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            logger.error("Error parsing JSON: {}", json, e);
            return null;
        }
    }

    /**
     * Returns a formatted (pretty-printed) JSON string from an input JSON string.
     *
     * @param json The JSON string to pretty-print.
     * @return Formatted JSON string.
     */
    public static String prettyPrint(String json) {
        try {
            Object jsonObject = objectMapper.readValue(json, Object.class);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
        } catch (JsonProcessingException e) {
            logger.error("Error pretty-printing JSON: {}", json, e);
            return json;
        }
    }

    /**
     * Checks if a given string is a valid JSON.
     *
     * @param json The JSON string to validate.
     * @return true if the string is valid JSON, false otherwise.
     */
    public static boolean isValidJson(String json) {
        try {
            objectMapper.readTree(json);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    /**
     * Converts a JSON array string into a List of objects of the specified class type.
     *
     * @param json  The JSON array string to deserialize.
     * @param clazz The class type of objects in the list.
     * @param <T>   The type of objects in the list.
     * @return List of objects deserialized from JSON.
     */
    public static <T> List<T> fromJsonToList(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, TypeFactory.defaultInstance().constructCollectionType(List.class, clazz));
        } catch (JsonProcessingException e) {
            logger.error("Error parsing JSON to List: {}", json, e);
            return Collections.emptyList();
        }
    }

    /**
     * Converts a JSON object string into a Map.
     *
     * @param json The JSON object string to deserialize.
     * @param <K>  The type of keys in the map.
     * @param <V>  The type of values in the map.
     * @return Map representing the JSON object.
     */
    public static <K, V> Map<K, V> fromJsonToMap(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            logger.error("Error parsing JSON to Map: {}", json, e);
            return Collections.emptyMap();
        }
    }

    /**
     * Parses a JSON string into a JsonNode object for further processing.
     *
     * @param json The JSON string to parse.
     * @return JsonNode representing the parsed JSON.
     */
    public static JsonNode parseJson(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            logger.error("Error parsing JSON node: {}", json, e);
            return null;
        }
    }

    /**
     * Retrieves the value of a specified key from a JSON string.
     *
     * @param json The JSON string.
     * @param key  The key whose value is to be retrieved.
     * @return Value associated with the specified key, or null if the key is not found.
     */
    public static String getNodeValue(String json, String key) {
        try {
            JsonNode node = objectMapper.readTree(json);
            return node.get(key).asText();
        } catch (JsonProcessingException e) {
            logger.error("Error getting node value: {} from JSON: {}", key, json, e);
            return null;
        }
    }

    /**
     * Validates a JSON string against a given JSON schema.
     *
     * @param json   The JSON string to validate.
     * @param schema The JSON schema string.
     * @return true if the JSON is valid against the schema, false otherwise.
     */
    public static boolean validateJsonAgainstSchema(String json, String schema) {
        try {
            JSONObject jsonSchema = new JSONObject(new JSONTokener(schema));
            JSONObject jsonObject = new JSONObject(new JSONTokener(json));

            Schema schemaObj = SchemaLoader.load(jsonSchema);
            schemaObj.validate(jsonObject);
            return true;
        } catch (ValidationException e) {
            logger.error("JSON validation error: {}", json, e);
            return false;
        }
    }

    /**
     * Streams a JSON file from the specified file path and returns the root JsonNode.
     *
     * @param filePath The file path of the JSON file.
     * @return JsonNode representing the root of the JSON document, or null if an error occurs.
     */
    public static JsonNode streamJsonFile(String filePath) {
        try (JsonParser parser = objectMapper.getFactory().createParser(new File(filePath))) {
            return objectMapper.readTree(parser);
        } catch (IOException e) {
            logger.error("Error streaming JSON file: {}", filePath, e);
            return null;
        }
    }


    /**
     * Converts a Java object to its XML representation.
     *
     * @param object The Java object to convert.
     * @return XML string representing the object.
     */
    public static String toXml(Object object) {
        try {
            return xmlMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            logger.error("Error converting object to XML: {}", object, e);
            return null;
        }
    }

    /**
     * Converts an XML string into an instance of the specified class.
     *
     * @param xml   The XML string to deserialize.
     * @param clazz The class type to deserialize into.
     * @param <T>   The type of the class.
     * @return An instance of the specified class.
     */
    public static <T> T fromXml(String xml, Class<T> clazz) {
        try {
            return xmlMapper.readValue(xml, clazz);
        } catch (JsonProcessingException e) {
            logger.error("Error parsing XML: {}", xml, e);
            return null;
        }
    }

    /**
     * Converts a JSON string to its equivalent XML representation.
     *
     * @param json The JSON string to convert.
     * @return XML string representing the JSON data.
     */
    public static String convertJsonToXml(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            return XML.toString(jsonObject);
        } catch (JSONException e) {
            logger.error("Error converting JSON to XML: {}", json, e);
            return null;
        }
    }

    /**
     * Converts an XML string to its equivalent JSON representation.
     *
     * @param xml The XML string to convert.
     * @return JSON string representing the XML data.
     */
    public static String convertXmlToJson(String xml) {
        try {
            JSONObject jsonObject = XML.toJSONObject(xml);
            return jsonObject.toString();
        } catch (JSONException e) {
            logger.error("Error converting XML to JSON: {}", xml, e);
            return null;
        }
    }
}
