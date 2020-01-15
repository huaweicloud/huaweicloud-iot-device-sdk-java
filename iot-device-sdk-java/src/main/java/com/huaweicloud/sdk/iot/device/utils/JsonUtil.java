package com.huaweicloud.sdk.iot.device.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * json工具类
 */
public class JsonUtil {
    private static final Logger log = Logger.getLogger(JsonUtil.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public JsonUtil() {
    }

    public static <T> ObjectNode convertObject2ObjectNode(T object) {
        if (null == object) {
            return null;
        } else {
            ObjectNode objectNode = null;
            if (object instanceof String) {
                objectNode = (ObjectNode) convertJsonStringToObject((String) object, ObjectNode.class);
            } else {
                objectNode = (ObjectNode) convertValue(object, ObjectNode.class);
            }

            return objectNode;
        }
    }

    private static <T> T convertValue(Object fromValue, Class<T> toValueType) {
        try {
            return objectMapper.convertValue(fromValue, toValueType);
        } catch (IllegalArgumentException e) {
            log.error(ExceptionUtil.getBriefStackTrace(e));
            return null;
        }
    }

    public static <T> String convertObject2String(T object) {
        if (null == object) {
            return null;
        } else {
            String rStr = null;

            try {
                rStr = objectMapper.writeValueAsString(object);
                return rStr;
            } catch (JsonProcessingException var3) {
                var3.printStackTrace();
                return null;
            }
        }
    }

    public static <T> T convertObjectNode2Object(ObjectNode objectNode, Class<T> cls) {
        if (null == objectNode) {
            return null;
        }
        T object = null;
        object = convertValue(objectNode, cls);
        return object;

    }

    public static <T> T convertMap2Object(Map map, Class<T> cls) {
        if (null == map) {
            return null;
        }
        T object = null;
        object = convertValue(map, cls);
        return object;

    }

    public static <T> T convertJsonStringToObject(String jsonString, Class<T> cls) {
        if (jsonString == null) {
            return null;
        } else {
            try {
                T object = objectMapper.readValue(jsonString, cls);
                return object;
            } catch (Exception var3) {
                var3.printStackTrace();
                return null;
            }
        }
    }

    public static <T> T convertJsonStringToObject(String jsonString, TypeReference<?> valueTypeRef) {
        if (jsonString == null) {
            return null;
        } else {
            try {

                return (T) objectMapper.readValue(jsonString, valueTypeRef);
            } catch (Exception var3) {
                var3.printStackTrace();
                return null;
            }
        }
    }

    public static ObjectNode createObjectNode() {
        return objectMapper.createObjectNode();
    }

    public static String getJsonValue(JsonNode jsonNode, String fieldName) {
        JsonNode node = getJsonNode(jsonNode, fieldName);
        return node == null ? null : node.asText();
    }

    public static String getJsonValue2(JsonNode jsonNode, String fieldName) {
        JsonNode node = getJsonNode(jsonNode, fieldName);
        return null != node && !(node instanceof NullNode) ? node.asText() : null;
    }

    public static JsonNode getJsonNode(JsonNode jsonNode, String fieldName) {
        if (jsonNode != null) {
            JsonNode valueNode = jsonNode.findValue(fieldName);
            return valueNode;
        } else {
            log.error("The input is invalid.");
            return null;
        }
    }

    public static String convertObject2StringIgnoreNullField(Object object) {
        if (null == object) {
            return null;
        } else {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            String rStr = null;

            try {
                rStr = mapper.writeValueAsString(object);
            } catch (JsonProcessingException var4) {
                var4.printStackTrace();
            }

            return rStr;
        }
    }
}
