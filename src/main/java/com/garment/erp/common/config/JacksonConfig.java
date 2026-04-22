package com.garment.erp.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Cấu hình Jackson để chuẩn hóa định dạng dữ liệu JSON.
 * 
 * Tại sao cần cấu hình này?
 * 1. Snake Case: Giúp API Response đồng bộ với DB Schema (đang dùng snake_case).
 * 2. Java Time: Hỗ trợ các kiểu dữ liệu mới như LocalDateTime.
 */
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        
        // Thiết lập quy tắc đặt tên: camelCase trong Java -> snake_case trong JSON
        // Ví dụ: createdAt -> created_at
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        
        // Đảm bảo hỗ trợ tốt Java 8 Date/Time
        objectMapper.registerModule(new JavaTimeModule());
        
        return objectMapper;
    }
}
