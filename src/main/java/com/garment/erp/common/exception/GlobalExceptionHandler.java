package com.garment.erp.common.exception;

import com.garment.erp.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * GlobalExceptionHandler là "tấm lưới" cuối cùng bắt mọi Exception
 * và ép nó về định dạng ApiResponse chuẩn.
 * 
 * Đây là linh hồn của việc xử lý lỗi trong Microservices:
 * Luôn trả về 1 format JSON duy nhất cho dù lỗi ở đâu.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Bắt lỗi nghiệp vụ do chúng ta chủ động throw (BaseException).
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Void>> handleBaseException(BaseException ex) {
        log.error("Business Error: code={}, message={}", ex.getErrorCode().getCode(), ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getErrorCode().getCode(), ex.getMessage()));
    }

    /**
     * Bắt lỗi Validation (ví dụ: thiếu field bắt buộc, sai định dạng email).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        log.error("Validation Error: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(CommonErrorCode.BAD_REQUEST.getCode(), "Dữ liệu không hợp lệ"));
    }

    /**
     * Bắt các lỗi hệ thống không mong muốn khác (NullPointer, DB Connection...).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception ex) {
        log.error("System Error: ", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(CommonErrorCode.SYSTEM_ERROR.getCode(),
                        CommonErrorCode.SYSTEM_ERROR.getMessage()));
    }
}
