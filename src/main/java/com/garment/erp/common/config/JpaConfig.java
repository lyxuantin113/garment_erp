package com.garment.erp.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;
import java.util.UUID;

/**
 * Cấu hình dành riêng cho JPA.
 */
@Configuration
public class JpaConfig {

    /**
     * Bean này giúp Spring Data JPA biết "Ai" là người đang thực hiện hành động.
     * 
     * Trong thực tế: Chúng ta sẽ lấy UUID của user từ Spring Security SecurityContext.
     * Hiện tại: Chúng ta trả về một UUID cố định (System User) để hệ thống có thể chạy được.
     */
    @Bean
    public AuditorAware<UUID> auditorProvider() {
        // Giả lập một System User ID
        return () -> Optional.of(UUID.fromString("00000000-0000-0000-0000-000000000000"));
    }
}
