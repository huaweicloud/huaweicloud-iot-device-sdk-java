/*
 * Copyright (c) 2020-2023 Huawei Cloud Computing Technology Co., Ltd. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 *    conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 *    of conditions and the following disclaimer in the documentation and/or other materials
 *    provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without specific prior written
 *    permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.huaweicloud.sdk.iot.device.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * json工具类
 */
public class JsonUtil {
    private static final Logger log = LogManager.getLogger(JsonUtil.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
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
            log.error("convert value failed " + e.getMessage());
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
                log.error("write value as string failed" + var3.getMessage());
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

    public static <T> T convertMap2Object(Map<String, Object> map, Class<T> cls) {
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
                log.error("read value failed" + var3.getMessage());
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
                log.error("read value failed" + var3.getMessage());
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
                log.error("write value as string failed" + var4.getMessage());
            }

            return rStr;
        }
    }
}
