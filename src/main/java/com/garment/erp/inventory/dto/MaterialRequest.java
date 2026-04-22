package com.garment.erp.inventory.dto;

/**
 * Record dành cho dữ liệu gửi lên khi tạo/cập nhật Vật tư.
 */
public record MaterialRequest(
        String code,
        String name,
        String baseUnit,
        String note) {
}
