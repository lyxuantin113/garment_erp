package com.garment.erp.inventory.dto;

/**
 * Record dành cho dữ liệu gửi lên khi tạo/cập nhật Nhà cung cấp.
 */
public record SupplierRequest(
    String name,
    String phoneNumber,
    String email,
    String location
) {}
