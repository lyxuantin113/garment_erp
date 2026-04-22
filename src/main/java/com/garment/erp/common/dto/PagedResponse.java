package com.garment.erp.common.dto;

import java.util.List;

/**
 * PagedResponse chuẩn hóa dữ liệu phân trang.
 */
public record PagedResponse<T>(
    List<T> content,
    int pageNumber,
    int pageSize,
    long totalElements,
    int totalPages,
    boolean last
) {}
