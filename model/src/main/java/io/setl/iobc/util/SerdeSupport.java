package io.setl.iobc.util;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.kafka.support.serializer.JsonSerializer;

import io.setl.common.ParameterisedException;
import io.setl.json.CJObject;
import io.setl.json.jackson.CanonicalFactory;
import io.setl.json.jackson.JsonModule;

/**
 * Support for Jackson based serialization and deserialization.
 *
 * @author Simon Greatrix on 18/11/2021.
 */
public class SerdeSupport {

  private static final ObjectMapper OBJECT_MAPPER;


  /**
   * Convert from JSON to an object instance.
   *
   * @param rawData the JSON
   * @param type    the required object type
   * @param <T>     the required object type
   *
   * @return the object instance
   *
   * @throws ParameterisedException if the conversion fails
   */
  public static <T> T getInstance(String rawData, Class<T> type) throws ParameterisedException {
    ObjectMapper mapper = getObjectMapper();
    try {
      return mapper.readValue(rawData, type);
    } catch (JsonProcessingException e) {
      CJObject cjObject = new CJObject();
      cjObject.put("rawData", rawData);
      cjObject.put("message", e.getMessage());
      JsonLocation location = e.getLocation();
      if (location != null) {
        CJObject cj2 = new CJObject();
        cj2.put("byteOffset", location.getByteOffset());
        cj2.put("charOffset", location.getCharOffset());
        cj2.put("columnNumber", location.getColumnNr());
        cj2.put("lineNumber", location.getLineNr());
        cj2.put("source", location.sourceDescription());
        cjObject.put("location", cj2);
      }
      throw new ParameterisedException(e.getMessage(), "iobc:json-deserialize-exception", cjObject, e);
    }
  }


  /**
   * Get the JSON equivalent of an object.
   *
   * @param value the object to convert to JSON
   *
   * @return the JSON
   *
   * @throws ParameterisedException if the conversion fails
   */
  public static String getJson(Object value) throws ParameterisedException {
    ObjectMapper mapper = getObjectMapper();
    try {
      return mapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      CJObject cjObject = new CJObject();
      cjObject.put("sourceType", value.getClass().getName());
      cjObject.put("message", e.getMessage());
      throw new ParameterisedException(e.getMessage(), "iobc:json-serialize-exception", cjObject, e);
    }
  }


  public static ObjectMapper getObjectMapper() {
    return OBJECT_MAPPER;
  }


  /**
   * Get the JSON equivalent of an object.
   *
   * @param value the object to convert to JSON
   *
   * @return the JSON
   */
  public static String getPrettyJson(Object value) {
    ObjectMapper mapper = getObjectMapper();
    CJObject cjObject = mapper.convertValue(value, CJObject.class);
    return cjObject.toPrettyString();
  }


  public static <T> JsonDeserializer<T> newDeserializer(Class<T> type) {
    return new JsonDeserializer<T>(OBJECT_MAPPER.constructType(type), OBJECT_MAPPER);
  }


  public static <T> JsonSerde<T> newSerde(Class<T> type) {
    return new JsonSerde<>(OBJECT_MAPPER.constructType(type), OBJECT_MAPPER);
  }


  public static <T> JsonSerializer<T> newSerializer(Class<T> type) {
    return new JsonSerializer<T>(OBJECT_MAPPER.constructType(type), OBJECT_MAPPER);
  }


  static {
    ObjectMapper objectMapper = new ObjectMapper(new CanonicalFactory());
    objectMapper.registerModule(new JsonModule());
    objectMapper.registerModule(new Jdk8Module());
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.registerModule(new ParameterNamesModule());
    OBJECT_MAPPER = objectMapper;
  }

}
