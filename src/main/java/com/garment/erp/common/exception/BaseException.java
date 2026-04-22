package com.garment.erp.common.exception;

import lombok.Getter;

/**
 * BaseException là Exception cha cho mọi lỗi nghiệp vụ trong hệ thống.
 * 
 * Tại sao dùng RuntimeException? Để tránh việc phải try-catch rườm rà ở khắp
 * nơi,
 * thay vào đó ta sẽ để GlobalExceptionHandler xử lý ở lớp ngoài cùng.
 */
@Getter
public class BaseException extends RuntimeException {

    private final ErrorCode errorCode;

    public BaseException(ErrorCode errorCode) {
        super(errorCode.getMessage()); // RuntimeException (Throwable) lưu message
        this.errorCode = errorCode;
    }

    public BaseException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }
}
