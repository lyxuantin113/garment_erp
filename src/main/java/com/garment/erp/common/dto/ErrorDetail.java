package com.garment.erp.common.dto;

/**
 * ErrorDetail định nghĩa cấu trúc chi tiết khi có lỗi xảy ra.
 * 
 * Sử dụng Record: Giúp DTO bất biến (Immutable), code ngắn gọn và tối ưu hiệu suất.
 */
public record ErrorDetail(
    String code,
    String message
) {}
