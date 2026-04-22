package com.garment.erp.common.test;

import java.time.LocalDateTime;

/**
 * DTO dùng để test việc chuyển đổi sang snake_case.
 */
public record TestDto(
    String firstName,
    String lastName,
    String emailAddress,
    LocalDateTime joinedAt
) {}
