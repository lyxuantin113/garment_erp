package com.garment.erp.common.dto;

import java.time.LocalDateTime;

/**
 * ApiResponse chuẩn hóa cấu trúc JSON trả về.
 * 
 * Sử dụng Record: Đảm bảo dữ liệu gửi đi không bị thay đổi giữa chừng.
 */
public record ApiResponse<T>(
    boolean success,
    String message,
    T data,
    ErrorDetail error,
    LocalDateTime timestamp
) {
    /**
     * Factory method cho trường hợp thành công.
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Operation successful", data, null, LocalDateTime.now());
    }

    /**
     * Factory method cho trường hợp lỗi.
     */
    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(false, message, null, new ErrorDetail(code, message), LocalDateTime.now());
    }
}
