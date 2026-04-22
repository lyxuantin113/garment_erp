package com.garment.erp.inventory.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Record dành cho dữ liệu trả về của Vật tư.
 */
public record MaterialResponse(
    UUID id,
    String code,
    String name,
    String baseUnit,
    String note,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Long version
) {}
