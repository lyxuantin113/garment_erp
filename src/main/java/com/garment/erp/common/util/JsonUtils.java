package com.garment.erp.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Utility giúp xử lý JSON một cách nhanh chóng.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JsonUtils {

    private final ObjectMapper objectMapper;

    /**
     * Chuyển Object thành chuỗi JSON.
     */
    public String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert object to JSON", e);
            return null;
        }
    }

    /**
     * Chuyển chuỗi JSON ngược lại thành Object.
     */
    public <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert JSON to object", e);
            return null;
        }
    }
}
