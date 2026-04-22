package com.garment.erp.common.exception;

/**
 * Interface định nghĩa cấu trúc của một mã lỗi trong hệ thống.
 * 
 * Tại sao dùng Interface? 
 * Để mỗi module (Inventory, Order...) có thể tự tạo Enum lỗi riêng mà vẫn 
 * tuân thủ cấu trúc chung mà Common module yêu cầu.
 */
public interface ErrorCode {
    String getCode();
    String getMessage();
}
