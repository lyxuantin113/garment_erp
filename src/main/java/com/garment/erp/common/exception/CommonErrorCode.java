package com.garment.erp.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Các mã lỗi chung nhất dùng cho toàn hệ thống.
 */
@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    SYSTEM_ERROR("SYS_001", "A system error occurred. Please contact admin."),
    BAD_REQUEST("SYS_002", "Invalid request data."),
    ENTITY_NOT_FOUND("SYS_003", "Requested entity not found."),
    UNAUTHORIZED("SYS_004", "You are not authorized to perform this action."),
    INTERNAL_SERVER_ERROR("SYS_005", "Internal server error.");

    private final String code;
    private final String message;
}
