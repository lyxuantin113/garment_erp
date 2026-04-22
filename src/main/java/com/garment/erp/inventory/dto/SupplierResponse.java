package com.garment.erp.inventory.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Record dành cho dữ liệu trả về của Nhà cung cấp.
 */
public record SupplierResponse(
    UUID id,
    String name,
    String phoneNumber,
    String email,
    String location,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Long version
) {}
